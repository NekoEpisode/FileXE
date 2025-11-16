package io.github.nekosora.api.story.node;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class StoryNode {
    private final String id;
    private final Map<String, StoryNode> nextStoryNodes = new ConcurrentHashMap<>();

    public StoryNode(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    /**
     * Returns the description of the story node.
     * Can be overridden
     *
     * @return the description of the story node, or null if no description is available
     */
    public String getDescription() {
        return null;
    }

    public abstract void execute();

    public void addNext(StoryNode node) {
        if (node == null) return;
        nextStoryNodes.put(node.getId(), node);
    }

    public void removeNext(String id) {
        nextStoryNodes.remove(id);
    }

    public StoryNode getNextNode(String id) {
        return nextStoryNodes.get(id);
    }

    public void printTree() {
        printTree("", true, new HashSet<>());
    }

    private void printTree(String prefix, boolean isTail, Set<String> visited) {
        String symbol = isTail ? "└── " : "├── ";

        // 构建显示内容：ID + 描述（如果有）
        String display = id;
        String description = getDescription();
        if (description != null && !description.trim().isEmpty()) {
            display += " - " + description;
        }

        // 检测循环
        if (visited.contains(id)) {
            System.out.println(prefix + symbol + display + " 🔁");
            return;
        }

        visited.add(id);
        System.out.println(prefix + symbol + display);

        int count = nextStoryNodes.size();
        int i = 0;
        for (StoryNode node : nextStoryNodes.values()) {
            boolean isChildTail = (++i == count);
            String newPrefix = prefix + (isTail ? "    " : "│   ");
            node.printTree(newPrefix, isChildTail, new HashSet<>(visited));
        }
    }

    public void printPlotView() {
        printPlotView("", true, new HashSet<>(), 0);
    }

    private void printPlotView(String prefix, boolean isTail, Set<String> visited, int depth) {
        String symbol = isTail ? "└── " : "├── ";

        // 构建显示内容
        String display = id;
        String description = getDescription();
        if (description != null && !description.trim().isEmpty()) {
            display += " [" + description + "]";
        }

        // 检测循环
        if (visited.contains(id)) {
            System.out.println(prefix + symbol + display + " ⤴");
            return;
        }

        // 防止栈溢出
        if (depth > 50) {
            System.out.println(prefix + symbol + display + " ... (深度限制)");
            return;
        }

        visited.add(id);
        System.out.println(prefix + symbol + display);

        int nodeCount = nextStoryNodes.size();
        int i = 0;
        for (StoryNode node : nextStoryNodes.values()) {
            boolean isChildTail = (++i == nodeCount);
            String newPrefix = prefix + (isTail ? "    " : "│   ");
            node.printPlotView(newPrefix, isChildTail, new HashSet<>(visited), depth + 1);
        }
    }
}
