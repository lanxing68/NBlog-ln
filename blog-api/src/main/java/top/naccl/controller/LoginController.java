package top.naccl.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import top.naccl.constant.JwtConstants;
import top.naccl.constant.RedisKeyConstants;
import top.naccl.entity.User;
import top.naccl.model.dto.LoginInfo;
import top.naccl.model.vo.Result;
import top.naccl.service.RedisService;
import top.naccl.service.UserService;
import top.naccl.util.CaptchaUtils;
import top.naccl.util.JwtUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 前台登录
 * @Author: Naccl
 * @Date: 2020-09-02
 */
@RestController
public class LoginController {
	@Autowired
	UserService userService;
	@Autowired
	private RedisService redisService;

	/**
	 * 登录成功后，签发博主身份Token
	 *
	 * @param loginInfo
	 * @return
	 */
	@PostMapping("/login")
	public Result login(@RequestBody LoginInfo loginInfo) {
		String redisKey = RedisKeyConstants.CAPTCHA_PREFIX + loginInfo.getCaptchaId();
		String correctCode = redisService.getStringByKey(redisKey);
		if (correctCode == null || correctCode.isEmpty()) {
			return Result.error("验证码已过期，请刷新");
		}
		if (!correctCode.equalsIgnoreCase(loginInfo.getCaptchaCode())) {
			return Result.error("验证码错误");
		}
		// 验证通过后立即删除，防止重复使用
		redisService.deleteCacheByKey(redisKey);

		User user = userService.findUserByUsernameAndPassword(loginInfo.getUsername(), loginInfo.getPassword());
		if (user == null) {
			return Result.error("用户名或密码错误");
		}
		if (!"ROLE_admin".equals(user.getRole())) {
			return Result.create(403, "无权限");
		}
		user.setPassword(null);
		String jwt = JwtUtils.generateToken(JwtConstants.ADMIN_PREFIX + user.getUsername());
		Map<String, Object> map = new HashMap<>(4);
		map.put("user", user);
		map.put("token", jwt);
		return Result.ok("登录成功", map);
	}

	@GetMapping("/captcha")
	public Result getCaptcha() {
		String[] captcha = CaptchaUtils.generate();
		String base64 = captcha[0];
		String code = captcha[1];
		String uuid = java.util.UUID.randomUUID().toString();
		String redisKey = RedisKeyConstants.CAPTCHA_PREFIX + uuid;
		redisService.saveStringWithExpireTime(redisKey, code, 5, java.util.concurrent.TimeUnit.MINUTES);
		Map<String, Object> map = new HashMap<>();
		map.put("captchaId", uuid);
		map.put("captchaImage", base64);
		return Result.ok("获取验证码成功", map);
	}
}
