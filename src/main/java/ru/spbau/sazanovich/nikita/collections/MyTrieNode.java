package ru.spbau.sazanovich.nikita.collections;

import java.util.HashMap;
import java.util.Set;

public class MyTrieNode {

    private boolean isTerminal = false;
    private int subtreeNodesCount = 0;
    private final HashMap<Character, MyTrieNode> childNodes = new HashMap<>();

    public MyTrieNode getChildWith(char key) {
        return childNodes.get(key);
    }

    public MyTrieNode putChildWith(char key, MyTrieNode value) {
        return childNodes.put(key, value);
    }

    public MyTrieNode removeChildWith(char key) {
        return childNodes.remove(key);
    }

    public Set<Character> getChildCharacters() {
        return childNodes.keySet();
    }

    public int getSubtreeNodesCount() {
        return subtreeNodesCount;
    }

    public void changeSubtreeNodesCount(int delta) {
        subtreeNodesCount += delta;
    }

    public boolean isTerminal() {
        return isTerminal;
    }

    public boolean setTerminal(boolean value) {
        boolean result = isTerminal;
        changeSubtreeNodesCount(isTerminal ? -1 : 0);
        isTerminal = value;
        changeSubtreeNodesCount(value ? +1 : 0);
        return result;
    }

}
