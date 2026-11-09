// ============================================================
// 全局开关：Mock / 真实后端
// ------------------------------------------------------------
// 默认 true：页面走本地假数据，不启后端也能完整演示（答辩离线兜底）。
// 要连真后端联调时，用环境变量打开，不改代码：
//     macOS / Linux：VITE_USE_MOCK=false npm run dev
//     Windows PowerShell：$env:VITE_USE_MOCK="false"; npm run dev
//
// 注意：USE_MOCK=false 且后端没启动时，页面所有接口都会报错、列表为空，
// 看起来像"页面打不开"。联调前先确认 http://localhost:8090 能访问。
// ============================================================
export const USE_MOCK = (import.meta.env.VITE_USE_MOCK ?? 'true') !== 'false'
