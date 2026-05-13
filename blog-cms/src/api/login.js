import axios from '@/util/request'
import rawAxios from 'axios'

export function login(loginForm) {
	return axios({
		url: 'login',
		method: 'POST',
		data: {
			...loginForm,
			captchaId: loginForm.captchaId,
			captchaCode: loginForm.captchaCode
		}
	})
}

export function getCaptcha() {
	return rawAxios({
		url: 'http://localhost:8090/captcha',
		method: 'GET'
	}).then(res => res.data.data)
}
