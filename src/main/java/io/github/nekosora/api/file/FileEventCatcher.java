package io.github.nekosora.api.file;

import io.github.nekosora.utils.CrashUtils;
import io.github.nekosora.utils.GameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文件事件监听器
 * data 数组内容：
 * - data[0]: File - 目标文件
 * - data[1]: FileEventType - 事件类型
 * - data[2]: 事件相关数据（根据事件类型不同）
 * - data[3]: 事件相关数据（根据事件类型不同）
 *
 * MODIFIED: data[2]=旧内容(String/null), data[3]=新内容(String/null)
 * CREATED: data[2]=文件大小(Long)
 * DELETE_OR_MOVED: data[2]=删除前的文件大小(Long)
 * RENAMED: data[2]=旧文件名(String), data[3]=新文件(File)
 */
public class FileEventCatcher {
    private final File target;
    private final FileEventRunnable recall;
    private final FileEventType type;
    private volatile boolean active = false;
    private volatile boolean autoFollowRename = true; // 自动跟踪重命名

    // 文件内容缓存大小限制（10MB）
    private static final long MAX_CACHE_SIZE = 10 * 1024 * 1024;

    // 缓存文件内容用于 MODIFIED 事件（仅小文件）
    private volatile String lastContent = null;
    private volatile String lastContentHash = null; // 内容哈希值

    public FileEventCatcher(File target, FileEventRunnable recall, FileEventType type) {
        this.target = target;
        this.recall = recall;
        this.type = type;
    }

    /**
     * 开始监听文件
     */
    public void start() {
        if (active) {
            return;
        }
        active = true;

        // 启动时初始化文件内容缓存
        if (target.exists() && target.isFile()) {
            initializeContentCache();
        }

        FileEventManager.getInstance().register(this);
    }

    /**
     * 初始化内容缓存（仅小文件）
     */
    private void initializeContentCache() {
        long fileSize = target.length();
        if (fileSize <= MAX_CACHE_SIZE) {
            this.lastContent = readFileContent(target);
            this.lastContentHash = calculateHash(this.lastContent);
        } else {
            this.lastContent = null;
            this.lastContentHash = calculateFileHash(target);
        }
    }

    /**
     * 停止监听文件
     */
    public void stop() {
        if (!active) {
            return;
        }
        active = false;
        FileEventManager.getInstance().unregister(this);
    }

    /**
     * 设置是否自动跟踪重命名（默认开启）
     */
    public void setAutoFollowRename(boolean autoFollowRename) {
        this.autoFollowRename = autoFollowRename;
    }

    /**
     * 判断是否应该处理该事件
     */
    boolean shouldHandle(FileEventType eventType) {
        if (!active) {
            return false;
        }
        return type == FileEventType.ALL || type == eventType;
    }

    /**
     * 触发回调 - MODIFIED 事件
     */
    void triggerModified() {
        if (!shouldHandle(FileEventType.MODIFIED)) {
            return;
        }

        try {
            long fileSize = target.exists() ? target.length() : 0L;
            String newContent = null;
            String oldContent = lastContent;

            // 只读取小文件内容
            if (fileSize <= MAX_CACHE_SIZE) {
                newContent = readFileContent(target);
                lastContent = newContent;
            } else {
                // 大文件不缓存旧内容
                oldContent = null;
                newContent = null; // 或者可以返回部分内容
            }

            // 更新哈希
            if (newContent != null) {
                lastContentHash = calculateHash(newContent);
            } else {
                lastContentHash = calculateFileHash(target);
            }

            Object[] data = new Object[]{target, FileEventType.MODIFIED, oldContent, newContent};
            recall.run(data);
        } catch (Exception e) {
            CrashUtils.crash(e);
        }
    }

    /**
     * 触发回调 - CREATED 事件
     */
    void triggerCreated() {
        if (!shouldHandle(FileEventType.CREATED)) {
            return;
        }

        try {
            long fileSize = target.exists() ? target.length() : 0L;

            // 初始化内容缓存
            if (fileSize <= MAX_CACHE_SIZE) {
                lastContent = readFileContent(target);
                lastContentHash = calculateHash(lastContent);
            } else {
                lastContent = null;
                lastContentHash = calculateFileHash(target);
            }

            Object[] data = new Object[]{target, FileEventType.CREATED, fileSize};
            recall.run(data);
        } catch (Exception e) {
            CrashUtils.crash(e);
        }
    }

