// ============================================================
// 文件传输 - 后端接口封装
// 对接 Spring Boot 后端（端口 8090，前缀 /api/v1/file）
// 通过 Vite 代理 /api -> localhost:8090
// 接口已在后端 FileController.java 核对（非猜测）
// ============================================================

const BASE = '/api/v1/file'

// 后端统一返回结构 R<T> = { code, msg, data }
// 成功 code=200（R.ok）；业务错误 code=1001（缺片）；HTTP 层失败走非 2xx

/** 通用请求：解析 R 结构，业务失败抛异常 */
async function request(url, options) {
  const res = await fetch(url, options)
  // 下载等非 JSON 响应由调用方自行处理
  const text = await res.text()
  let json = null
  try { json = text ? JSON.parse(text) : null } catch { json = null }
  if (!res.ok) {
    throw new Error(json?.msg || `HTTP ${res.status}`)
  }
  if (json && json.code !== undefined && json.code !== 1) {
    // 业务错误（如缺片 code=1001），返回给调用方判断
    const err = new Error(json.msg || '请求失败')
    err.code = json.code
    err.body = json
    throw err
  }
  return json?.data
}

/** 带进度条的表单 POST（上传进度用 xhr.upload.onprogress） */
function postFormWithProgress(url, formData, onProgress) {
  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest()
    xhr.open('POST', url)
    xhr.responseType = 'json'
    xhr.onload = () => {
      const r = xhr.response
      if (xhr.status >= 200 && xhr.status < 300 && r) {
        if (r.code !== undefined && r.code !== 1) {
          const err = new Error(r.msg || '请求失败')
          err.code = r.code
          err.body = r
          reject(err)
          return
        }
        resolve(r.data)
      } else {
        reject(new Error((r && r.msg) || `HTTP ${xhr.status}`))
      }
    }
    xhr.onerror = () => reject(new Error('网络错误'))
    if (onProgress) {
      xhr.upload.onprogress = (e) => {
        if (e.lengthComputable) onProgress(e.loaded, e.total)
      }
    }
    xhr.send(formData)
  })
}

// ================= 大文件分片上传 =================

/** 1. 初始化分片上传 */
export function initUpload({ fileName, fileSize, chunkSize, totalChunks, fileMd5 }) {
  return request(`${BASE}/upload/init`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ fileName, fileSize, chunkSize, totalChunks, fileMd5 })
  })
}

/** 2. 上传单个分片 */
export function uploadChunk(uploadId, chunkIndex, file, chunkMd5, onProgress) {
  const fd = new FormData()
  fd.append('uploadId', uploadId)
  fd.append('chunkIndex', chunkIndex)
  if (chunkMd5) fd.append('chunkMd5', chunkMd5)
  fd.append('file', file)
  return postFormWithProgress(`${BASE}/upload/chunk`, fd, onProgress)
}

/** 3. 查询已上传的分片下标 */
export function getUploadedChunks(uploadId) {
  return request(`${BASE}/upload/${uploadId}/chunks`)
}

/** 4. 合并分片；缺片时抛 code=1001 异常（err.body.data.missingChunks） */
export function mergeUpload(uploadId) {
  return request(`${BASE}/upload/merge?uploadId=${uploadId}`, { method: 'POST' })
}

// ================= 批量上传 =================

/** 5. 小文件批量上传 */
export function batchUpload(files, onProgress) {
  const fd = new FormData()
  files.forEach((f) => fd.append('files', f))
  return postFormWithProgress(`${BASE}/upload/batch`, fd, onProgress)
}

// ================= 下载（Range / 并发 / 进度） =================

/** 6. Range 下载单个区间，返回 { blob, status, contentRange } */
export function downloadRange(fileId, start, end, onProgress) {
  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest()
    xhr.open('GET', `${BASE}/download/${fileId}`)
    xhr.setRequestHeader('Range', `bytes=${start}-${end}`)
    xhr.responseType = 'blob'
    xhr.onload = () => {
      if (xhr.status === 206 || xhr.status === 200) {
        resolve({
          blob: xhr.response,
          status: xhr.status,
          contentRange: xhr.getResponseHeader('Content-Range'),
          contentLength: Number(xhr.getResponseHeader('Content-Length') || 0)
        })
      } else if (xhr.status === 416) {
        reject(new Error(`416 范围无法满足 (${xhr.getResponseHeader('Content-Range')})`))
      } else {
        reject(new Error(`HTTP ${xhr.status}`))
      }
    }
    xhr.onerror = () => reject(new Error('网络错误'))
    if (onProgress) {
      xhr.onprogress = (e) => {
        if (e.lengthComputable) onProgress(e.loaded, e.total)
      }
    }
    xhr.send()
  })
}

/** 完整下载（无 Range，200） */
export function downloadFull(fileId, onProgress) {
  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest()
    xhr.open('GET', `${BASE}/download/${fileId}`)
    xhr.responseType = 'blob'
    xhr.onload = () => {
      if (xhr.status === 200 || xhr.status === 206) {
        resolve({ blob: xhr.response, status: xhr.status })
      } else if (xhr.status === 416) {
        reject(new Error(`416 范围无法满足 (${xhr.getResponseHeader('Content-Range')})`))
      } else {
        reject(new Error(`HTTP ${xhr.status}`))
      }
    }
    xhr.onerror = () => reject(new Error('网络错误'))
    if (onProgress) {
      xhr.onprogress = (e) => {
        if (e.lengthComputable) onProgress(e.loaded, e.total)
      }
    }
    xhr.send()
  })
}

export default {
  BASE,
  initUpload,
  uploadChunk,
  getUploadedChunks,
  mergeUpload,
  batchUpload,
  downloadRange,
  downloadFull
}
