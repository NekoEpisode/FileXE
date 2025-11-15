package io.github.nekosora.api.story;

import java.util.Arrays;
import java.util.List;

public abstract class StoryNode {
    private final int id;
    private final List<StoryNode> nextNodes;

    public StoryNode(int id, List<StoryNode> nextNodes) {
        this.id = id;
        this.nextNodes = nextNodes;
    }

    public StoryNode(int id, StoryNode... nextNodes) {
        this(id, Arrays.asList(nextNodes));
    }

    public int getId() {
        return id;
    }

    abstract void execute();

    public List<StoryNode>  getNextNodes() {
        return nextNodes;
    }
}
