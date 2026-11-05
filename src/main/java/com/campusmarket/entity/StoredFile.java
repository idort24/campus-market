package com.campusmarket.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 已成功合并/上传的文件记录（内存态，演示用）。
 * 记录了文件在磁盘上的位置，供下载接口使用。
 */
@Data
public class StoredFile {

    /** 下载用唯一标识 */
    private String fileId;

    /** 展示用文件名（下载时作为 attachment 的文件名） */
    private String fileName;

    /** 文件大小（字节） */
    private long size;

    /** 文件在磁盘上的绝对路径 */
    private String path;

    /** 文件 MD5 */
    private String md5;

    private LocalDateTime createTime;
}
