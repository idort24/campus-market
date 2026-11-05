<template>
  <div class="file-transfer-page">
    <!-- ==================== 顶部导航 ==================== -->
    <header class="navbar">
      <div class="logo">
        <span class="logo-icon">🛍️</span>
        <span>校园二手集市</span>
      </div>

      <nav class="nav-menu">
        <router-link to="/">首页</router-link>
        <router-link to="/product">商品市场</router-link>
        <router-link to="/publish">发布商品</router-link>
        <router-link to="/categories">分类管理</router-link>
        <router-link to="/ai">AI助手</router-link>
        <router-link to="/file-transfer" active-class="active">文件传输</router-link>
        <router-link to="/profile">个人中心</router-link>
      </nav>
    </header>

    <main class="container">
      <div class="page-title">
        <h1>📁 文件传输演示</h1>
        <p>大文件分片 · 批量上传 · 断点续传 · 进度条 · 高并发下载</p>
      </div>

      <el-row :gutter="16">
        <!-- ========== ① 大文件分片上传 ========== -->
        <el-col :xs="24" :md="12">
          <el-card class="func-card" shadow="hover">
            <template #header>
              <span class="card-title">① 大文件分片上传 + 坏块重传</span>
            </template>

            <el-upload
              class="chunk-upload"
              drag
              :auto-upload="false"
              :show-file-list="true"
              :limit="1"
              accept="*"
              :on-change="onChunkFileChange"
              :on-remove="() => (chunkFile = null)"
              :file-list="chunkFileList"
            >
              <div class="upload-tip">
                <el-icon size="40"><UploadFilled /></el-icon>
                <div>拖拽或点击选择大文件（将自动分片）</div>
              </div>
            </el-upload>

            <div class="field-row">
              <span>分片大小</span>
              <el-select v-model="chunkSize" size="small" style="width: 130px">
                <el-option :value="512 * 1024" label="512 KB" />
                <el-option :value="1024 * 1024" label="1 MB" />
                <el-option :value="2 * 1024 * 1024" label="2 MB" />
              </el-select>
            </div>

            <el-button type="primary" :loading="chunkUploading" @click="startChunkUpload" style="width: 100%">
              开始分片上传
            </el-button>

            <template v-if="chunkUploadId">
              <div class="meta-line">
                <span>uploadId: <code>{{ chunkUploadId }}</code></span>
                <span>片数: {{ totalChunks }}</span>
                <span>已传: {{ uploadedChunks.length }}</span>
              </div>
              <el-progress :percentage="chunkProgress" :stroke-width="16" :format="() => chunkProgress + '%'" />
            </template>

            <!-- 缺片失败响应 -->
            <el-alert
              v-if="missingInfo"
              type="error"
              :closable="false"
              show-icon
              class="alert"
              title="缺片响应 (code=1001)"
            >
              <template #default>
                <div>message: {{ missingInfo.msg }}</div>
                <div>missingChunks: [{{ missingInfo.missingChunks.join(', ') }}]</div>
              </template>
            </el-alert>

            <!-- 合并成功 -->
            <el-alert
              v-if="chunkResult"
              type="success"
              :closable="false"
              show-icon
              class="alert"
            >
              <template #default>
                <div>合并成功 fileId: <code>{{ chunkResult.fileId }}</code></div>
                <div>size: {{ chunkResult.size }} · MD5: <code>{{ chunkResult.md5 }}</code></div>
                <el-button size="small" type="success" plain @click="fillDownloadId(chunkResult.fileId)">
                  填入下载框 ↓
                </el-button>
              </template>
            </el-alert>
          </el-card>
        </el-col>

        <!-- ========== ② 批量上传 ========== -->
        <el-col :xs="24" :md="12">
          <el-card class="func-card" shadow="hover">
            <template #header>
              <span class="card-title">② 小文件批量上传</span>
            </template>

            <el-upload
              class="batch-upload"
              drag
              multiple
              :auto-upload="false"
              :show-file-list="true"
              :on-change="onBatchFileChange"
              :file-list="batchFileList"
              style="margin-bottom: 12px"
            >
              <div class="upload-tip">
                <el-icon size="40"><FolderOpened /></el-icon>
                <div>拖拽或点击选择多个小文件</div>
              </div>
            </el-upload>

            <el-button type="primary" :loading="batchUploading" @click="startBatchUpload" style="width: 100%">
              批量上传
            </el-button>

            <el-progress v-if="batchUploading" :percentage="batchProgress" :stroke-width="16" style="margin-top: 12px" />

            <el-table
              v-if="batchResults.length"
              :data="batchResults"
              size="small"
              max-height="240"
              style="margin-top: 12px; width: 100%"
            >
              <el-table-column prop="fileName" label="文件名" min-width="120" show-overflow-tooltip />
              <el-table-column label="结果" width="90">
                <template #default="{ row }">
                  <el-tag v-if="row.success" type="success">✓ {{ row.size }}B</el-tag>
                  <el-tag v-else type="danger">✗ {{ row.msg }}</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>

        <!-- ========== ③ 断点续传 ========== -->
        <el-col :xs="24" :md="12">
          <el-card class="func-card" shadow="hover">
            <template #header>
              <span class="card-title">③ 断点续传（查已传片 → 补缺失片）</span>
            </template>

            <el-upload
              class="resume-upload"
              drag
              :auto-upload="false"
              :show-file-list="true"
              :limit="1"
              accept="*"
              :on-change="onResumeFileChange"
              :on-remove="() => (resumeFile = null)"
              :file-list="resumeFileList"
            >
              <div class="upload-tip">
                <el-icon size="40"><Refresh /></el-icon>
                <div>选择文件，查询已传分片并只补缺失片</div>
              </div>
            </el-upload>

            <div class="field-row" style="margin-top: 10px">
              <span>中断会话 uploadId（可选）</span>
              <el-input
                v-model="resumeUploadIdInput"
                placeholder="填写已中断的 uploadId 实现续传；留空则新建"
                size="small"
                clearable
              />
            </div>

            <el-button type="primary" :loading="resumeUploading" @click="startResumeUpload" style="width: 100%">
              断点续传
            </el-button>

            <template v-if="resumeUploadId">
              <div class="meta-line">
                <span>uploadId: <code>{{ resumeUploadId }}</code></span>
                <span>已传: {{ resumeUploaded.length }}/{{ resumeTotalChunks }}</span>
              </div>
              <el-progress :percentage="resumeProgress" :stroke-width="16" />
            </template>

            <el-alert
              v-if="resumeResult"
              type="success"
              :closable="false"
              show-icon
              class="alert"
            >
              <template #default>
                <div>续传完成 fileId: <code>{{ resumeResult.fileId }}</code></div>
                <el-button size="small" type="success" plain @click="fillDownloadId(resumeResult.fileId)">
                  填入下载框 ↓
                </el-button>
              </template>
            </el-alert>
          </el-card>
        </el-col>

        <!-- ========== ④ 下载（进度条 / 并发） ========== -->
        <el-col :xs="24" :md="12">
          <el-card class="func-card" shadow="hover">
            <template #header>
              <span class="card-title">④ 下载进度条 + 高并发下载（Range 分段）</span>
            </template>

            <div class="field-row">
              <span>fileId</span>
              <el-input v-model="downloadFileId" placeholder="粘贴上方 fileId" size="small" clearable />
            </div>

            <el-space wrap>
              <el-button type="primary" :loading="dlDownloading" @click="startSingleDownload">
                单线程下载（进度）
              </el-button>
              <el-button type="warning" :loading="dlDownloading" @click="startConcurrentDownload">
                并发下载（{{ CONCURRENCY }} 连接）
              </el-button>
              <el-button v-if="dlBlobUrl" type="success" plain @click="saveDownload">保存文件</el-button>
            </el-space>

            <el-progress
              v-if="dlDownloading"
              :percentage="dlProgress"
              :stroke-width="16"
              style="margin-top: 12px"
            />
            <div class="part-log" v-if="dlDownloading && dlPartLog.length">
              <span v-for="(t, i) in dlPartLog" :key="i">{{ t }}&nbsp;</span>
            </div>

            <div v-if="dlSize" class="meta-line" style="margin-top: 8px">
              <span>文件大小: {{ (dlSize / 1024 / 1024).toFixed(2) }} MB</span>
              <span>分段: {{ dlParts }} 段</span>
            </div>

            <el-alert v-if="dlError" type="error" :closable="false" show-icon class="alert" :title="dlError" />
          </el-card>
        </el-col>
      </el-row>

      <!-- ========== 运行日志 ========== -->
      <el-card class="func-card log-card" shadow="hover" style="margin-top: 16px">
        <template #header>
          <span class="card-title">运行日志（含请求 / 失败响应）</span>
        </template>
        <div class="log-panel">
          <div
            v-for="(l, i) in logs"
            :key="i"
            class="log-line"
            :class="'log-' + l.type"
          >
            <span class="log-time">{{ l.time }}</span> {{ l.msg }}
          </div>
          <div v-if="logs.length === 0" class="log-empty">等待操作……</div>
        </div>
      </el-card>
    </main>

    <footer>© 2026 校园二手集市 · 让闲置物品流动起来</footer>
  </div>
