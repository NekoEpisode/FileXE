package io.github.nekosora.api.file.widgets;

import io.github.nekosora.api.file.FileEventCatcher;
import io.github.nekosora.api.file.FileEventType;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class FileButton extends FileWidgets {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    public FileButton(Consumer<Void> handler, File file, String onExt) {
        super(createEventCatcher(handler, file, onExt));
    }

    private static FileEventCatcher createEventCatcher(Consumer<Void> handler, File file, String onExt) {
        return new FileEventCatcher(file, (data) -> {
            File newFile;
            if (data != null && data[3] instanceof File file1)
                newFile = file1;
            else
                throw new InternalError("RENAME data[3] should be of type File");

            handler.accept(null);

            scheduler.schedule(() -> {
                if (!newFile.exists()) return;
                if (newFile.getName().endsWith("." + onExt)) {
                    File offFile = new File(file.getParent(), file.getName());
                    newFile.renameTo(offFile);
                }
            }, 2, TimeUnit.SECONDS);
        }, FileEventType.RENAMED);
    }
}