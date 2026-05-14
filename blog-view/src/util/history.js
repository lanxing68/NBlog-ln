const HISTORY_KEY = 'blog_history'

/**
 * 获取历史记录
 */
export function getHistory() {
    const history = localStorage.getItem(HISTORY_KEY)
    return history ? JSON.parse(history) : []
}

/**
 * 添加历史记录
 */
export function addHistory(blog) {

    let history = getHistory()

    // 去重
    history = history.filter(item => item.id !== blog.id)

    // 插入头部
    history.unshift({
        id: blog.id,
        title: blog.title,
        createTime: blog.createTime
    })

    // 最多保存10条
    if (history.length > 10) {
        history = history.slice(0, 10)
    }

    localStorage.setItem(HISTORY_KEY, JSON.stringify(history))
}