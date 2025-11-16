package io.github.nekosora.utils;

import io.github.nekosora.api.story.node.StoryNode;
import io.github.nekosora.api.story.node.StoryNodeRegistry;
import io.github.nekosora.api.story.stories.chapter1.Chapter1FirstSeenNode;
import io.github.nekosora.api.story.stories.chapter1.Chapter1RootNode;

public class StoryUtils {
    public static void init() {
        StoryNode chapter1 = initChapter1();

        StoryNodeRegistry.ROOT.addNext(chapter1);
        StoryNodeRegistry.ROOT.printPlotView();
    }

    private static StoryNode initChapter1() {
        StoryNode chapter1 = new Chapter1RootNode();
        chapter1.addNext(new Chapter1FirstSeenNode());
        return chapter1;
    }
}