</template>

<script setup>
import { ref, reactive, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled, FolderOpened, Refresh } from '@element-plus/icons-vue'
import {
  initUpload,
  uploadChunk,
  getUploadedChunks,
  mergeUpload,
  batchUpload,
  downloadRange,
  downloadFull
} from '../api/file'

// ================= 全局日志 =================
const logs = ref([])
function log(msg, type = 'info') {
  logs.value.push({ msg, type, time: new Date().toLocaleTimeString() })
}

// ================= ① 大文件分片上传 =================
const chunkFile = ref(null)
const chunkFileList = ref([])
const chunkSize = ref(1024 * 1024)
const chunkUploading = ref(false)
const chunkUploadId = ref('')
const totalChunks = ref(0)
const totalBytes = ref(0)
const uploadedChunks = ref([])
const chunkProgress = ref(0)
const missingInfo = ref(null)
const chunkResult = ref(null)

function onChunkFileChange(file) {
  if (file.raw) {
    chunkFile.value = file.raw
    chunkFileList.value = [file]
    chunkResult.value = null
    missingInfo.value = null
  }
}

async function startChunkUpload() {
  const file = chunkFile.value
  if (!file) {
    ElMessage.warning('请先选择一个大文件')
    return
  }
  chunkUploading.value = true
  chunkResult.value = null
  missingInfo.value = null
  uploadedChunks.value = []
  chunkProgress.value = 0
  totalBytes.value = file.size

  const total = file.size
  const cs = chunkSize.value
  const tc = Math.ceil(total / cs)
  totalChunks.value = tc

  log(`▶ 分片上传：${file.name} (${(total / 1024 / 1024).toFixed(2)} MB)，${tc} 片 × ${cs}B`)

  // 1. init
  try {
    const init = await initUpload({ fileName: file.name, fileSize: total, chunkSize: cs, totalChunks: tc })
    chunkUploadId.value = init.uploadId
    log(`① init → uploadId = ${init.uploadId}`, 'success')
  } catch (e) {
    chunkUploading.value = false
    ElMessage.error(`init 失败：${e.message}`)
    log(`① init 失败：${e.message}`, 'error')
    return
  }

  // 2. 逐片上传
  for (let i = 0; i < tc; i++) {
    const start = i * cs
    const end = Math.min((i + 1) * cs, total)
    try {
      await uploadChunk(chunkUploadId.value, i, new File([file.slice(start, end)], `chunk-${i}`))
      uploadedChunks.value.push(i)
      chunkProgress.value = Math.round(((i + 1) / tc) * 100)
      log(`  分片 ${i}/${tc - 1} 成功`, 'success')
    } catch (e) {
      chunkUploading.value = false
      chunkProgress.value = Math.round((i / tc) * 100)
      log(`  分片 ${i} 失败：${e.message}`, 'error')
      ElMessage.error(`分片 ${i} 上传失败（坏块/网络）`)
      return
    }
  }

  await doMerge()
}

