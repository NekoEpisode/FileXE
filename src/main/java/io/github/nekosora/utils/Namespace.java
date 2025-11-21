package io.github.nekosora.utils;

import java.util.Objects;

public record Namespace(String name, String path) {
    @Override
    public String toString () {
        return name + ":" + path;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Namespace namespace = (Namespace) o;
        return Objects.equals(name, namespace.name) && Objects.equals(path, namespace.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, path);
    }

    public static Namespace fromString(String string) {
        String[] split = string.split(":");
        if (split.length < 2) throw new IllegalArgumentException("Invalid namespace string");
        return new Namespace(split[0], split[1]);
    }
}
