import http from './http'
export const getDictTypes = () => http.get('/dict-types')
export const getDictData = (typeCode) => http.get(`/dict-data/${typeCode}`)