async function doMerge() {
  try {
    const data = await mergeUpload(chunkUploadId.value)
    chunkResult.value = data
    chunkUploading.value = false
    log(`② merge 成功 → fileId=${data.fileId}, md5=${data.md5}`, 'success')
  } catch (e) {
    chunkUploading.value = false
    if (e.code === 1001) {
      missingInfo.value = { msg: e.message, missingChunks: e.body?.data?.missingChunks || [] }
      log(`② merge 缺片 → code=1001, missingChunks=[${(e.body?.data?.missingChunks || []).join(',')}]`, 'error')
      // 自动补传缺失片
      await retryMissing(missingInfo.value.missingChunks)
    } else {
      ElMessage.error(`merge 失败：${e.message}`)
      log(`② merge 失败：${e.message}`, 'error')
    }
  }
}

async function retryMissing(missingChunks) {
  if (!missingChunks || missingChunks.length === 0) return
  const file = chunkFile.value
  const cs = chunkSize.value
  log(`③ 缺片重传：只重传 ${missingChunks.join(', ')}`, 'warn')
  for (const idx of missingChunks) {
    const start = idx * cs
    const end = Math.min((idx + 1) * cs, totalBytes.value)
    try {
      await uploadChunk(chunkUploadId.value, idx, new File([file.slice(start, end)], `chunk-${idx}`))
      uploadedChunks.value.push(idx)
      log(`  补传分片 ${idx} 成功`, 'success')
    } catch (e) {
      log(`  补传分片 ${idx} 失败：${e.message}`, 'error')
      return
    }
  }
  await doMerge()
}

