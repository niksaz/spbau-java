package ru.spbau.sazanovich.nikita.collections;

import org.junit.Test;

import java.io.*;

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

    @Test
    public void testSerialization() {
        final MyTrie trie = new MyTrie();
        final String s1 = "testSerialize";
        final String s2 = "serial";

        assertTrue(trie.add(s1));
        assertTrue(trie.add(s2));
        assertTrue(trie.contains(s1));
        assertTrue(trie.contains(s2));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            trie.serialize(out);
        } catch (IOException e) {
            fail(e.getMessage());
        }

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        try {
            trie.deserialize(in);
        } catch (IOException e) {
            fail(e.getMessage());
        }

        assertTrue(trie.contains(s1));
        assertTrue(trie.contains(s2));
    }

    @Test
    public void testDeletePrefix() {
        final MyTrie trie = new MyTrie();
        final String s = "pandemonium";
        final String prefix = s.substring(0, 6);

        assertTrue(trie.add(prefix));
        assertTrue(trie.add(s));

        assertTrue(trie.contains(prefix));
        assertFalse(trie.contains(s.substring(0, 7)));
        assertTrue(trie.contains(s));

        assertTrue(trie.remove(prefix));

        assertFalse(trie.contains(prefix));
        assertFalse(trie.contains(s.substring(0, 7)));
        assertTrue(trie.contains(s));
    }

    @Test
    public void testDoubleAdd() {
        final MyTrie trie = new MyTrie();
        final String s = "panda";

        assertTrue(trie.add(s));
        assertFalse(trie.add(s));

        assertEquals(1, trie.howManyStartsWithPrefix(s));
        assertEquals(1, trie.howManyStartsWithPrefix(s.substring(0, 3)));
    }

    @Test
    public void testDoubleRemove() {
        final MyTrie trie = new MyTrie();
        final String s = "ambiguity";

        assertTrue(trie.add(s));

        assertTrue(trie.remove(s));
        assertEquals(0, trie.howManyStartsWithPrefix(""));
        assertFalse(trie.remove(s));
        assertEquals(0, trie.howManyStartsWithPrefix(""));
    }

    @Test
    public void testRemoveStringNotInTrie() {
        final MyTrie trie = new MyTrie();
        final String s = "adhoc";
        final String prefix = s.substring(0, 2);

        assertTrue(trie.add(s));

        assertFalse(trie.remove(prefix));
        assertEquals(1, trie.howManyStartsWithPrefix(prefix));
    }

}