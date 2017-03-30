package ru.spbau.sazanovich.nikita;

import org.jetbrains.annotations.NotNull;

public class Main {

    public static void main(@NotNull String[] args) {
        if (args.length == 0) {
            System.out.println("you should enter at least one file path");
            return;
        }
        final MD5Util hasher = new MD5Util();
        final MD5Util concurrentHasher = new MD5Util(4);
        for (String arg : args) {
            System.out.println(arg + ":");
            try {
                long start = System.currentTimeMillis();
                System.out.println(hasher.getHashFromFile(arg));
                System.out.printf("Computed in single-threaded environment in %d msecs\n",
                                   System.currentTimeMillis() - start);

                start = System.currentTimeMillis();
                System.out.println(concurrentHasher.getHashFromFile(arg));
                System.out.printf("Computed in concurrent environment in %d msecs\n",
                                   System.currentTimeMillis() - start);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
