package ru.spbau.sazanovich.nikita.collections;

import org.junit.Test;

import static org.junit.Assert.*;

public class MyHashMapTest {

    @Test
    public void testSize() throws Exception {
        MyHashMap hashMap = new MyHashMap(4);
        hashMap.put("a", "123");
        hashMap.put("b", "123");
        hashMap.put("c", "123");
        assertEquals(hashMap.size(), 3);

        hashMap.put("c", "124");
        assertEquals(hashMap.size(), 3);

        hashMap.put("d", "123");
        assertEquals(hashMap.size(), 4);

        hashMap.remove("d");
        assertEquals(hashMap.size(), 3);
    }

    @Test
    public void testContains() throws Exception {
        MyHashMap hashMap = new MyHashMap();
        hashMap.put("a", "por");
        assertTrue(hashMap.contains("a"));
        assertFalse(hashMap.contains("d"));

        hashMap.put("a", "b");
        assertTrue(hashMap.contains("a"));

        hashMap.put("sun", "oracle");
        assertTrue(hashMap.contains("sun"));
    }

    @Test
    public void testGet() throws Exception {
        MyHashMap hashMap = new MyHashMap();

        String key = "a";
        String value = "night";

        hashMap.put(key, value);
        assertEquals(value, hashMap.get(key));

        value = "process";
        hashMap.put(key, value);
        assertEquals(value, hashMap.get(key));
    }

    @Test
    public void testPut() throws Exception {
        MyHashMap hashMap = new MyHashMap();
        assertNull(hashMap.put("pro", "soft"));
        assertEquals(hashMap.put("pro", "hard"), "soft");
    }

    @Test
    public void testRemove() throws Exception {
        MyHashMap hashMap = new MyHashMap();
        hashMap.put("pro", "soft");
        assertEquals(hashMap.remove("pro"), "soft");
        assertNull(hashMap.remove("pro"));
    }

    @Test
    public void testClear() throws Exception {
        MyHashMap hashMap = new MyHashMap();
        hashMap.put("hello", "world");
        hashMap.put("good", "morning");
        hashMap.put("happy", "Saturday");
        assertEquals(hashMap.get("hello"), "world");
        assertEquals(hashMap.get("good"), "morning");
        assertEquals(hashMap.get("happy"), "Saturday");

        hashMap.clear();
        assertEquals(hashMap.size(), 0);
        assertNull(hashMap.get("hello"));
        assertNull(hashMap.get("good"));
        assertNull(hashMap.get("happy"));
    }
}