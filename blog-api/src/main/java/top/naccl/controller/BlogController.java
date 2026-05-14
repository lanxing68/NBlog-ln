package top.naccl.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.naccl.annotation.VisitLogger;
import top.naccl.constant.JwtConstants;
import top.naccl.entity.User;
import top.naccl.enums.VisitBehavior;
import top.naccl.model.dto.BlogPassword;
import top.naccl.model.vo.BlogDetail;
import top.naccl.model.vo.BlogInfo;
import top.naccl.model.vo.PageResult;
import top.naccl.model.vo.Result;
import top.naccl.model.vo.SearchBlog;
import top.naccl.service.BlogService;
import top.naccl.service.impl.UserServiceImpl;
import top.naccl.util.JwtUtils;
import top.naccl.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 博客相关
 * @Author: Naccl
 * @Date: 2020-08-12
 */
@RestController
public class BlogController {
	@Autowired
	BlogService blogService;
	@Autowired
	UserServiceImpl userService;

	/**
	 * 按置顶、创建时间排序 分页查询博客简要信息列表
	 *
	 * @param pageNum 页码
	 * @return
	 */
	@VisitLogger(VisitBehavior.INDEX)
	@GetMapping("/blogs")
	public Result blogs(@RequestParam(defaultValue = "1") Integer pageNum) {
        PageResult<BlogInfo> pageResult = blogService.getBlogInfoListByIsPublished(pageNum);
        return Result.ok("请求成功", pageResult);
	}

	/**
	 * 按id获取公开博客详情
	 *
	 * @param id  博客id
	 * @param jwt 密码保护文章的访问Token
	 * @return
	 */
	@VisitLogger(VisitBehavior.BLOG)
	@GetMapping("/blog")
	public Result getBlog(@RequestParam Long id,
	                      @RequestHeader(value = "Authorization", defaultValue = "") String jwt) {
		BlogDetail blog = blogService.getBlogByIdAndIsPublished(id);
		//对密码保护的文章校验Token
		if (!"".equals(blog.getPassword())) {
			if (JwtUtils.judgeTokenIsExist(jwt)) {
				try {
					String subject = JwtUtils.getTokenBody(jwt).getSubject();
					if (subject.startsWith(JwtConstants.ADMIN_PREFIX)) {
						//博主身份Token
						String username = subject.replace(JwtConstants.ADMIN_PREFIX, "");
						User admin = (User) userService.loadUserByUsername(username);
						if (admin == null) {
							return Result.create(403, "博主身份Token已失效，请重新登录！");
						}
					} else {
						//经密码验证后的Token
						Long tokenBlogId = Long.parseLong(subject);
						//博客id不匹配，验证不通过，可能博客id改变或客户端传递了其它密码保护文章的Token
						if (!tokenBlogId.equals(id)) {
							return Result.create(403, "Token不匹配，请重新验证密码！");
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					return Result.create(403, "Token已失效，请重新验证密码！");
				}
			} else {
				return Result.create(403, "此文章受密码保护，请验证密码！");
			}
			blog.setPassword("");
		}
		blogService.updateViewsToRedis(id);
		return Result.ok("获取成功", blog);
	}

	/**
	 * 校验受保护文章密码是否正确，正确则返回jwt
	 *
	 * @param blogPassword 博客id、密码
	 * @return
	 */
	@VisitLogger(VisitBehavior.CHECK_PASSWORD)
	@PostMapping("/checkBlogPassword")
	public Result checkBlogPassword(@RequestBody BlogPassword blogPassword) {
		String password = blogService.getBlogPassword(blogPassword.getBlogId());
		if (password.equals(blogPassword.getPassword())) {
			//生成有效时间一个月的Token
			String jwt = JwtUtils.generateToken(blogPassword.getBlogId().toString(), 1000 * 3600 * 24 * 30L);
			return Result.ok("密码正确", jwt);
		} else {
			return Result.create(403, "密码错误");
		}
	}

	/**
	 * 按关键字根据文章内容搜索公开且无密码保护的博客文章
	 *
	 * @param query 关键字字符串
	 * @return
	 */
	@VisitLogger(VisitBehavior.SEARCH)
	@GetMapping("/searchBlog")
	public Result searchBlog(@RequestParam String query) {
		//校验关键字字符串合法性
		if (StringUtils.isEmpty(query) || StringUtils.hasSpecialChar(query) || query.trim().length() > 20) {
			return Result.error("参数错误");
		}
		List<SearchBlog> searchBlogs = blogService.getSearchBlogListByQueryAndIsPublished(query.trim());
		return Result.ok("获取成功", searchBlogs);
	}
    @PostMapping("/blog/{id}/like")
    public Result likeBlog(@PathVariable Long id,
                           @RequestHeader(value = "identification", required = false) String visitorId) {
        if (visitorId == null) {
            return Result.error("请先访问网站获取访客标识");
        }
        boolean success = blogService.likeBlog(id, visitorId);
        if (success) {
            return Result.ok("点赞成功");
        } else {
            return Result.error("您已经点过赞了");
        }
    }

    /**
     * 获取点赞数和点赞状态
     */
    @GetMapping("/blog/{id}/like")
    public Result getLikeInfo(@PathVariable Long id,
                              @RequestHeader(value = "identification", required = false) String visitorId) {
        int count = blogService.getBlogLikeCount(id);
        boolean liked = visitorId != null && blogService.hasLikedBlog(id, visitorId);
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        result.put("liked", liked);
        return Result.ok("获取成功", result);
    }

    @GetMapping("/searchTitle")
    public Result searchTitle(@RequestParam String keyword) {
        if (keyword == null || keyword.trim().length() < 1) {
            return Result.error("请输入搜索关键词");
        }
        if (keyword.length() > 20) {
            return Result.error("关键词不能超过20个字符");
        }
        List<BlogInfo> list = blogService.searchBlogByTitle(keyword);
        // 给返回结果加上高亮标记
        for (BlogInfo blog : list) {
            // 在标题中高亮关键词
            String title = blog.getTitle();
            title = title.replaceAll("(?i)(" + java.util.regex.Pattern.quote(keyword) + ")",
                    "<mark>$1</mark>");
            blog.setTitle(title);
        }
        return Result.ok("搜索成功", list);
    }




}
