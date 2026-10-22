<template>
  <NightSky />
  <router-view v-slot="{ Component }">
    <transition name="fade" mode="out-in">
      <component :is="Component" />
    </transition>
  </router-view>
  <Toast />
</template>

<script setup>
import NightSky from './components/NightSky.vue'
import Toast from './components/Toast.vue'
</script>

<style>
.fade-enter-active, .fade-leave-active { transition: opacity 0.35s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
