package io.github.nekosora.api.file.widgets;

import io.github.nekosora.api.file.FileEventCatcher;
import io.github.nekosora.api.file.FileEventType;
import io.github.nekosora.utils.CrashUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class FileButton extends FileWidgets {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private static final Logger log = LoggerFactory.getLogger(FileButton.class);

    // 添加关闭方法
    public static void shutdown() {
        scheduler.shutdown();
    }

    public FileButton(Consumer<Void> handler, File file) {
        super(createEventCatcher(handler, file));
    }

    private static FileEventCatcher createEventCatcher(Consumer<Void> handler, File file1) {
        return new FileEventCatcher(file1, (data) -> {
            handler.accept(null);  // 执行按钮点击的处理程序

            scheduler.schedule(() -> {
                // 检查原始文件是否不存在（被删除或移动了）
                File originalFile = new File(file1.getParent(), file1.getName());
                if (!originalFile.exists()) {
                    try {
                        if (!originalFile.createNewFile()) {
                            log.error("Could not create {}", originalFile.getAbsolutePath());
                        } else {
                            log.debug("Button reset: {}", originalFile.getAbsolutePath());
                        }
                    } catch (IOException e) {
                        CrashUtils.crash(e);
                    }
                }
            }, 2, TimeUnit.SECONDS);  // 2秒后回弹
        }, FileEventType.DELETE_OR_MOVED);
    }
}