// ================= ② 批量上传 =================
const batchFileList = ref([])
const batchFiles = ref([])
const batchUploading = ref(false)
const batchProgress = ref(0)
const batchResults = ref([])

function onBatchFileChange(file, fileList) {
  batchFileList.value = fileList
  batchFiles.value = fileList.map((f) => f.raw)
  batchResults.value = []
}

async function startBatchUpload() {
  if (batchFiles.value.length === 0) {
    ElMessage.warning('请先选择小文件')
    return
  }
  batchUploading.value = true
  batchResults.value = []
  batchProgress.value = 0
  log(`▶ 批量上传 ${batchFiles.value.length} 个文件`)
  try {
    const data = await batchUpload(batchFiles.value, (loaded, total) => {
      batchProgress.value = Math.round((loaded / total) * 100)
    })
    batchResults.value = data
    const ok = data.filter((x) => x.success).length
    log(`批量上传完成：成功 ${ok}/${data.length}`, 'success')
  } catch (e) {
    ElMessage.error(`批量上传失败：${e.message}`)
    log(`批量上传失败：${e.message}`, 'error')
  }
  batchUploading.value = false
}

// ================= ③ 断点续传 =================
const resumeFile = ref(null)
const resumeFileList = ref([])
const resumeUploading = ref(false)
const resumeUploadId = ref('')
const resumeUploadIdInput = ref('')
const resumeTotalChunks = ref(0)
const resumeUploaded = ref([])
const resumeProgress = ref(0)
const resumeResult = ref(null)

function onResumeFileChange(file) {
  if (file.raw) {
    resumeFile.value = file.raw
    resumeFileList.value = [file]
    resumeResult.value = null
  }
}

