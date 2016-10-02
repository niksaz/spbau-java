package ru.spbau.sazanovich.nikita.collections;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MyTrie implements Trie, StreamSerializable {

    private MyTrieNode root = new MyTrieNode();

    @Override
    public void serialize(OutputStream out) throws IOException {
    }

    @Override
    public void deserialize(InputStream in) throws IOException {
    }

    @Override
    public boolean add(String element) {
        return recursive_add(root, element, 0);
    }

    @Override
    public boolean contains(String element) {
        MyTrieNode nodeForElement = nodeFor(element);
        return nodeForElement != null && nodeForElement.isTerminal();
    }

    @Override
    public boolean remove(String element) {
        return recursive_remove(root, element, 0);
    }

    @Override
    public int size() {
        return root.getSubtreeNodesCount();
    }

    @Override
    public int howManyStartsWithPrefix(String prefix) {
        MyTrieNode nodeForPrefix = nodeFor(prefix);
        return nodeForPrefix == null ? 0 : nodeForPrefix.getSubtreeNodesCount();
    }

    private MyTrieNode nodeFor(String prefix) {
        MyTrieNode currentNode = root;
        for (char transition : prefix.toCharArray()) {
            currentNode = currentNode.getChildWith(transition);
            if (currentNode == null) {
                return null;
            }
        }
        return currentNode;
    }

    private boolean recursive_add(MyTrieNode node, String element, int pos) {
        if (pos == element.length()) {
            return !node.setTerminal(true);
        }
        MyTrieNode nextNode = node.getChildWith(element.charAt(pos));
        if (nextNode == null) {
            nextNode = new MyTrieNode();
            node.putChildWith(element.charAt(pos), nextNode);
        }
        boolean result = recursive_add(nextNode, element, pos + 1);
        if (result) {
            node.changeSubtreeNodesCount(+1);
        }
        return result;
    }

    private boolean recursive_remove(MyTrieNode node, String element, int pos) {
        if (pos == element.length()) {
            return node.setTerminal(false);
        }
        MyTrieNode nextNode = node.getChildWith(element.charAt(pos));
        if (nextNode == null) {
            return false;
        }
        boolean result = recursive_remove(nextNode, element, pos + 1);
        if (result) {
            node.changeSubtreeNodesCount(-1);
            if (nextNode.getSubtreeNodesCount() == 0) {
                node.removeChildWith(element.charAt(pos));
            }
        }
        return result;
    }

}
