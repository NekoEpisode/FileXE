package io.github.nekosora.api.file.widgets;

import io.github.nekosora.api.file.FileEventCatcher;
import io.github.nekosora.api.file.FileEventType;

import java.io.File;
import java.util.function.Consumer;

public class FileSwitch extends FileWidgets {
    public FileSwitch(Consumer<Boolean> handler, File file, String onExt, String offExt) {
        super(new FileEventCatcher(file, (data) -> {
            File newFile;
            if (data != null && data[3] instanceof File file1)
                newFile = file1;
            else
                throw new InternalError("RENAME data[3] should be of type File");

            // 修正逻辑：检查文件扩展名
            String fileName = newFile.getName();
            if (fileName.endsWith(onExt)) {
                handler.accept(true);  // 切换到on状态
            } else if (fileName.endsWith(offExt)) {
                handler.accept(false); // 切换到off状态
            }
            // 如果都不是，说明扩展名不合法，忽略
        }, FileEventType.RENAMED));
    }

    // 便捷构造方法，使用默认的.on/.off
    public FileSwitch(Consumer<Boolean> handler, File file) {
        this(handler, file, ".on", ".off");
    }
}