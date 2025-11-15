package io.github.nekosora.api.file.widgets;

import io.github.nekosora.api.file.FileEventCatcher;

public abstract class FileWidgets {
    private final FileEventCatcher catcher;

    public FileWidgets (FileEventCatcher catcher) {
        this.catcher = catcher;
    }

    public void register () {
        catcher.start();
    }
}
