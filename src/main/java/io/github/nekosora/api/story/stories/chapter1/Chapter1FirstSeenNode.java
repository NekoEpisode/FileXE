package io.github.nekosora.api.story.stories.chapter1;

import io.github.nekosora.api.story.node.NodeIDs;
import io.github.nekosora.api.story.node.StoryNode;
import io.github.nekosora.settings.GameSettings;
import io.github.nekosora.utils.CrashUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Chapter1FirstSeenNode extends StoryNode {
    private static final Logger log = LoggerFactory.getLogger(Chapter1FirstSeenNode.class);

    private final File FIRST_SEEN = new File(GameSettings.mainDir, "FirstSeen.txt");

    public Chapter1FirstSeenNode() {
        super(NodeIDs.CHAPTER1_FIRST_SEEN.getId());
    }

    @Override
    public void execute() {
        super.execute();

        try {
            if (!FIRST_SEEN.exists()) {
                // TODO: 完成剧情
                if (!FIRST_SEEN.createNewFile()) {
                    throw new IOException("FileXE cannot create file: " + FIRST_SEEN.getAbsolutePath());
                }
                Files.writeString(FIRST_SEEN.toPath(), "1");
            } else {
                Files.writeString(FIRST_SEEN.toPath(), "");
            }
        } catch (IOException e) {
            CrashUtils.crash(e);
        }
    }

    @Override
    public void cleanFiles() {
        FIRST_SEEN.delete();

    }

    @Override
    public String getDescription() {
        return "第一章FirstSeen任务节点";
    }
}
