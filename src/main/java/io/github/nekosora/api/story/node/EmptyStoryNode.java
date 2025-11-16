package io.github.nekosora.api.story.node;

public class EmptyStoryNode extends StoryNode {
    private final String description;

    public EmptyStoryNode(String id, String description) {
        super(id);
        this.description = description;
    }

    public EmptyStoryNode(String id) {
        this(id, null);
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void execute() {}
}
