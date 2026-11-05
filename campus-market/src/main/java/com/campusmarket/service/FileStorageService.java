package com.campusmarket.service;

import com.campusmarket.entity.FileUploadSession;
import com.campusmarket.entity.StoredFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 文件存储服务：分片上传、合并、批量上传、断点续传、Range 下载的数据层
 */
public interface FileStorageService {

    /**
     * 1. 初始化分片上传会话，返回 uploadId
     */
    FileUploadSession initUpload(String fileName, long fileSize, int chunkSize, int totalChunks, String fileMd5);

    /**
     * 2. 保存单个分片。
     *    若传了 chunkMd5，会做分块校验，不一致说明该块损坏，抛异常通知前端重传。
     */
    void saveChunk(String uploadId, int chunkIndex, MultipartFile file, String chunkMd5) throws IOException;

    /**
     * 3. 查询某个 uploadId 已成功上传的分片下标（断点续传/缺片重传依据）
     */
    List<Integer> listUploadedChunks(String uploadId);

    /**
     * 4. 把分片按顺序合并成完整文件。缺片时抛 MissingChunksException，携带缺失片下标。
     */
    StoredFile merge(String uploadId) throws IOException;

    /**
     * 5. 小文件批量上传：一次请求保存多个文件
     */
    List<BatchResultItem> saveSmallFiles(MultipartFile[] files) throws IOException;

    /**
     * 6. 按 fileId 获取已存储文件
     */
    StoredFile getFile(String fileId);

    /**
     * 批量上传里单个文件的结果
     */
    class BatchResultItem {
        private String fileName;
        private boolean success;
        private String fileId;
        private Long size;
        private String msg;

        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getFileId() { return fileId; }
        public void setFileId(String fileId) { this.fileId = fileId; }
        public Long getSize() { return size; }
        public void setSize(Long size) { this.size = size; }
        public String getMsg() { return msg; }
        public void setMsg(String msg) { this.msg = msg; }
    }
}