async function startResumeUpload() {
  const file = resumeFile.value
  if (!file) {
    ElMessage.warning('请选择一个文件')
    return
  }
  resumeUploading.value = true
  resumeResult.value = null
  resumeUploaded.value = []
  resumeProgress.value = 0

  const cs = 1024 * 1024
  const tc = Math.ceil(file.size / cs)
  resumeTotalChunks.value = tc

  try {
    let uploadId = resumeUploadIdInput.value.trim()

    if (uploadId) {
      // 基于已有（中断的）会话续传：直接查已传分片
      log(`① 使用已中断会话 uploadId = ${uploadId}`)
      const uploaded = await getUploadedChunks(uploadId)
      resumeUploadId.value = uploadId
      resumeUploaded.value = uploaded
      log(`② 已传分片：${uploaded.length ? uploaded.join(', ') : '无（全部需重传）'}`, 'info')
    } else {
      // 新建会话（无中断记录时从零开始）
      const init = await initUpload({ fileName: file.name, fileSize: file.size, chunkSize: cs, totalChunks: tc })
      uploadId = init.uploadId
      resumeUploadId.value = uploadId
      log(`① init → uploadId = ${uploadId}`)
      resumeUploaded.value = []
      log(`② 新会话，无已传分片`, 'info')
    }

    // 补缺失片
    const set = new Set(resumeUploaded.value)
    const missing = []
    for (let i = 0; i < tc; i++) if (!set.has(i)) missing.push(i)
    log(`③ 缺失片：${missing.length ? missing.join(', ') : '无，直接合并'}`, 'info')

    for (let i = 0; i < tc; i++) {
      if (set.has(i)) continue
      const start = i * cs
      const end = Math.min((i + 1) * cs, file.size)
      try {
        await uploadChunk(resumeUploadId.value, i, new File([file.slice(start, end)], `chunk-${i}`))
        resumeUploaded.value.push(i)
        resumeProgress.value = Math.round(((i + 1) / tc) * 100)
        log(`  续传分片 ${i} 成功`, 'success')
      } catch (e) {
        resumeUploading.value = false
        log(`  续传分片 ${i} 失败：${e.message}`, 'error')
        ElMessage.error(`分片 ${i} 失败：${e.message}`)
        return
      }
    }

    const data = await mergeUpload(resumeUploadId.value)
    resumeResult.value = data
    log(`断点续传完成 → fileId=${data.fileId}, md5=${data.md5}`, 'success')
  } catch (e) {
    resumeUploading.value = false
    log(`断点续传失败：${e.message}`, 'error')
    ElMessage.error(e.message)
    return
  }
  resumeUploading.value = false
}

// ================= ④ 下载 =================
const downloadFileId = ref('')
const dlDownloading = ref(false)
const dlProgress = ref(0)
const dlPartLog = ref([])
const dlSize = ref(0)
const dlParts = ref(0)
const dlBlobUrl = ref('')
const dlBlob = ref(null)
const dlError = ref('')
const CONCURRENCY = 4

function fillDownloadId(id) {
  if (id) {
    downloadFileId.value = id
    log(`已填入 fileId：${id}`)
    ElMessage.success('已填入 fileId')
  }
}

// 探测文件大小
async function probeSize(fileId) {
  const probe = await downloadRange(fileId, 0, 0)
  const m = probe.contentRange && probe.contentRange.match(/\/(\d+)\s*$/)
  return m ? parseInt(m[1]) : 0
}

// 单线程完整下载（进度条）
async function startSingleDownload() {
  const fileId = downloadFileId.value.trim()
  if (!fileId) {
    ElMessage.warning('请填写 fileId')
    return
  }
  dlDownloading.value = true
  dlError.value = ''
  dlProgress.value = 0
  dlBlobUrl.value = ''
  log(`▶ 单线程下载 fileId=${fileId}`)
  try {
    const size = await probeSize(fileId)
    dlSize.value = size
    const { blob } = await downloadFull(fileId, (loaded, total) => {
      dlProgress.value = Math.round((loaded / total) * 100)
    })
    dlBlob.value = blob
    dlBlobUrl.value = URL.createObjectURL(blob)
    dlProgress.value = 100
    log(`下载完成：${blob.size} 字节`, 'success')
  } catch (e) {
    dlError.value = e.message
    log(`下载失败：${e.message}`, 'error')
  }
  dlDownloading.value = false
}

// 并发下载（Range 分段 + 进度）
async function startConcurrentDownload() {
  const fileId = downloadFileId.value.trim()
  if (!fileId) {
    ElMessage.warning('请填写 fileId')
    return
  }
  dlDownloading.value = true
  dlError.value = ''
  dlProgress.value = 0
  dlBlobUrl.value = ''
  dlPartLog.value = []
  log(`▶ 并发下载 fileId=${fileId}，并发数=${CONCURRENCY}`)

  try {
    const total = await probeSize(fileId)
    if (!total) throw new Error('无法获取文件大小')
    dlSize.value = total

    const totalParts = Math.min(CONCURRENCY, total)
    dlParts.value = totalParts
    const partSize = Math.ceil(total / totalParts)
    const downloadedBytes = new Array(totalParts).fill(0)

    const promises = []
    for (let p = 0; p < totalParts; p++) {
      const start = p * partSize
      const end = Math.min(start + partSize - 1, total - 1)
      promises.push(
        downloadRange(fileId, start, end, (loaded) => {
          downloadedBytes[p] = loaded
          const sum = downloadedBytes.reduce((a, b) => a + b, 0)
          dlProgress.value = Math.round((sum / total) * 100)
          dlPartLog.value[p] = `段${p}: ${(loaded / 1024).toFixed(0)}KB`
        }).then(({ blob }) => {
          log(`段 ${p} 完成 (bytes ${start}-${end})`, 'success')
          return { p, blob, start }
        })
      )
    }

    const parts = await Promise.all(promises)
    parts.sort((a, b) => a.start - b.start)
    const full = new Blob(parts.map((x) => x.blob), { type: 'application/octet-stream' })
    dlBlob.value = full
    dlBlobUrl.value = URL.createObjectURL(full)
    dlProgress.value = 100
    log(`并发下载完成：${parts.length} 段拼合，共 ${full.size} 字节`, 'success')
  } catch (e) {
    dlError.value = e.message
    log(`并发下载失败：${e.message}`, 'error')
  }
  dlDownloading.value = false
}

