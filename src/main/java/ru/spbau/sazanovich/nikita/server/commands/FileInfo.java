package ru.spbau.sazanovich.nikita.server.commands;

import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static ru.spbau.sazanovich.nikita.server.commands.FileInfo.FileType.DIRECTORY;
import static ru.spbau.sazanovich.nikita.server.commands.FileInfo.FileType.FILE;

/**
 * Class which represents a file info that is returned from a server to a client.
 */
public class FileInfo {

    @NotNull
    private final String name;

    @NotNull
    private final FileType type;

    /**
     * Constructs info about given file.
     *
     * @param path file
     */
    public FileInfo(@NotNull Path path) {
        this(path.getFileName().toString(), FileType.forFile(path));
    }

    private FileInfo(@NotNull String name, @NotNull FileType type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Returns file's name.
     *
     * @return file's name
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Utility method for checking whether the file is a directory.
     *
     * @return {@code true} if it is a directory; {@code false} otherwise
     */
    public boolean isDirectory() {
        return type.equals(DIRECTORY);
    }

    /**
     * Checks whether this object is equal to another.
     * They are equal iff name and type are the same.
     *
     * @param o another object
     * @return {@code true} if equal; {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileInfo info = (FileInfo) o;

        return name.equals(info.name) && type == info.type;
    }

    /**
     * Computes hash code using name and type.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    /**
     * Converts info to {@link String}. Puts filename
     * and a filesystem-dependent separator after it if it is a directory.
     *
     * @return string representation
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(name);
        if (isDirectory()) {
            builder.append(File.separatorChar);
        }
        return builder.toString();
    }

    void writeTo(@NotNull DataOutputStream outputStream) throws IOException {
        outputStream.writeUTF(name);
        outputStream.writeBoolean(type.equals(DIRECTORY));
    }

    static FileInfo readFrom(@NotNull DataInputStream inputStream) throws IOException {
        String name = inputStream.readUTF();
        boolean isDirectory = inputStream.readBoolean();
        FileType type = isDirectory ? DIRECTORY : FILE;
        return new FileInfo(name, type);
    }

    /**
     * Enum which stands for file's type in a filesystem.
     */
    public enum FileType {

        /**
         * Represents a directory in a filesystem.
         */
        DIRECTORY,
        /**
         * Represents a file in a filesystem.
         */
        FILE;

        /**
         * Gets {@link FileType} for a given file.
         *
         * @param path file
         * @return type for the given file
         */
        public static FileType forFile(@NotNull Path path) {
            return Files.isDirectory(path) ? DIRECTORY : FILE;
        }
    }
}