    /**
     * 触发回调 - DELETE_OR_MOVED 事件
     */
    void triggerDelete(long lastFileSize) {
        if (!shouldHandle(FileEventType.DELETE_OR_MOVED)) {
            return;
        }

        try {
            Object[] data = new Object[]{target, FileEventType.DELETE_OR_MOVED, lastFileSize};
            recall.run(data);
        } catch (Exception e) {
            CrashUtils.crash(e);
        }
    }

    /**
     * 触发回调 - RENAMED 事件
     */
    void triggerRenamed(String oldFileName, File newFile) {
        if (!shouldHandle(FileEventType.RENAMED)) {
            return;
        }

        try {
            Object[] data = new Object[]{target, FileEventType.RENAMED, oldFileName, newFile};
            recall.run(data);

            // 如果开启自动跟踪，更新监听目标
            if (autoFollowRename && newFile != null) {
                FileEventManager.getInstance().updateTarget(this, newFile);
            }
        } catch (Exception e) {
            CrashUtils.crash(e);
        }
    }

    /**
     * 读取文件内容
     */
    private String readFileContent(File file) {
        if (!file.exists() || !file.isFile()) {
            return null;
        }

        try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 计算字符串内容的哈希值
     */
    private String calculateHash(String content) {
        if (content == null) {
            return null;
        }
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 计算文件的哈希值（用于大文件）
     */
    private String calculateFileHash(File file) {
        if (!file.exists() || !file.isFile()) {
            return null;
        }

        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    md.update(buffer, 0, bytesRead);
                }
            }
            return bytesToHex(md.digest());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 字节数组转十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 更新监听目标（用于重命名后的跟踪）
     */
    void updateTarget(File newTarget) {
        try {
            java.lang.reflect.Field field = FileEventCatcher.class.getDeclaredField("target");
            field.setAccessible(true);
            field.set(this, newTarget);

            // 更新内容缓存
            initializeContentCache();
        } catch (Exception e) {
            System.err.println("Failed to update target: " + e.getMessage());
        }
    }

    public File getTarget() {
        return target;
    }

    public FileEventType getType() {
        return type;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isAutoFollowRename() {
        return autoFollowRename;
    }

    String getLastContent() {
        return lastContent;
    }

    String getLastContentHash() {
        return lastContentHash;
    }
}

/**
 * 文件事件管理器（单例模式）
 */
class FileEventManager {
    private static final FileEventManager INSTANCE = new FileEventManager();
    private final Map<Path, DirectoryWatcher> watchers = new ConcurrentHashMap<>();

    private FileEventManager() {}

    public static FileEventManager getInstance() {
        return INSTANCE;
    }

    public synchronized void register(FileEventCatcher catcher) {
        File target = catcher.getTarget();
        Path directory = target.getParentFile().toPath();

        DirectoryWatcher watcher = watchers.get(directory);
        if (watcher == null) {
            try {
                watcher = new DirectoryWatcher(directory);
                watchers.put(directory, watcher);

                Thread thread = new Thread(watcher, "FileWatcher-" + directory.getFileName());
                thread.setDaemon(true);
                thread.start();
            } catch (IOException e) {
                throw new RuntimeException("Failed to create watcher for directory: " + directory, e);
            }
        }

        watcher.addCatcher(catcher);
    }

    public synchronized void unregister(FileEventCatcher catcher) {
        File target = catcher.getTarget();
        Path directory = target.getParentFile().toPath();

        DirectoryWatcher watcher = watchers.get(directory);
        if (watcher != null) {
            watcher.removeCatcher(catcher);

            if (watcher.isEmpty()) {
                watcher.shutdown();
                watchers.remove(directory);
            }
        }
    }

    /**
     * 更新监听目标（重命名后）
     */
    public synchronized void updateTarget(FileEventCatcher catcher, File newTarget) {
        File oldTarget = catcher.getTarget();
        Path oldDirectory = oldTarget.getParentFile().toPath();
        Path newDirectory = newTarget.getParentFile().toPath();

        // 如果目录相同，只需要更新文件名映射
        if (oldDirectory.equals(newDirectory)) {
            DirectoryWatcher watcher = watchers.get(oldDirectory);
            if (watcher != null) {
                watcher.updateCatcherTarget(catcher, newTarget);
            }
        } else {
            // 如果目录不同，需要重新注册
            unregister(catcher);
            catcher.updateTarget(newTarget);
            register(catcher);
        }
    }
}

/**
 * 目录监听器
 */
class DirectoryWatcher implements Runnable {
    private final Path directory;
    private final WatchService watchService;
    private final Map<String, List<FileEventCatcher>> catcherMap = new ConcurrentHashMap<>();
    private final Map<String, FileMetadata> fileMetadata = new ConcurrentHashMap<>(); // 记录文件元数据

    // 重命名检测
    private final Map<String, DeletionRecord> deletionRecords = new ConcurrentHashMap<>();
    private static final long RENAME_DETECTION_WINDOW = 500; // 500ms 内的删除+创建视为重命名

    private volatile boolean running = true;

    /**
     * 文件元数据
     */
    private static class FileMetadata {
        final long size;
        final String contentHash;

        FileMetadata(long size, String contentHash) {
            this.size = size;
            this.contentHash = contentHash;
        }
    }

    /**
     * 删除记录
     */
    private static class DeletionRecord {
        final long timestamp;
        final long size;
        final String contentHash;

        DeletionRecord(long timestamp, long size, String contentHash) {
            this.timestamp = timestamp;
            this.size = size;
            this.contentHash = contentHash;
        }
    }

    public DirectoryWatcher(Path directory) throws IOException {
        this.directory = directory;
        this.watchService = FileSystems.getDefault().newWatchService();

        directory.register(watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY);

        // 初始化文件大小记录
        initializeFileSizes();
    }

    private void initializeFileSizes() {
        File dir = directory.toFile();
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    String hash = calculateQuickHash(file);
                    fileMetadata.put(file.getName(), new FileMetadata(file.length(), hash));
                }
            }
        }
    }

    /**
     * 快速计算文件哈希（用于重命名检测）
     */
    private String calculateQuickHash(File file) {
        if (!file.exists() || !file.isFile()) {
            return null;
        }

        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    md.update(buffer, 0, bytesRead);
                }
            }
            byte[] hash = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public void addCatcher(FileEventCatcher catcher) {
        String fileName = catcher.getTarget().getName();
        catcherMap.computeIfAbsent(fileName, k -> new ArrayList<>()).add(catcher);

        // 记录文件元数据
        if (catcher.getTarget().exists()) {
            String hash = catcher.getLastContentHash();
            if (hash == null) {
                hash = calculateQuickHash(catcher.getTarget());
            }
            fileMetadata.put(fileName, new FileMetadata(catcher.getTarget().length(), hash));
        }
    }

    public void removeCatcher(FileEventCatcher catcher) {
        String fileName = catcher.getTarget().getName();
        List<FileEventCatcher> list = catcherMap.get(fileName);
        if (list != null) {
            list.remove(catcher);
            if (list.isEmpty()) {
                catcherMap.remove(fileName);
                fileMetadata.remove(fileName);
            }
        }
    }

    public void updateCatcherTarget(FileEventCatcher catcher, File newTarget) {
        String oldFileName = catcher.getTarget().getName();
        String newFileName = newTarget.getName();

        // 从旧文件名映射中移除
        List<FileEventCatcher> list = catcherMap.get(oldFileName);
        if (list != null) {
            list.remove(catcher);
            if (list.isEmpty()) {
                catcherMap.remove(oldFileName);
            }
        }

        // 更新 catcher 内部的 target
        catcher.updateTarget(newTarget);

        // 添加到新文件名映射
        catcherMap.computeIfAbsent(newFileName, k -> new ArrayList<>()).add(catcher);

        // 更新文件元数据
        fileMetadata.remove(oldFileName);
        if (newTarget.exists()) {
            String hash = catcher.getLastContentHash();
            if (hash == null) {
                hash = calculateQuickHash(newTarget);
            }
            fileMetadata.put(newFileName, new FileMetadata(newTarget.length(), hash));
        }
    }

    public boolean isEmpty() {
        return catcherMap.isEmpty();
    }

    public void shutdown() {
        running = false;
        try {
            watchService.close();
        } catch (IOException e) {
            CrashUtils.crash(e);
        }
    }

    @Override
    public void run() {
        try {
            while (running) {
                WatchKey key = watchService.take();

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }

                    Path fileName = (Path) event.context();
                    String fileNameStr = fileName.toString();

                    handleEvent(kind, fileNameStr);
                }

                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ClosedWatchServiceException e) {
            // WatchService 已关闭，正常退出
        }
    }

    private void handleEvent(WatchEvent.Kind<?> kind, String fileName) {
        if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
            handleCreate(fileName);
        } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
            handleDelete(fileName);
        } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
            handleModify(fileName);
        }
    }

    private void handleCreate(String fileName) {
        File newFile = directory.resolve(fileName).toFile();
        String newHash = calculateQuickHash(newFile);

        // 首先检查是否是重命名事件
        String renamedFrom = detectRename(newHash);

        if (renamedFrom != null) {
            // 如果是重命名，直接处理重命名并返回
            handleRename(renamedFrom, fileName);

            // 更新新文件的元数据
            if (newFile.exists()) {
                fileMetadata.put(fileName, new FileMetadata(newFile.length(), newHash));
            }
            return; // 重要：直接返回，不触发创建事件
        }

        // 如果不是重命名，才是真正的创建事件
        List<FileEventCatcher> catchers = catcherMap.get(fileName);
        if (catchers != null) {
            for (FileEventCatcher catcher : new ArrayList<>(catchers)) {
                catcher.triggerCreated();
            }
        }

        // 更新文件元数据
        if (newFile.exists()) {
            fileMetadata.put(fileName, new FileMetadata(newFile.length(), newHash));
        }
    }

    private void handleDelete(String fileName) {
        FileMetadata metadata = fileMetadata.remove(fileName);
        long fileSize = metadata != null ? metadata.size : 0L;
        String contentHash = metadata != null ? metadata.contentHash : null;

        // 记录删除信息，用于重命名检测
        deletionRecords.put(fileName, new DeletionRecord(
                System.currentTimeMillis(),
                fileSize,
                contentHash
        ));

        // 延迟触发删除事件，等待重命名检测
        scheduleDelayedDelete(fileName, fileSize);

        // 注意：这里不再立即触发删除事件！
    }

    private void scheduleDelayedDelete(String fileName, long fileSize) {
        // 使用 ScheduledExecutorService 来延迟执行
        java.util.concurrent.ScheduledExecutorService scheduler =
                java.util.concurrent.Executors.newScheduledThreadPool(1);

        scheduler.schedule(() -> {
            // 检查这个文件是否已经被判定为重命名
            if (deletionRecords.containsKey(fileName)) {
                // 如果还在删除记录中，说明不是重命名，触发真正的删除事件
                List<FileEventCatcher> catchers = catcherMap.get(fileName);
                if (catchers != null) {
                    for (FileEventCatcher catcher : new ArrayList<>(catchers)) {
                        catcher.triggerDelete(fileSize);
                    }
                }
                // 清理删除记录
                deletionRecords.remove(fileName);
            }
        }, RENAME_DETECTION_WINDOW + 100, java.util.concurrent.TimeUnit.MILLISECONDS);

        scheduler.shutdown();
    }

    private void handleModify(String fileName) {
        List<FileEventCatcher> catchers = catcherMap.get(fileName);
        if (catchers != null) {
            for (FileEventCatcher catcher : new ArrayList<>(catchers)) {
                catcher.triggerModified();
            }
        }

        // 更新文件元数据
        File file = directory.resolve(fileName).toFile();
        if (file.exists()) {
            String hash = calculateQuickHash(file);
            fileMetadata.put(fileName, new FileMetadata(file.length(), hash));
        }
    }

    private void handleRename(String oldFileName, String newFileName) {
        // 立即从删除记录中移除，这样延迟的删除事件就不会被触发
        DeletionRecord record = deletionRecords.remove(oldFileName);

        List<FileEventCatcher> catchers = catcherMap.get(oldFileName);
        if (catchers != null) {
            File newFile = directory.resolve(newFileName).toFile();

            for (FileEventCatcher catcher : new ArrayList<>(catchers)) {
                catcher.triggerRenamed(oldFileName, newFile);
            }
        }
    }

    /**
     * 检测是否是重命名事件
     * 通过比对内容哈希来确定是否是同一个文件
     */
    private String detectRename(String newFileHash) {
        if (newFileHash == null) {
            return null;
        }

        long now = System.currentTimeMillis();

        for (Map.Entry<String, DeletionRecord> entry : deletionRecords.entrySet()) {
            String deletedFileName = entry.getKey();
            DeletionRecord record = entry.getValue();

            // 检查时间窗口
            if (now - record.timestamp > RENAME_DETECTION_WINDOW) {
                continue;
            }

            // 检查是否有监听器在监听被删除的文件
            if (!catcherMap.containsKey(deletedFileName)) {
                continue;
            }

            // 关键：比对内容哈希
            if (record.contentHash != null && record.contentHash.equals(newFileHash)) {
                return deletedFileName;
            }
        }

        return null;
    }

    private void cleanOldDeletions() {
        long now = System.currentTimeMillis();
        deletionRecords.entrySet().removeIf(
                entry -> now - entry.getValue().timestamp > RENAME_DETECTION_WINDOW
        );
    }
}