package ru.spbau.sazanovich.nikita.collections;

import org.junit.Test;

import static org.junit.Assert.*;

public class MyTrieTest {

    @Test
    public void testTypicalAlpha() {
        final MyTrie trie = new MyTrie();
        final String add = "testTypicalAlpha";
        final String random = "random";

        assertTrue(trie.add(add));

        assertTrue(trie.contains(add));
        assertFalse(trie.contains(random));

        assertEquals(1, trie.size());

        assertFalse(trie.remove(random));
        assertTrue(trie.remove(add));

        assertEquals(0, trie.size());

        assertFalse(trie.contains(add));
    }

    @Test
    public void testTypicalBeta() {
        final MyTrie trie = new MyTrie();
        final String add = "testTypicalBeta";

        assertTrue(trie.add(add));

        assertTrue(trie.contains(add));
        assertFalse(trie.contains(add.substring(0, 4)));

        assertEquals(1, trie.howManyStartsWithPrefix(add));
        assertEquals(1, trie.howManyStartsWithPrefix(add.substring(0, 1)));
        assertEquals(1, trie.howManyStartsWithPrefix(add.substring(0, 4)));
        assertEquals(0, trie.howManyStartsWithPrefix(add.substring(4)));

        assertTrue(trie.remove(add));

        assertEquals(0, trie.size());

        assertEquals(0, trie.howManyStartsWithPrefix(add));
        assertEquals(0, trie.howManyStartsWithPrefix(add.substring(0, 1)));
        assertEquals(0, trie.howManyStartsWithPrefix(add.substring(0, 4)));
        assertEquals(0, trie.howManyStartsWithPrefix(add.substring(4)));
    }

}