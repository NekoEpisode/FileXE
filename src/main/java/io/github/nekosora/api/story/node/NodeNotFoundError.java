package io.github.nekosora.api.story.node;

public class NodeNotFoundError extends RuntimeException {
    public NodeNotFoundError(String message) {
        super(message);
    }
}
