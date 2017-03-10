package ru.spbau.sazanovich.nikita.mygit.utils;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.exceptions.MyGitFilesystemException;
import ru.spbau.sazanovich.nikita.mygit.exceptions.MyGitStateException;
import ru.spbau.sazanovich.nikita.mygit.objects.Tree;

import java.io.*;
import java.nio.file.Path;

/**
 * Utils method to serialize Java {@link java.io.Serializable} classes.
 */
public class Mapper {

    @NotNull
    private final Path myGitDirectory;

    public Mapper(@NotNull Path path) {
        this.myGitDirectory = path;
    }

    @NotNull
    public String map(@NotNull Object object) throws MyGitFilesystemException, IOException {
        final String hash = Hasher.getHashFromObject(object);
        final String directoryName = myGitDirectory + "/.mygit/objects/" + hash.substring(0, 2);
        final String fileName = directoryName + "/" + hash.substring(2);
        boolean createdSuccessfully = new File(directoryName).mkdir();
        if (!createdSuccessfully) {
            throw new MyGitFilesystemException("could not create " + fileName);
        }
        final File objectFile = new File(fileName);
        //noinspection ResultOfMethodCallIgnored
        objectFile.createNewFile();
        try (FileOutputStream fileOutputStream = new FileOutputStream(objectFile);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)
        ) {
            objectOutputStream.writeObject(object);
        }
        return hash;
    }

    //@NotNull
    public Tree getHeadTree() throws MyGitStateException, IOException {
        final File headFile = new File(myGitDirectory + "/.mygit/HEAD");
        if (!headFile.exists()) {
            throw new MyGitStateException("could not find " + headFile.getAbsolutePath());
        }
        String headType;
        String headPath;
        try (FileInputStream fileInputStream = new FileInputStream(headFile);
             InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
             BufferedReader reader = new BufferedReader(inputStreamReader)
        ) {
            headType = reader.readLine();
            headPath = reader.readLine();
            if (headType == null || headPath == null) {
                throw new MyGitStateException("corrupted HEAD file");
            }
        }
        System.out.println("HEAD HASH " + headType + " " + headPath);
        return null;
    }
}
