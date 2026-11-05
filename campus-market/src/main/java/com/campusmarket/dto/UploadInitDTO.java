package com.campusmarket.dto;

import lombok.Data;

/**
 * 初始化分片上传请求体
 */
@Data
public class UploadInitDTO {

    /** 文件名 */
    private String fileName;

    /** 文件总大小（字节） */
    private Long fileSize;

    /** 每片大小（字节），前端 slice 用 */
    private Integer chunkSize;

    /** 总分片数 */
    private Integer totalChunks;

    /** 整文件 MD5（可选） */
    private String fileMd5;
}