function saveDownload() {
  if (!dlBlob.value) return
  const a = document.createElement('a')
  a.href = dlBlobUrl.value
  a.download = `concurrent-${downloadFileId.value}.bin`
  a.click()
  log('已保存文件到本地', 'success')
}

// 组件卸载时释放 Blob URL
onUnmounted(() => {
  if (dlBlobUrl.value) URL.revokeObjectURL(dlBlobUrl.value)
})
</script>

<style scoped>
/* 整体 */
.file-transfer-page { min-height: 100vh; background: #f7f8fc; color: #222; }
.container { max-width: 1200px; margin: 0 auto; padding: 30px 24px 40px; box-sizing: border-box; }

/* 顶部导航（与主项目一致） */
.navbar {
  height: 70px; background: white;
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 10%; border-bottom: 1px solid #eee; box-sizing: border-box;
}
.logo { display: flex; align-items: center; gap: 10px; font-size: 22px; font-weight: 700; white-space: nowrap; }
.logo-icon { font-size: 28px; }
.nav-menu { display: flex; align-items: center; gap: 35px; }
.nav-menu a { color: #666; text-decoration: none; cursor: pointer; font-size: 16px; transition: 0.2s; }
.nav-menu a:hover { color: #6c5ce7; }
.nav-menu a.active { color: #6c5ce7; font-weight: 700; }

/* 页面标题 */
.page-title { text-align: center; margin-bottom: 24px; }
.page-title h1 { margin: 0; font-size: 30px; color: #6c5ce7; }
.page-title p { color: #777; margin-top: 8px; }

/* 功能卡片 */
.func-card { border-radius: 12px; margin-bottom: 16px; }
.card-title { font-weight: 700; color: #6c5ce7; }
.upload-tip { padding: 12px 0; text-align: center; }
.upload-tip div { margin-top: 8px; color: #888; font-size: 13px; }

.field-row { display: flex; align-items: center; gap: 8px; margin: 12px 0; }
.field-row > span:first-child { color: #555; font-size: 13px; flex-shrink: 0; }

.meta-line { display: flex; gap: 12px; flex-wrap: wrap; font-size: 12px; color: #666; margin: 10px 0; }
.meta-line code { background: #f1f0ff; padding: 1px 5px; border-radius: 4px; color: #6c5ce7; }

.alert { margin-top: 12px; }
.part-log { font-size: 12px; color: #888; margin-top: 8px; font-family: Consolas, monospace; }

/* 日志面板 */
.log-panel {
  background: #1e1e2e; border-radius: 8px; padding: 12px;
  height: 240px; overflow-y: auto; font-family: Consolas, monospace; font-size: 13px;
}
.log-line { padding: 2px 0; border-bottom: 1px dashed #2a2a3c; }
.log-time { color: #666; margin-right: 8px; }
.log-info { color: #cdd6f4; }
.log-success { color: #a6e3a1; }
.log-error { color: #f38ba8; }
.log-warn { color: #f9e2af; }
.log-empty { color: #666; }

footer { background: white; padding: 30px; text-align: center; color: #888; border-top: 1px solid #eee; }

@media (max-width: 1000px) {
  .navbar { padding: 0 5%; }
  .nav-menu { gap: 15px; }
}
</style>
