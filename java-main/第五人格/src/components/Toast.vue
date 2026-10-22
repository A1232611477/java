<template>
  <transition name="toast-fade">
    <div v-if="visible" class="toast">{{ message }}</div>
  </transition>
</template>

<script setup>
import { useToast } from '../composables/useToast'
const { message, visible } = useToast()
</script>

<style scoped>
.toast-fade-enter-active, .toast-fade-leave-active { transition: opacity 0.3s, transform 0.3s; }
.toast-fade-enter-from, .toast-fade-leave-to { opacity: 0; transform: translateX(-50%) translateY(20px); }
</style>
