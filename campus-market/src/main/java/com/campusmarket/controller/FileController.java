
package com.campusmarket.controller;

import com.campusmarket.common.R;
import com.campusmarket.dto.UploadInitDTO;
import com.campusmarket.entity.FileUploadSession;
import com.campusmarket.entity.StoredFile;
import com.campusmarket.service.FileStorageService;
import com.campusmarket.service.impl.FileStorageServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件上传 / 下载 Controller
 *
 * 功能：
 * 1. 大文件分片上传
 * 2. 分片损坏检测及指定分片重传
 * 3. 小文件批量上传
 * 4. 上传断点续传
 * 5. 下载断点续传
 * 6. HTTP Range 流式下载
 */
@RestController
@RequestMapping("/api/v1/file")
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 1. 初始化分片上传
     *
     * POST /api/v1/file/upload/init
     */
    @PostMapping("/upload/init")
    public R<Map<String, Object>> initUpload(
            @RequestBody UploadInitDTO dto) {

        FileUploadSession session = fileStorageService.initUpload(
                dto.getFileName(),
                dto.getFileSize(),
                dto.getChunkSize(),
                dto.getTotalChunks(),
                dto.getFileMd5()
        );

        Map<String, Object> data = new HashMap<>();
        data.put("uploadId", session.getUploadId());
        data.put("chunkSize", session.getChunkSize());
        data.put("totalChunks", session.getTotalChunks());

        return R.ok(data);
    }

    /**
     * 2. 上传单个分片
     *
     * POST /api/v1/file/upload/chunk
     *
     * 正常上传：返回上传成功。
     * 分片损坏：返回 missingChunks，通知前端只重传损坏分片。
     */
    @PostMapping("/upload/chunk")
    public R<Map<String, Object>> uploadChunk(
            @RequestParam("uploadId") String uploadId,
            @RequestParam("chunkIndex") int chunkIndex,
            @RequestParam(value = "chunkMd5", required = false) String chunkMd5,
            @RequestPart("file") MultipartFile file) throws IOException {

        try {
            // 保存分片，并由 Service 层进行 MD5 校验
            fileStorageService.saveChunk(
                    uploadId,
                    chunkIndex,
                    file,
                    chunkMd5
            );

            // 上传成功
            Map<String, Object> data = new HashMap<>();
            data.put("success", true);
            data.put("chunkIndex", chunkIndex);
            data.put("message", "第" + chunkIndex + "片上传成功");

            return R.ok(data);

        } catch (FileStorageServiceImpl.ChunkIntegrityException e) {

            // 捕获损坏分片异常
            Map<String, Object> data = new HashMap<>();

            data.put("success", false);
            data.put("chunkIndex", e.getChunkIndex());

            // 关键：明确告诉前端需要重新上传的分片下标
            data.put(
                    "missingChunks",
                    Collections.singletonList(e.getChunkIndex())
            );

            R<Map<String, Object>> result =
                    R.error(
                            1002,
                            "第" + e.getChunkIndex() + "片校验失败，请重新上传该分片"
                    );

            result.setData(data);

            return result;
        }
    }

    /**
     * 3. 查询已经上传的分片
     *
     * GET /api/v1/file/upload/{uploadId}/chunks
     *
     * 用于断点续传和缺失分片查询。
     */
    @GetMapping("/upload/{uploadId}/chunks")
    public R<List<Integer>> uploadedChunks(
            @PathVariable("uploadId") String uploadId) {

        return R.ok(fileStorageService.listUploadedChunks(uploadId));
    }

    /**
     * 4. 合并分片为完整文件
     *
     * POST /api/v1/file/upload/merge?uploadId=xxx
     *
     * 如果存在缺失分片，返回 missingChunks。
     */
    @PostMapping("/upload/merge")
    public R<Map<String, Object>> merge(
            @RequestParam("uploadId") String uploadId) {

        try {
            StoredFile stored = fileStorageService.merge(uploadId);

            return R.ok(toFileVO(stored));

        } catch (FileStorageServiceImpl.MissingChunksException e) {

            // 存在缺失分片，通知前端补传
            Map<String, Object> data = new HashMap<>();
            data.put("missingChunks", e.getMissingChunks());

            R<Map<String, Object>> result =
                    R.error(1001, "存在缺失分片，请重传");

            result.setData(data);

            return result;

        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
    }

    /**
     * 5. 小文件批量上传
     *
     * POST /api/v1/file/upload/batch
     */
    @PostMapping("/upload/batch")
    public R<List<Map<String, Object>>> batchUpload(
            @RequestParam("files") MultipartFile[] files) throws IOException {

        List<FileStorageService.BatchResultItem> items =
                fileStorageService.saveSmallFiles(files);

        List<Map<String, Object>> result = new ArrayList<>();

        for (FileStorageService.BatchResultItem it : items) {

            Map<String, Object> m = new HashMap<>();

            m.put("fileName", it.getFileName());
            m.put("success", it.isSuccess());
            m.put("msg", it.getMsg());

            if (it.getFileId() != null) {
                m.put("fileId", it.getFileId());
                m.put("size", it.getSize());
                m.put(
                        "downloadUrl",
                        "/api/v1/file/download/" + it.getFileId()
                );
            }

            result.add(m);
        }

        return R.ok(result);
    }

    /**
     * 6. 下载文件
     *
     * GET /api/v1/file/download/{fileId}
     *
     * 支持：
     * - 普通完整下载：200
     * - Range 分段下载：206
     * - 不合法范围：416
     */
    @GetMapping("/download/{fileId}")
    public ResponseEntity<StreamingResponseBody> download(
            @PathVariable("fileId") String fileId,
            @RequestHeader(
                    value = HttpHeaders.RANGE,
                    required = false
            ) String rangeHeader) throws IOException {

        StoredFile stored = fileStorageService.getFile(fileId);

        if (stored == null) {
            return ResponseEntity.notFound().build();
        }

        Path path = Paths.get(stored.getPath());

        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }

        long total = Files.size(path);

        MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;

        String disposition =
                "attachment; filename=\"" + stored.getFileName() + "\"";

        // 没有 Range 请求头，返回完整文件
        if (rangeHeader == null) {
            return fullResponse(
                    path,
                    0,
                    total,
                    total,
                    contentType,
                    disposition
            );
        }

        // 解析 Range 请求头
        RangeSpec spec = parseRangeHeader(rangeHeader, total);

        if (spec == null) {
            // Range 语法错误，忽略 Range，返回完整文件
            return fullResponse(
                    path,
                    0,
                    total,
                    total,
                    contentType,
                    disposition
            );
        }

        if (spec.status == HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE) {
            return notSatisfiableResponse(total);
        }

        long start = spec.start;
        long end = spec.end;
        long length = end - start + 1;

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(
                        HttpHeaders.CONTENT_RANGE,
                        "bytes " + start + "-" + end + "/" + total
                )
                .contentLength(length)
                .body(streamingBody(path, start, length));
    }

    /**
     * 手动解析 HTTP Range 请求头。
     *
     * 支持：
     * bytes=start-end
     * bytes=start-
     * bytes=-suffix
     *
     * 返回 null：语法错误，忽略 Range。
     * 返回 416：请求范围无法满足。
     * 返回有效 start/end：返回 206。
     */
    private RangeSpec parseRangeHeader(
            String rangeHeader,
            long total) {

        if (rangeHeader == null ||
                !rangeHeader.startsWith("bytes=")) {
            return null;
        }

        String value = rangeHeader
                .substring("bytes=".length())
                .trim();

        // 不支持多区间 Range
        if (value.contains(",")) {
            return new RangeSpec(
                    HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE,
                    -1,
                    -1
            );
        }

        int dash = value.indexOf('-');

        if (dash < 0) {
            return null;
        }

        String startStr = value.substring(0, dash).trim();
        String endStr = value.substring(dash + 1).trim();

        try {
            if (startStr.isEmpty()) {

                // bytes=-suffix
                long suffix = Long.parseLong(endStr);

                if (suffix <= 0 || total == 0) {
                    return null;
                }

                if (suffix > total) {
                    suffix = total;
                }

                return new RangeSpec(
                        null,
                        total - suffix,
                        total - 1
                );
            }

            long start = Long.parseLong(startStr);

            if (start >= total) {
                return new RangeSpec(
                        HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE,
                        -1,
                        -1
                );
            }

            long end;

            if (endStr.isEmpty()) {
                // bytes=start-
                end = total - 1;

            } else {
                end = Long.parseLong(endStr);

                if (end < start) {
                    return new RangeSpec(
                            HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE,
                            -1,
                            -1
                    );
                }

                if (end >= total) {
                    end = total - 1;
                }
            }

            return new RangeSpec(null, start, end);

        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Range 解析结果
     */
    private static class RangeSpec {

        final HttpStatus status;
        final long start;
        final long end;

        RangeSpec(HttpStatus status, long start, long end) {
            this.status = status;
            this.start = start;
            this.end = end;
        }
    }

    /**
     * 完整下载：HTTP 200
     */
    private ResponseEntity<StreamingResponseBody> fullResponse(
            Path path,
            long start,
            long length,
            long total,
            MediaType contentType,
            String disposition) {

        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .contentLength(length)
                .body(streamingBody(path, start, length));
    }

    /**
     * 无法满足 Range 请求：HTTP 416
     */
    private ResponseEntity<StreamingResponseBody> notSatisfiableResponse(
            long total) {

        return ResponseEntity
                .status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_RANGE, "bytes */" + total)
                .build();
    }

    /**
     * 构造流式响应体。
     *
     * 随机定位到指定位置，只输出指定长度的数据。
     */
    private StreamingResponseBody streamingBody(
            Path path,
            long begin,
            long count) {

        return outputStream -> {

            try (var raf = new java.io.RandomAccessFile(
                    path.toFile(),
                    "r")) {

                raf.seek(begin);

                long remaining = count;

                byte[] buf = new byte[8192];

                while (remaining > 0) {

                    int read = raf.read(
                            buf,
                            0,
                            (int) Math.min(buf.length, remaining)
                    );

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(buf, 0, read);

                    remaining -= read;
                }

                outputStream.flush();
            }
        };
    }

    /**
     * 将 StoredFile 转换成前端需要的文件信息。
     */
    private Map<String, Object> toFileVO(StoredFile stored) {

        Map<String, Object> m = new HashMap<>();

        m.put("fileId", stored.getFileId());
        m.put("fileName", stored.getFileName());
        m.put("size", stored.getSize());
        m.put("md5", stored.getMd5());

        String baseUrl = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .build()
                .toUriString();

        m.put(
                "downloadUrl",
                baseUrl + "/api/v1/file/download/" + stored.getFileId()
        );

        return m;
    }
}
