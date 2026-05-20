package top.naccl.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import top.naccl.annotation.AccessLimit;
import top.naccl.model.vo.ChatRequest;
import top.naccl.model.vo.Result;
import top.naccl.service.BlogService;
import top.naccl.service.DashScopeService;

import java.util.Arrays;
import java.util.List;

@RestController
public class AiController {
	@Autowired
	DashScopeService dashScopeService;
	@Autowired
	BlogService blogService;

	// Prompt 注入检测关键词
	private static final List<String> INJECTION_KEYWORDS = Arrays.asList(
		"ignore", "forget", "system prompt", "你是", "你的指令",
		"忽略", "忘记", "重新设定", "忽略之前的", "忽略上面"
	);

	@AccessLimit(seconds = 60, maxCount = 10, msg = "AI 请求过于频繁，请稍后再试")
	@PostMapping("/ai/summarize/{blogId}")
	public Result summarize(@PathVariable Long blogId) {
		if (blogService.getBlogByIdAndIsPublished(blogId) == null) {
			return Result.error("文章不存在");
		}
		String summary = dashScopeService.summarizeBlog(blogId);
		return Result.ok("获取成功", summary);
	}

	@AccessLimit(seconds = 60, maxCount = 10, msg = "AI 请求过于频繁，请稍后再试")
	@PostMapping("/ai/chat")
	public Result chat(@RequestBody ChatRequest request) {
		// ① 参数判空
		if (request.getBlogId() == null) {
			return Result.error("文章ID不能为空");
		}
		String question = request.getQuestion();
		if (question == null || question.trim().isEmpty()) {
			return Result.error("问题不能为空");
		}

		// ② 长度限制：防恶意超长输入（大模型 token 有上限）
		if (question.length() > 500) {
			return Result.error("问题长度不能超过500字");
		}

		// ③ Prompt 注入检测：防用户用指令覆盖系统 Prompt
		if (containsInjection(question)) {
			return Result.error("输入包含非法内容");
		}

		String answer = dashScopeService.chatAboutBlog(request.getBlogId(), question);
		return Result.ok("获取成功", answer);
	}

	/**
	 * 检测用户输入是否包含试图覆盖系统 Prompt 的指令
	 */
	private boolean containsInjection(String input) {
		String lower = input.toLowerCase().trim();
		for (String keyword : INJECTION_KEYWORDS) {
			if (lower.contains(keyword)) {
				return true;
			}
		}
		return false;
	}
}
