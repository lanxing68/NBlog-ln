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
  </div>
</template>

<script>
import { searchByTitle } from '@/api/blog'

export default {
  name: "Search",
  data() {
    return {
      keyword: '',
      blogList: []
    }
  },
  created() {
    this.keyword = this.$route.query.keyword || ''
    if (this.keyword) {
      searchByTitle(this.keyword).then(res => {
        if (res.code === 200) {
          this.blogList = res.data
        }
      })
    }
  }
}
</script>
