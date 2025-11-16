package io.github.nekosora.api.story.node;

public enum NodeIDs {
    ROOT("root"),

    CHAPTER1("chapter1"),
    CHAPTER1_FIRST_SEEN("chapter1+first_seen")

    ;

    private final String id;

    NodeIDs(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString () {
        return getId();
    }
}
