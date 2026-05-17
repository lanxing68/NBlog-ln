package top.naccl.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import top.naccl.model.vo.ChatRequest;
import top.naccl.model.vo.Result;
import top.naccl.service.BlogService;
import top.naccl.service.DashScopeService;

@RestController
public class AiController {
	@Autowired
	DashScopeService dashScopeService;
	@Autowired
	BlogService blogService;

	@PostMapping("/ai/summarize/{blogId}")
	public Result summarize(@PathVariable Long blogId) {
		if (blogService.getBlogByIdAndIsPublished(blogId) == null) {
			return Result.error("文章不存在");
		}
		String summary = dashScopeService.summarizeBlog(blogId);
		return Result.ok("获取成功", summary);
	}

	@PostMapping("/ai/chat")
	public Result chat(@RequestBody ChatRequest request) {
		if (request.getBlogId() == null || request.getQuestion() == null) {
			return Result.error("参数错误");
		}
		String answer = dashScopeService.chatAboutBlog(request.getBlogId(), request.getQuestion());
		return Result.ok("获取成功", answer);
	}
}
