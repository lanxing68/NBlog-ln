<template>
  <!--热门文章-->
  <div class="ui segments m-box">
    <div class="ui secondary segment"><i class="fire icon"></i>热门文章</div>
    <div class="ui yellow segment">
      <div class="ui divided items">
        <div class="m-item" v-for="(blog, index) in hotBlogList" :key="blog.id" @click.prevent="toBlog(blog)">
          <div class="rank" :class="'rank-' + (index + 1)">{{ index + 1 }}</div>
          <div class="info">
            <div class="title">{{ blog.title }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "HotBlog",
  props: {
    hotBlogList: {
      type: Array,
      required: true
    },
  },
  methods: {
    toBlog(blog) {
      this.$store.dispatch('goBlogPage', blog)
    }
  }
}
</script>

<style scoped>
.secondary.segment {
  padding: 10px;
}

.ui.divided.items .m-item:first-child {
  margin-top: 0;
}

.ui.divided.items .m-item {
  display: flex;
  align-items: center;
  margin-top: 0.5rem;
  padding: 0.3rem 0;
  cursor: pointer;
  user-select: none;
}

.ui.divided.items .m-item .rank {
  width: 22px;
  height: 22px;
  line-height: 22px;
  text-align: center;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
  color: #fff;
  background: #999;
  margin-right: 0.6rem;
  flex-shrink: 0;
}

.rank.rank-1 { background: #e74c3c; }
.rank.rank-2 { background: #e67e22; }
.rank.rank-3 { background: #f1c40f; }

.ui.divided.items .m-item .info .title {
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
  word-break: break-word;
}
</style>
