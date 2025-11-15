package io.github.nekosora.api.file;

import java.io.IOException;

public interface FileEventRunnable {
    void run(Object[] data) throws IOException;
}
