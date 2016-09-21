package ru.spbau.sazanovich.nikita.collections;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MyTrie implements Trie, StreamSerializable {

    @Override
    public void serialize(OutputStream out) throws IOException {

    }

    @Override
    public void deserialize(InputStream in) throws IOException {

    }

    @Override
    public boolean add(String element) {
        return false;
    }

    @Override
    public boolean contains(String element) {
        return false;
    }

    @Override
    public boolean remove(String element) {
        return false;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public int howManyStartsWithPrefix(String prefix) {
        return 0;
    }

}
