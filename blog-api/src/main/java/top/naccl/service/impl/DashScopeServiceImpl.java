package top.naccl.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import top.naccl.config.properties.AiProperties;
import top.naccl.entity.Blog;
import top.naccl.mapper.BlogMapper;
import top.naccl.service.DashScopeService;
import top.naccl.service.RedisService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class DashScopeServiceImpl implements DashScopeService {

	@Autowired
	@Qualifier("restTemplate")
	private RestTemplate restTemplate;

	@Autowired
	private AiProperties aiProperties;

	@Autowired
	private BlogMapper blogMapper;

	@Autowired
	private RedisService redisService;

	private static final String API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
	private static final String SUMMARY_CACHE_KEY = "ai:summary:";
	private static final long SUMMARY_CACHE_DAYS = 30;

	@Override
	public String summarizeBlog(Long blogId) {
		String cacheKey = SUMMARY_CACHE_KEY + blogId;
		String cached = redisService.getStringByKey(cacheKey);
		if (cached != null) {
			return cached;
		}

		Blog blog = blogMapper.getBlogById(blogId);
		if (blog == null) {
			return null;
		}

		String content = blog.getContent();
		if (content != null && content.length() > 8000) {
			content = content.substring(0, 8000);
		}

		String summary = callApi(
			"你是一个专业的技术博客助手，请用3-5句话简洁地总结以下文章的核心内容，使用中文。",
			"文章标题：" + blog.getTitle() + "\n\n文章内容：\n" + content
		);

		if (summary != null) {
			redisService.saveStringWithExpireTime(cacheKey, summary, SUMMARY_CACHE_DAYS, TimeUnit.DAYS);
		}
		return summary;
	}

	@Override
	public String chatAboutBlog(Long blogId, String question) {
		Blog blog = blogMapper.getBlogById(blogId);
		if (blog == null) {
			return "文章不存在";
		}

		String content = blog.getContent();
		if (content != null && content.length() > 6000) {
			content = content.substring(0, 6000);
		}

		return callApi(
			"你是一个友好的技术博客助手。请基于提供的文章内容回答读者的问题。" +
			"如果文章中没有相关信息，请诚实告知。使用中文回答。",
			"文章标题：" + blog.getTitle() + "\n\n文章内容：\n" + content + "\n\n读者提问：" + question
		);
	}

	private String callApi(String systemPrompt, String userMessage) {
		if (aiProperties.getApiKey() == null || "你的API_KEY".equals(aiProperties.getApiKey())) {
			return "AI 服务未配置，请在 application-dev.properties 中设置 ai.dashscope.api-key";
		}

		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setBearerAuth(aiProperties.getApiKey());

			List<Map<String, String>> messages = new ArrayList<>();
			Map<String, String> systemMsg = new HashMap<>();
			systemMsg.put("role", "system");
			systemMsg.put("content", systemPrompt);
			messages.add(systemMsg);

			Map<String, String> userMsg = new HashMap<>();
			userMsg.put("role", "user");
			userMsg.put("content", userMessage);
			messages.add(userMsg);

			Map<String, Object> body = new HashMap<>();
			body.put("model", aiProperties.getModel());
			body.put("messages", messages);
			body.put("temperature", aiProperties.getTemperature());
			body.put("max_tokens", aiProperties.getMaxTokens());

			HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
			Map<String, Object> response = restTemplate.postForObject(API_URL, request, Map.class);

			if (response != null) {
				List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
				if (choices != null && !choices.isEmpty()) {
					Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
					if (message != null) {
						return (String) message.get("content");
					}
				}
			}
			return "AI 返回内容为空";
		} catch (Exception e) {
			return "AI 服务调用失败：" + e.getMessage();
		}
	}
}
