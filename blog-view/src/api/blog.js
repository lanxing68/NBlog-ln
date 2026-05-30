import axios from '@/plugins/axios'

export function getBlogById(token, id) {
	return axios({
		url: 'blog',
		method: 'GET',
		headers: {
			Authorization: token,
		},
		params: {
			id
		}
	})
}
 export function getRelatedBlogs(id, categoryId){
    return axios({
        url:'blog/related',
        method:'GET',
        params:{
            id,categoryId
        }

    })


}


export function checkBlogPassword(blogPasswordForm) {
	return axios({
		url: 'checkBlogPassword',
		method: 'POST',
		data: {
			...blogPasswordForm
		}
	})
}

export function getSearchBlogList(query) {
	return axios({
		url: 'searchBlog',
		method: 'GET',
		params: {
			query
		}
	})

}
export function likeBlog(id, visitorId) {
    return axios({
        url: 'blog/' + id + '/like',
        method: 'POST',
        headers: { identification: visitorId }
    })
}

export function getSearchHistory() {
    return axios({
        url: 'searchHistory',
        method: 'GET'
    })
}

// 获取点赞信息
export function getLikeInfo(id, visitorId) {
    return axios({
        url: 'blog/' + id + '/like',
        method: 'GET',
        headers: visitorId ? { identification: visitorId } : {}
    })
}
