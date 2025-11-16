package io.github.nekosora.api.story.stories.chapter1;

import io.github.nekosora.api.file.widgets.FileButton;
import io.github.nekosora.api.file.widgets.FileSwitch;
import io.github.nekosora.api.story.node.NodeIDs;
import io.github.nekosora.api.story.node.StoryNode;
import io.github.nekosora.settings.GameSettings;
import io.github.nekosora.utils.CrashUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;

public class Chapter1FirstSeenNode extends StoryNode {
    private static final Logger log = LoggerFactory.getLogger(Chapter1FirstSeenNode.class);

    public Chapter1FirstSeenNode() {
        super(NodeIDs.CHAPTER1_FIRST_SEEN.getId());
    }

    @Override
    public void execute() {
        File file = new File(GameSettings.mainDir, "FirstSeen.txt");
        try {
            String buttonTarget = "I just created a button on this folder, go press it, and go back here.\nYou can open the file to see hints."; // 任务内容

            File questFile;

            if (file.exists()) {
                File file1 = new File(GameSettings.mainDir, "FirstSeen-" + new Random().nextLong() + ".txt");
                if (file1.exists()) {
                    // 玩家精准预测了随机数名称
                    File file2 = File.createTempFile(String.valueOf(new Random().nextLong()), ".txt");
                    if (!file2.exists()) {
                        // 这里不可能被触发
                        throw new IOException("我求你了 (File creation error)");
                    }
                    file2.deleteOnExit();
                    Files.writeString(file2.toPath(), "How do you???\n*sigh*\nokay\nWelcome to FileXE, please complete these:\n" + buttonTarget);
                    questFile = file2;
                } else {
                    // file1不存在的情况
                    Files.writeString(file1.toPath(), "A HA, Got you!\n\nWelcome to FileXE! please complete these:\n" + buttonTarget);
                    questFile = file1;
                }
            } else {
                // 第二条线
                if (!file.createNewFile()) {
                    log.error("FileXE cannot create this file: {}", file.getAbsolutePath());
                }
                Files.writeString(file.toPath(), "Hello, Welcome to FileXE, please complete these:\n" + buttonTarget);
                questFile = file;
            }

            // TODO: 继续
            File buttonFile = new File(GameSettings.mainDir, "THIS_IS_A_BUTTON");
            if (!buttonFile.createNewFile()) {
                log.error("FileXE cannot create this file: {}", buttonFile.getAbsolutePath());
            }
            Files.writeString(buttonFile.toPath(), "You can press a button with delete it.\nDon't worry, button will reset(recreate) itself.");
            File finalQuestFile = questFile;
            FileButton button = new FileButton(v -> {
                try {
                    File switchFile = new File(GameSettings.mainDir, "THIS_IS_A_SWITCH.off");
                    if (!switchFile.createNewFile()) {
                        log.error("FileXE cannot create this file: {}", switchFile.getAbsolutePath());
                    }
                    Files.writeString(switchFile.toPath(), "You can switch it by change '.off' to '.on' or '.on' to '.off'.");
                    FileSwitch fswitch = new FileSwitch(b -> {
                        File result;
                        if (b) {
                            result = new File(GameSettings.mainDir, "YOU_TURNED_IT_ON");
                        } else {
                            result = new File(GameSettings.mainDir,"YOU_TURNED_IT_OFF");
                        }
                        try {
                            if (!result.createNewFile()) {
                                log.error("FileXE cannot create this file: {}", result.getAbsolutePath());
                            }
                        } catch (IOException e) {
                            CrashUtils.crash(e);
                        }
                    }, switchFile);
                    fswitch.register();

                    Files.writeString(finalQuestFile.toPath(), "Good, now i will create a switch, go try it");
                } catch (IOException e) {
                    CrashUtils.crash(e);
                }
            }, new File(GameSettings.mainDir, "THIS_IS_A_BUTTON"));
            button.register();
        } catch (IOException e) {
            CrashUtils.crash(e);
        }
    }

    @Override
    public String getDescription() {
        return "第一章FirstSeen任务节点";
    }
}
