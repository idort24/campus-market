<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import Home from './views/Home.vue'
import AiAssistant from './views/AiAssistant.vue'

const currentHash = ref(window.location.hash)

const view = computed(() => {
  if (currentHash.value.startsWith('#/ai')) return 'assistant'
  return 'home'
})

const onHashChange = () => {
  currentHash.value = window.location.hash
}

onMounted(() => window.addEventListener('hashchange', onHashChange))
onBeforeUnmount(() => window.removeEventListener('hashchange', onHashChange))
</script>

<template>
  <Home v-if="view === 'home'" />
  <AiAssistant v-else />
</template>
