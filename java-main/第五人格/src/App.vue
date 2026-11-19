<template>
  <router-view v-slot="{ Component }">
    <transition name="fade" mode="out-in">
      <component :is="Component" />
    </transition>
  </router-view>
  <Toast />
  <GomokuInviteListener />
  <EscapeInviteListener />
</template>

<script setup>
import Toast from './components/Toast.vue'
import GomokuInviteListener from './components/GomokuInviteListener.vue'
import EscapeInviteListener from './components/EscapeInviteListener.vue'
</script>

<style>
.fade-enter-active, .fade-leave-active { transition: opacity 0.35s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
