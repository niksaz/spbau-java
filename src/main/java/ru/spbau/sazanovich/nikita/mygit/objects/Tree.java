package ru.spbau.sazanovich.nikita.mygit.objects;

import java.util.List;

/**
 * Object which corresponds to a directory in filesystems. Stores several blobs and subtrees.
 */
public class Tree extends GitObject {

    private static final String TYPE = "tree";

    private String name;
    private List<GitObject> children;

    public Tree(String name) {
        super(TYPE);
        this.name = name;
    }
}
