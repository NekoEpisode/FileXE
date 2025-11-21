package io.github.nekosora.api.story.stories.chapter1;

import io.github.nekosora.api.story.node.NodeIDs;
import io.github.nekosora.api.story.node.NodeNotFoundError;
import io.github.nekosora.api.story.node.StoryNode;

public class Chapter1RootNode extends StoryNode {
    public Chapter1RootNode() {
        super(NodeIDs.CHAPTER1.getId());
    }

    @Override
    public void execute() {
        super.execute();
        if (getNextNode(NodeIDs.CHAPTER1_FIRST_SEEN.getId()) == null) throw new NodeNotFoundError("Chapter1FirstSeen node not found, please addNext instead");

        StoryNode firstSeen = getNextNode(NodeIDs.CHAPTER1_FIRST_SEEN.getId());
        firstSeen.execute();
    }
}
