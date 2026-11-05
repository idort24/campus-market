package com.campusmarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 【演示用】BLOB 存储文件表，仅用于方案对比，本项目业务不使用
 */
@Data
@TableName("demo_file_blob")
public class DemoFileBlob {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 文件名 */
    private String fileName;
    /** 文件 MIME 类型 */
    private String fileType;
    /** 文件二进制 BLOB 字段（业务不使用） */
    private byte[] fileContent;
    private LocalDateTime createTime;
}
