import { useAuthStore } from '../store/auth'

/** v-permission="'system:user:add'" 无该权限码则移除元素 */
export default {
  mounted(el, binding) {
    const store = useAuthStore()
    const code = binding.value
    if (code && !store.hasPerm(code)) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  }
}
