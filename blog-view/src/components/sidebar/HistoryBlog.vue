<template>
  <div class="ui segments m-box">
    <div class="ui secondary segment">
      <i class="history icon"></i>
      浏览历史
    </div>

    <div class="ui segment">

      <div
          class="history-item"
          v-for="blog in historyList"
          :key="blog.id"
          @click="toBlog(blog)"
      >
        {{ blog.title }}
      </div>

    </div>
  </div>
</template>

<script>

import { getHistory } from '@/util/history'

export default {

  name: "HistoryBlog",

  data() {
    return {
      historyList: []
    }
  },

  mounted() {
    this.historyList = getHistory()
  },

  methods: {

    toBlog(blog) {
      this.$store.dispatch('goBlogPage', blog)
    }

  }
}
</script>

<style scoped>

.history-item {

  padding: 8px 0;

  cursor: pointer;

  border-bottom: 1px solid #f0f0f0;

  transition: all .2s;
}

.history-item:hover {

  color: #49b1f5;

  transform: translateX(3px);
}

</style>