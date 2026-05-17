<template>
	<div v-if="summary || loading">
		<div class="ui icon message">
			<i class="robot icon"></i>
			<div class="content">
				<div class="header">AI 摘要</div>
				<p v-if="loading" style="color: #999">正在生成摘要...</p>
				<p v-else>{{ summary }}</p>
			</div>
		</div>
	</div>
</template>

<script>
import { getAISummary } from '@/api/ai'

export default {
	name: "AISummary",
	props: {
		blogId: Number
	},
	data() {
		return {
			summary: '',
			loading: false
		}
	},
	watch: {
		blogId() {
			this.fetchSummary()
		}
	},
	created() {
		this.fetchSummary()
	},
	methods: {
		fetchSummary() {
			if (!this.blogId) return
			this.loading = true
			this.summary = ''
			getAISummary(this.blogId).then(res => {
				if (res.code === 200 && res.data) {
					this.summary = res.data
				}
			}).catch(() => {}).finally(() => {
				this.loading = false
			})
		}
	}
}
</script>
