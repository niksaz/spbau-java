package ru.spbau.sazanovich.nikita.mygit.objects;

/**
 * Base of objects hierarchy. Stores sha of the file, its type and content.
 */
public class GitObject {

    protected String sha;
    protected final String type;
    protected byte[] content;

    public GitObject(String type) {
        this.type = type;
    }
}
