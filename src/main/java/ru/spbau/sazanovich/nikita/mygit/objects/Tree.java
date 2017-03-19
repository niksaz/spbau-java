package ru.spbau.sazanovich.nikita.mygit.objects;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Object which corresponds to a directory in filesystems. Stores several blobs and subtrees.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Tree implements Serializable {

    /**
     * Constant which is used through the library to identify Tree objects.
     */
    public static final String TYPE = "tree";

    @NotNull
    private final List<TreeEdge> children;

    /**
     * Constructs a tree with no children.
     */
    public Tree() {
        this(new ArrayList<>());
    }

    /**
     * Adds a new child {@link TreeEdge} to the list of children.
     *
     * @param child adds {@link TreeEdge} as a children of the tree
     * @return whether it was added
     */
    public boolean addChild(TreeEdge child) {
        return children.add(child);
    }

    /**
     * Objects which are put into {@link Tree} object to represent edges in a filesystem graph.
     */
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public static class TreeEdge implements Serializable {

        @NotNull
        private String hash;
        @NotNull
        private String name;
        @NotNull
        private String type;
        @NotNull
        private Date dateCreated;

        /**
         * Construct an object with given hash, name and type. Uses current date as a creation date.
         *
         * @param hash hash of the file, associated with the edge
         * @param name name of the file, associated with the edge
         * @param type type of the file, associated with the edge (Branch.Type or Blob.Type)
         */
        public TreeEdge(@NotNull String hash, @NotNull String name, @NotNull String type) {
            this(hash, name, type, new Date());
        }

        /**
         * Checks whether the file is directory.
         *
         * @return {@code true} if it is directory; {@code false} otherwise
         */
        public boolean isDirectory() {
            return getType().equals(Tree.TYPE);
        }
    }
}
