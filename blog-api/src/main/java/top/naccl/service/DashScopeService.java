package top.naccl.service;

public interface DashScopeService {
	/**
	 * 生成博客文章摘要
	 */
	String summarizeBlog(Long blogId);

	/**
	 * 基于博客文章内容回答问题
	 */
	String chatAboutBlog(Long blogId, String question);
}
