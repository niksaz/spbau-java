package ru.spbau.sazanovich.nikita.mygit.objects;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Object which corresponds to a directory in filesystems. Stores several blobs and subtrees.
 */
public class Tree implements Serializable {

    public static final String TYPE = "tree";

    @NotNull
    private final List<TreeObject> children;

    public Tree() {
        this(new ArrayList<>());
    }

    private Tree(@NotNull List<TreeObject> children) {
        this.children = children;
    }

    @NotNull
    public List<TreeObject> getChildren() {
        return children;
    }

    public boolean addChild(TreeObject child) {
        return children.add(child);
    }

    /**
     * Objects which are put into {@link Tree object} to represent edges in a filesystem graph
     */
    public static class TreeObject implements Serializable {

        @NotNull
        private String sha;
        @NotNull
        private String name;
        @NotNull
        private String type;
        @NotNull
        private Date dateCreated;

        public TreeObject(@NotNull String sha, @NotNull String name, @NotNull String type) {
            this.sha = sha;
            this.name = name;
            this.type = type;
            this.dateCreated = new Date();
        }

        @NotNull
        public String getSha() {
            return sha;
        }

        @NotNull
        public String getName() {
            return name;
        }

        @NotNull
        public String getType() {
            return type;
        }

        @NotNull
        public Date getDateCreated() {
            return dateCreated;
        }

        public boolean isDirectory() {
            return getType().equals(Tree.TYPE);
        }
    }
}
