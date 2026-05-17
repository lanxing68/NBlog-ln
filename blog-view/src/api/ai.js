import axios from '@/plugins/axios'

export function getAISummary(blogId) {
	return axios({
		url: 'ai/summarize/' + blogId,
		method: 'POST'
	})
}

export function askAI(blogId, question) {
	return axios({
		url: 'ai/chat',
		method: 'POST',
		data: {
			blogId: blogId,
			question: question
		}
	})
}
