package ru.spbau.sazanovich.nikita.mygit.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Object which corresponds to a directory in filesystems. Stores several blobs and subtrees.
 */
public class Tree implements Serializable {

    public static final String TYPE = "tree";

    private final List<TreeObject> children;

    public Tree() {
        this(new ArrayList<>());
    }

    public Tree(List<TreeObject> children) {
        this.children = children;
    }

    /**
     * Objects which represent edges in a filesystem graph
     */
    public static class TreeObject implements Serializable {

        private String sha;
        private String name;
        private String type;
    }
}
