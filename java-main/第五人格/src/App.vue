<template>
  <router-view v-slot="{ Component }">
    <transition name="fade" mode="out-in">
      <component :is="Component" />
    </transition>
  </router-view>
  <Toast />
  <GomokuInviteListener />
</template>

<script setup>
import Toast from './components/Toast.vue'
import GomokuInviteListener from './components/GomokuInviteListener.vue'
</script>

<style>
.fade-enter-active, .fade-leave-active { transition: opacity 0.35s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
