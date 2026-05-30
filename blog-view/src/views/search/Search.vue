<template>
  <div>
    <h3>搜索 "{{ keyword }}" 的结果</h3>
    <div v-for="blog in blogList" :key="blog.id" class="search-item">
      <router-link :to="'/blog/' + blog.id">
        <h4 v-html="blog.title"></h4>
      </router-link>
      <p v-html="blog.description"></p>
      <span>{{ blog.createTime | dateFormat('YYYY-MM-DD') }}</span>
    </div>
    <div v-if="blogList.length === 0" style="text-align:center;padding:50px">
      没有找到相关文章
    </div>

    <div v-if="searchHistory.length > 0" style="margin-top:40px">
      <h3 class="ui dividing header">最近搜索</h3>
      <div class="ui labels">
        <a class="ui label" v-for="(word, index) in searchHistory" :key="index"
           @click="$router.push({ name: 'search', query: { keyword: word } })"
           style="cursor:pointer;margin:5px">
          <i class="history icon"></i> {{ word }}
        </a>
      </div>
    </div>
  </div>
</template>

<script>
import { getSearchBlogList, getSearchHistory } from '@/api/blog'

export default {
  name: "Search",
  data() {
    return {
      keyword: '',
      blogList: [],
      searchHistory: []
    }
  },
  created() {
    this.keyword = this.$route.query.keyword || ''
    if (this.keyword) {
      getSearchBlogList(this.keyword).then(res => {
        if (res.code === 200) {
          this.blogList = res.data
        }
      })
    }
    getSearchHistory().then(res => {
      if (res.code === 200) {
        this.searchHistory = res.data || []
      }
    })
  }
}
</script>
