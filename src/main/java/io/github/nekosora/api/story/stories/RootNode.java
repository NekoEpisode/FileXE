package io.github.nekosora.api.story.stories;

import io.github.nekosora.api.story.node.NodeIDs;
import io.github.nekosora.api.story.node.NodeNotFoundError;
import io.github.nekosora.api.story.node.StoryNode;

public class RootNode extends StoryNode {
    public RootNode(String id) {
        super(id);
    }

    @Override
    public void execute() {
        if (getNextNode(NodeIDs.CHAPTER1.getId()) == null) throw new NodeNotFoundError("Chapter1 node not found, please addNext instead");

        StoryNode chapter1 = getNextNode(NodeIDs.CHAPTER1.getId());
        chapter1.execute();

        // 顺序执行
    }
}
