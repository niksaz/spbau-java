package ru.spbau.sazanovich.nikita.collections;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

public class MyTrie implements Trie, StreamSerializable {

    private static final int CNT_MASK = (1 << 6) - 1;
    private static final int IS_TERMINAL_MASK = 1 << 6;

    private MyTrieNode root = new MyTrieNode();

    @Override
    public void serialize(OutputStream out) throws IOException {
        recursive_serialize(root, out);
    }

    @Override
    public void deserialize(InputStream in) throws IOException {
        recursive_deserialize(root = new MyTrieNode(), in);
    }

    @Override
    public boolean add(String element) {
        return recursive_add(root, element, 0);
    }

    @Override
    public boolean contains(String element) {
        final MyTrieNode nodeForElement = nodeFor(element);
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
        final MyTrieNode nodeForPrefix = nodeFor(prefix);
        return nodeForPrefix == null ? 0 : nodeForPrefix.getSubtreeNodesCount();
    }

    private void recursive_serialize(MyTrieNode node, OutputStream out) throws IOException {
        final Set<Character> keys = node.getChildCharacters();
        out.write(keys.size() + (node.isTerminal() ? IS_TERMINAL_MASK : 0));
        for (char transition : keys) {
            out.write(transition);
            recursive_serialize(node.getChildWith(transition), out);
        }
    }

    private MyTrieNode recursive_deserialize(MyTrieNode node, InputStream in) throws IOException {
        final int node_info_byte = in.read();
        final int cnt = node_info_byte & CNT_MASK;
        node.setTerminal((node_info_byte & IS_TERMINAL_MASK) > 0);
        for (int i = 0; i < cnt; i++) {
            final char transition = (char) in.read();
            final MyTrieNode nextNode = new MyTrieNode();
            node.putChildWith(transition, nextNode);
            recursive_deserialize(nextNode, in);
            node.changeSubtreeNodesCount(nextNode.getSubtreeNodesCount());
        }
        return null;
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
        final MyTrieNode nextNode = node.getChildWith(element.charAt(pos));
        if (nextNode == null) {
            return false;
        }
        final boolean result = recursive_remove(nextNode, element, pos + 1);
        if (result) {
            node.changeSubtreeNodesCount(-1);
            if (nextNode.getSubtreeNodesCount() == 0) {
                node.removeChildWith(element.charAt(pos));
            }
        }
        return result;
    }

}
