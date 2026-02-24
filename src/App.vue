<template>
  <button style="margin-top: env(safe-area-inset-top);" @click="taskPictureWrap">
    take picture
  </button>
  <div v-for="(time, index) in timeList" :key="index">
    {{ index }}th: {{ time.start }} ~ {{ time.end }}
  </div>
  <div>{{ error }}</div>
</template>

<script setup lang="ts">
import { invoke } from "@tauri-apps/api/core";
import { ref } from "vue";

interface PictureUri {
  imageUri: string;
}

const idx = ref(0);
const timeList = ref<{
  start: number;
  end: number | undefined;
}[]>([]);
const waiting = ref(false);
const error = ref("");

const takePicture = async () => {
  return invoke<PictureUri>("plugin:a|take_picture", { payload: {} });
};

const taskPictureWrap = async () => {
  const temp = idx.value;
  idx.value += 1;
  try {
    timeList.value.push({
      start: Date.now(),
      end: undefined,
    });
    const result = await takePicture();
    timeList.value[temp].end = Date.now();
  } catch (e: any) {
    error.value = e.message || e;
  } finally {
    waiting.value = false;
  }
};
</script>