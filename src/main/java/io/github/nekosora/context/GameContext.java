package io.github.nekosora.context;

import io.github.nekosora.api.story.node.StoryNode;

public class GameContext {
    public static volatile boolean isExiting = false;
    public static volatile StoryNode singleplayerCurrentNode = null;
}
