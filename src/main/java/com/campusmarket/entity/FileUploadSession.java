package com.campusmarket.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分片上传会话（内存态，仅用于本次演示）。
 * 生产环境一般存数据库，这里为了讲清楚分片流程，用内存 Map 保存。
 */
@Data
public class FileUploadSession {

    /** 本次上传的唯一标识，前端后续每片上传、合并都靠它 */
    private String uploadId;

    /** 原始文件名 */
    private String fileName;

    /** 文件总大小（字节） */
    private long fileSize;

    /** 每片大小（字节），由前端切分时约定 */
    private int chunkSize;

    /** 总共分了多少片 */
    private int totalChunks;

    /** 整文件 MD5（可选，合并后做完整性校验） */
    private String fileMd5;

    private LocalDateTime createTime;
}
