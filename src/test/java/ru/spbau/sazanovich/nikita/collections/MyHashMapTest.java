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
        assertEquals(3, hashMap.size());

        hashMap.put("c", "124");
        assertEquals(3, hashMap.size());

        hashMap.put("d", "123");
        assertEquals(4, hashMap.size());

        hashMap.remove("d");
        assertEquals(3, hashMap.size());
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
        assertEquals("soft", hashMap.put("pro", "hard"));
    }

    @Test
    public void testRemove() throws Exception {
        MyHashMap hashMap = new MyHashMap();
        hashMap.put("pro", "soft");
        assertEquals("soft", hashMap.remove("pro"));
        assertNull(hashMap.remove("pro"));
    }

    @Test
    public void testClear() throws Exception {
        MyHashMap hashMap = new MyHashMap();
        hashMap.put("hello", "world");
        hashMap.put("good", "morning");
        hashMap.put("happy", "Saturday");
        assertEquals("world", hashMap.get("hello"));
        assertEquals("morning", hashMap.get("good"));
        assertEquals("Saturday", hashMap.get("happy"));

        hashMap.clear();
        assertEquals(0, hashMap.size());
        assertNull(hashMap.get("hello"));
        assertNull(hashMap.get("good"));
        assertNull(hashMap.get("happy"));
    }
}