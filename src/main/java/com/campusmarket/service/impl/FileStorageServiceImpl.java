
package com.campusmarket.service.impl;

import com.campusmarket.entity.FileUploadSession;
import com.campusmarket.entity.StoredFile;
import com.campusmarket.service.FileStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文件存储核心实现。
 *
 * 存储结构：
 * {storageDir}/staging/{uploadId}/{分片下标}.part
 * {storageDir}/staging/{uploadId}/meta.json
 * {storageDir}/final/{fileId}{扩展名}
 * {storageDir}/final/{fileId}.json
 *
 * 功能：
 * 1. 大文件分片上传
 * 2. 分片 MD5 校验
 * 3. 损坏分片识别及单片重传
 * 4. 断点续传
 * 5. 分片合并
 * 6. 整文件 MD5 校验
 * 7. 上传会话持久化及重启恢复
 * 8. 小文件批量上传
 */
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Logger log =
            LoggerFactory.getLogger(FileStorageServiceImpl.class);

    /** 上传中的分片临时目录 */
    private static final String DIR_STAGING = "staging";

    /** 合并完成的最终文件目录 */
    private static final String DIR_FINAL = "final";

    /** 上传会话元信息文件名 */
    private static final String SESSION_META = "meta.json";

    /** uploadId -> 上传会话 */
    private final Map<String, FileUploadSession> sessions =
            new ConcurrentHashMap<>();

    /** fileId -> 已存储文件 */
    private final Map<String, StoredFile> files =
            new ConcurrentHashMap<>();

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${file.storage.dir:file-storage}")
    private String storageDir;

    /**
     * 获取存储根目录绝对路径
     */
    private Path storageRoot() {
        return Paths.get(storageDir).toAbsolutePath();
    }

    private Path stagingRoot() {
        return storageRoot().resolve(DIR_STAGING);
    }

    private Path finalRoot() {
        return storageRoot().resolve(DIR_FINAL);
    }

    /**
     * 初始化存储目录，并恢复历史数据
     */
    @PostConstruct
    public void init() throws IOException {

        Files.createDirectories(stagingRoot());
        Files.createDirectories(finalRoot());

        recoverSessions();
        recoverFiles();

        log.info(
                "文件存储初始化完成，恢复会话 {} 个，恢复文件记录 {} 个",
                sessions.size(),
                files.size()
        );
    }

    /**
     * 恢复未完成的上传会话
     */
    private void recoverSessions() {

        try (DirectoryStream<Path> stream =
                     Files.newDirectoryStream(stagingRoot())) {

            for (Path uploadDir : stream) {

                Path meta = uploadDir.resolve(SESSION_META);

                if (!Files.isRegularFile(meta)) {
                    continue;
                }

                FileUploadSession session = objectMapper.readValue(
                        Files.readString(meta, StandardCharsets.UTF_8),
                        FileUploadSession.class
                );

                if (session != null && session.getUploadId() != null) {
                    sessions.put(session.getUploadId(), session);
                }
            }

        } catch (Exception e) {
            log.warn("恢复上传会话失败: {}", e.getMessage());
        }
    }

    /**
     * 恢复已完成文件的元信息
     */
    private void recoverFiles() {

        try (DirectoryStream<Path> stream =
                     Files.newDirectoryStream(finalRoot(), "*.json")) {

            for (Path meta : stream) {

                StoredFile stored = objectMapper.readValue(
                        Files.readString(meta, StandardCharsets.UTF_8),
                        StoredFile.class
                );

                if (stored != null && stored.getFileId() != null) {
                    files.put(stored.getFileId(), stored);
                }
            }

        } catch (Exception e) {
            log.warn("恢复文件记录失败: {}", e.getMessage());
        }
    }

    // ================= 1. 初始化分片上传 =================

    @Override
    public FileUploadSession initUpload(
            String fileName,
            long fileSize,
            int chunkSize,
            int totalChunks,
            String fileMd5) {

        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        if (fileSize <= 0 || chunkSize <= 0 || totalChunks <= 0) {
            throw new IllegalArgumentException("文件大小或分片参数不合法");
        }

        FileUploadSession session = new FileUploadSession();

        session.setUploadId(
                UUID.randomUUID().toString().replace("-", "")
        );

        session.setFileName(fileName);
        session.setFileSize(fileSize);
        session.setChunkSize(chunkSize);
        session.setTotalChunks(totalChunks);
        session.setFileMd5(fileMd5);
        session.setCreateTime(LocalDateTime.now());

        sessions.put(session.getUploadId(), session);

        persistSession(session);

        log.info(
                "初始化分片上传 uploadId={}, fileName={}, totalChunks={}",
                session.getUploadId(),
                fileName,
                totalChunks
        );

        return session;
    }

    // ================= 2. 保存单个分片 =================

    @Override
    public void saveChunk(
            String uploadId,
            int chunkIndex,
            MultipartFile file,
            String chunkMd5) throws IOException {
/*==========真正保存分片和校验 MD5 的代码=====================*/
        FileUploadSession session = sessions.get(uploadId);

        if (session == null) {
            throw new IllegalArgumentException(
                    "上传会话不存在，请先调用 /upload/init 初始化"
            );
        }

        if (chunkIndex < 0 || chunkIndex >= session.getTotalChunks()) {
            throw new IllegalArgumentException(
                    "分片下标越界: " + chunkIndex
            );
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传分片不能为空");
        }

        Path chunkDir = stagingRoot().resolve(uploadId);

        Files.createDirectories(chunkDir);

        Path chunkFile = chunkDir.resolve(chunkIndex + ".part");

        // 先将分片保存到磁盘
        // 同一个分片重复上传时会覆盖旧文件
        file.transferTo(chunkFile.toFile());

        // 如果前端提供了 MD5，则进行完整性校验
        if (chunkMd5 != null && !chunkMd5.trim().isEmpty()) {

            String actualMd5 = md5Hex(chunkFile);

            if (!chunkMd5.equalsIgnoreCase(actualMd5)) {

                // 删除损坏的分片
                // 避免该分片被后端识别为已上传
                Files.deleteIfExists(chunkFile);

                log.warn(
                        "分片校验失败 uploadId={}, chunkIndex={}, expectedMd5={}, actualMd5={}",
                        uploadId,
                        chunkIndex,
                        chunkMd5,
                        actualMd5
                );

                // 抛出专门异常，让 Controller 返回 missingChunks
                throw new ChunkIntegrityException(chunkIndex);
            }
        }

        log.info(
                "分片上传成功 uploadId={}, chunkIndex={}",
                uploadId,
                chunkIndex
        );
    }

    // ================= 3. 查询已上传分片 =================

    @Override
    public List<Integer> listUploadedChunks(String uploadId) {

        Path chunkDir = stagingRoot().resolve(uploadId);

        if (!Files.isDirectory(chunkDir)) {
            return Collections.emptyList();
        }

        List<Integer> indexes = new ArrayList<>();

        try (DirectoryStream<Path> stream =
                     Files.newDirectoryStream(chunkDir, "*.part")) {

            for (Path p : stream) {

                String name = p.getFileName().toString();

                String num = name.substring(
                        0,
                        name.length() - ".part".length()
                );

                indexes.add(Integer.parseInt(num));
            }

        } catch (Exception e) {

            log.warn(
                    "查询已上传分片失败 uploadId={}: {}",
                    uploadId,
                    e.getMessage()
            );
        }

        indexes.sort(Comparator.naturalOrder());

        return indexes;
    }

    // ================= 4. 合并分片 =================

    @Override
    public StoredFile merge(String uploadId) throws IOException {

        FileUploadSession session = sessions.get(uploadId);

        if (session == null) {
            throw new IllegalArgumentException("上传会话不存在");
        }

        Path chunkDir = stagingRoot().resolve(uploadId);

        // 4.1 检查分片是否全部上传
        List<Integer> uploaded = listUploadedChunks(uploadId);

        Set<Integer> uploadedSet = new HashSet<>(uploaded);

        List<Integer> missing = new ArrayList<>();

        for (int i = 0; i < session.getTotalChunks(); i++) {

            if (!uploadedSet.contains(i)) {
                missing.add(i);
            }
        }

        // 有缺失分片时，不执行合并
        if (!missing.isEmpty()) {

            log.warn(
                    "合并失败，存在缺失分片 uploadId={}, missingChunks={}",
                    uploadId,
                    missing
            );

            throw new MissingChunksException(missing);
        }

        // 4.2 按顺序合并分片
        String ext = extractExt(session.getFileName());

        String fileId =
                UUID.randomUUID().toString().replace("-", "");

        Path finalFile = finalRoot().resolve(fileId + ext);

        try (var out = Files.newOutputStream(
                finalFile,
                StandardOpenOption.CREATE_NEW
        )) {

            for (int i = 0; i < session.getTotalChunks(); i++) {

                Path part = chunkDir.resolve(i + ".part");

                Files.copy(part, out);
            }
        } catch (IOException e) {

            Files.deleteIfExists(finalFile);

            throw e;
        }

        // 4.3 检查合并后的文件大小
        long mergedSize = Files.size(finalFile);

        if (mergedSize != session.getFileSize()) {

            Files.deleteIfExists(finalFile);

            throw new IllegalArgumentException(
                    "合并后文件大小不正确，预期 "
                            + session.getFileSize()
                            + " 字节，实际 "
                            + mergedSize
                            + " 字节"
            );
        }

        // 4.4 整文件 MD5 校验
        String finalMd5 = md5Hex(finalFile);

        if (session.getFileMd5() != null
                && !session.getFileMd5().isEmpty()
                && !session.getFileMd5().equalsIgnoreCase(finalMd5)) {

            Files.deleteIfExists(finalFile);

            throw new IllegalArgumentException(
                    "合并后文件校验失败，请检查分片数据"
            );
        }

        // 4.5 保存文件记录
        StoredFile stored = new StoredFile();

        stored.setFileId(fileId);
        stored.setFileName(session.getFileName());
        stored.setSize(mergedSize);
        stored.setPath(finalFile.toAbsolutePath().toString());
        stored.setMd5(finalMd5);
        stored.setCreateTime(LocalDateTime.now());

        files.put(fileId, stored);

        persistFile(stored);

        // 4.6 清理临时分片和上传会话
        deleteChunks(chunkDir);

        sessions.remove(uploadId);

        log.info(
                "分片合并完成 fileId={}, fileName={}",
                fileId,
                stored.getFileName()
        );

        return stored;
    }

    // ================= 5. 小文件批量上传 =================

    @Override
    public List<BatchResultItem> saveSmallFiles(
            MultipartFile[] filesArr) throws IOException {

        List<BatchResultItem> results = new ArrayList<>();

        if (filesArr == null || filesArr.length == 0) {
            return results;
        }

        for (MultipartFile f : filesArr) {

            BatchResultItem item = new BatchResultItem();

            item.setFileName(f.getOriginalFilename());

            try {

                if (f.isEmpty()) {

                    item.setSuccess(false);
                    item.setMsg("空文件");

                    results.add(item);

                    continue;
                }

                String ext = extractExt(f.getOriginalFilename());

                String fileId =
                        UUID.randomUUID().toString().replace("-", "");

                Path target = finalRoot().resolve(fileId + ext);

                f.transferTo(target.toFile());

                StoredFile stored = new StoredFile();

                stored.setFileId(fileId);
                stored.setFileName(f.getOriginalFilename());
                stored.setSize(f.getSize());
                stored.setPath(target.toAbsolutePath().toString());
                stored.setMd5(md5Hex(target));
                stored.setCreateTime(LocalDateTime.now());

                files.put(fileId, stored);

                persistFile(stored);

                item.setSuccess(true);
                item.setFileId(fileId);
                item.setSize(f.getSize());
                item.setMsg("成功");

            } catch (Exception e) {

                item.setSuccess(false);
                item.setMsg(e.getMessage());
            }

            results.add(item);
        }

        return results;
    }

    // ================= 6. 获取已存储文件 =================

    @Override
    public StoredFile getFile(String fileId) {
        return files.get(fileId);
    }

    // ================= 7. 持久化工具方法 =================

    /**
     * 保存上传会话元信息
     */
    private void persistSession(FileUploadSession session) {

        try {

            Path chunkDir =
                    stagingRoot().resolve(session.getUploadId());

            Files.createDirectories(chunkDir);

            Files.writeString(
                    chunkDir.resolve(SESSION_META),
                    objectMapper.writeValueAsString(session),
                    StandardCharsets.UTF_8
            );

        } catch (Exception e) {

            log.warn(
                    "持久化上传会话失败 uploadId={}: {}",
                    session.getUploadId(),
                    e.getMessage()
            );
        }
    }

    /**
     * 保存文件记录元信息
     */
    private void persistFile(StoredFile stored) {

        try {

            Path meta = finalRoot().resolve(
                    stored.getFileId() + ".json"
            );

            Files.writeString(
                    meta,
                    objectMapper.writeValueAsString(stored),
                    StandardCharsets.UTF_8
            );

        } catch (Exception e) {

            log.warn(
                    "持久化文件记录失败 fileId={}: {}",
                    stored.getFileId(),
                    e.getMessage()
            );
        }
    }

    /**
     * 获取文件扩展名
     */
    private String extractExt(String fileName) {

        if (fileName == null) {
            return "";
        }

        int dot = fileName.lastIndexOf('.');

        return dot >= 0 ? fileName.substring(dot) : "";
    }

    /**
     * 删除上传临时目录
     */
    private void deleteChunks(Path chunkDir) throws IOException {

        if (!Files.isDirectory(chunkDir)) {
            return;
        }

        try (DirectoryStream<Path> stream =
                     Files.newDirectoryStream(chunkDir)) {

            for (Path p : stream) {
                Files.deleteIfExists(p);
            }
        }

        Files.deleteIfExists(chunkDir);
    }

    /**
     * 计算文件 MD5
     */
    private String md5Hex(Path path) throws IOException {

        MessageDigest md;

        try {
            md = MessageDigest.getInstance("MD5");

        } catch (Exception e) {

            throw new IOException("MD5算法不可用", e);
        }

        try (InputStream in = Files.newInputStream(path)) {

            byte[] buf = new byte[8192];

            int n;

            while ((n = in.read(buf)) != -1) {
                md.update(buf, 0, n);
            }
        }

        StringBuilder sb = new StringBuilder(32);

        for (byte b : md.digest()) {

            sb.append(
                    String.format("%02x", b)
            );
        }

        return sb.toString();
    }

    // ================= 8. 异常类 =================

    /**
     * 缺失分片异常
     *
     * 合并时发现分片不齐全，携带缺失分片下标。
     */
    public static class MissingChunksException
            extends RuntimeException {

        private final List<Integer> missingChunks;

        public MissingChunksException(List<Integer> missingChunks) {

            super("存在缺失分片");

            this.missingChunks = missingChunks;
        }

        public List<Integer> getMissingChunks() {
            return missingChunks;
        }
    }

    /**
     * 分片完整性校验异常
     *
     * 当某个分片 MD5 校验失败时抛出。
     * Controller 捕获后返回：
     * {
     *     "missingChunks": [损坏分片下标]
     * }
     */
    public static class ChunkIntegrityException
            extends RuntimeException {

        private final int chunkIndex;

        public ChunkIntegrityException(int chunkIndex) {

            super(
                    "第" + chunkIndex
                            + "片 MD5 校验失败，请重新上传该分片"
            );

            this.chunkIndex = chunkIndex;
        }

        public int getChunkIndex() {
            return chunkIndex;
        }
    }
}
