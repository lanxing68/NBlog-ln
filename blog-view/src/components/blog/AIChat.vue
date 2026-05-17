<template>
	<div class="ai-chat-wrapper">
		<div class="ai-chat-btn" @click="togglePanel" :class="{ active: visible }">
			<i class="comments icon"></i>
		</div>

		<transition name="slide-up">
			<div class="ai-chat-panel" v-if="visible">
				<div class="ai-chat-header">
					<span>AI 助手</span>
					<i class="close icon" @click="visible = false"></i>
				</div>
				<div class="ai-chat-body" ref="chatBody">
					<div v-if="messages.length === 0" class="ai-chat-empty">
						基于当前文章内容向我提问吧~
					</div>
					<div v-for="(msg, idx) in messages" :key="idx" :class="['ai-chat-bubble', msg.role]">
						{{ msg.content }}
					</div>
					<div v-if="loading" class="ai-chat-bubble assistant" style="color: #999">
						思考中...
					</div>
				</div>
				<div class="ai-chat-footer">
					<input v-model="input" @keyup.enter="send" placeholder="输入你的问题..." />
					<button @click="send" :disabled="!input.trim() || loading">
						<i class="paper plane icon"></i>
					</button>
				</div>
			</div>
		</transition>
	</div>
</template>

<script>
import { askAI } from '@/api/ai'

export default {
	name: "AIChat",
	props: {
		blogId: Number
	},
	data() {
		return {
			visible: false,
			input: '',
			messages: [],
			loading: false
		}
	},
	methods: {
		togglePanel() {
			this.visible = !this.visible
		},
		send() {
			const q = this.input.trim()
			if (!q || this.loading) return
			this.messages.push({ role: 'user', content: q })
			this.input = ''
			this.loading = true
			this.$nextTick(() => {
				this.scrollToBottom()
			})
			askAI(this.blogId, q).then(res => {
				if (res.code === 200) {
					this.messages.push({ role: 'assistant', content: res.data })
				} else {
					this.messages.push({ role: 'assistant', content: '抱歉，出了点问题: ' + res.msg })
				}
			}).catch(() => {
				this.messages.push({ role: 'assistant', content: '网络请求失败，请稍后再试' })
			}).finally(() => {
				this.loading = false
				this.$nextTick(() => {
					this.scrollToBottom()
				})
			})
		},
		scrollToBottom() {
			const el = this.$refs.chatBody
			if (el) el.scrollTop = el.scrollHeight
		}
	}
}
</script>

<style>
.ai-chat-wrapper {
	position: fixed;
	right: 20px;
	bottom: 20px;
	z-index: 9999;
}

.ai-chat-btn {
	width: 56px;
	height: 56px;
	border-radius: 50%;
	background: #1b1c1d;
	color: #fff;
	display: flex;
	align-items: center;
	justify-content: center;
	cursor: pointer;
	box-shadow: 0 2px 12px rgba(0,0,0,.25);
	font-size: 22px;
	transition: background .2s;
}
.ai-chat-btn:hover { background: #27292a; }
.ai-chat-btn.active { background: #e03997; }

.ai-chat-panel {
	position: fixed;
	right: 20px;
	bottom: 90px;
	width: 360px;
	height: 480px;
	background: #fff;
	border-radius: 12px;
	box-shadow: 0 4px 24px rgba(0,0,0,.15);
	display: flex;
	flex-direction: column;
	overflow: hidden;
}

.ai-chat-header {
	background: #1b1c1d;
	color: #fff;
	padding: 12px 16px;
	display: flex;
	justify-content: space-between;
	align-items: center;
	font-size: 16px;
}
.ai-chat-header .close { cursor: pointer; }

.ai-chat-body {
	flex: 1;
	overflow-y: auto;
	padding: 12px;
	background: #f5f5f5;
}

.ai-chat-empty {
	text-align: center;
	color: #aaa;
	margin-top: 40px;
}

.ai-chat-bubble {
	max-width: 80%;
	padding: 10px 14px;
	border-radius: 16px;
	margin-bottom: 10px;
	word-break: break-word;
	font-size: 14px;
	line-height: 1.6;
}
.ai-chat-bubble.user {
	background: #e03997;
	color: #fff;
	margin-left: auto;
	border-bottom-right-radius: 4px;
}
.ai-chat-bubble.assistant {
	background: #fff;
	color: #333;
	margin-right: auto;
	border-bottom-left-radius: 4px;
	box-shadow: 0 1px 2px rgba(0,0,0,.08);
}

.ai-chat-footer {
	border-top: 1px solid #e0e0e0;
	padding: 8px 12px;
	display: flex;
	align-items: center;
	background: #fff;
}
.ai-chat-footer input {
	flex: 1;
	border: none;
	outline: none;
	padding: 8px;
	font-size: 14px;
}
.ai-chat-footer button {
	background: #e03997;
	color: #fff;
	border: none;
	padding: 8px 12px;
	border-radius: 8px;
	cursor: pointer;
	font-size: 14px;
}
.ai-chat-footer button:disabled {
	opacity: .5;
	cursor: default;
}

.slide-up-enter-active, .slide-up-leave-active {
	transition: all .25s ease;
}
.slide-up-enter, .slide-up-leave-to {
	opacity: 0;
	transform: translateY(12px);
}
</style>
