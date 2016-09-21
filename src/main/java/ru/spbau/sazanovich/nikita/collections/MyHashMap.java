package ru.spbau.sazanovich.nikita.collections;

/**
 * Custom implementation of HashMap. Keys are objects of String, values - String.<p>
 * This implementation provides constant-time performance for the basic operations (get and put),
 * assuming the hash function disperses the elements properly among the buckets.<p>
 *
 * An instance of HashMap has two parameters that affect its performance: initial capacity and load factor. The capacity
 * is the number of buckets in the hash table, and the initial capacity is simply the capacity at the time the hash
 * table is created. The load factor is a measure of how full the hash table is allowed to get before its capacity is
 * automatically increased. When the number of entries in the hash table exceeds the product of the load factor and
 * the current capacity, the hash table is rehashed (that is, internal data structures are rebuilt) so that
 * the hash table has twice the number of buckets.
 */
public class MyHashMap {

    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = .75f;

    private final float loadFactor;
    private int size;

    private MyList[] listsArray;

    public MyHashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    public MyHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public MyHashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity <= 0 || loadFactor <= 0) {
            throw new IllegalArgumentException();
        }
        listsArray = new MyList[initialCapacity];
        this.loadFactor = loadFactor;
    }

    public int size() {
        return size;
    }

    public boolean contains(String key) {
        int listPos = getStringBucket(key);
        return listsArray[listPos] != null && listsArray[listPos].contains(key);
    }

    public String get(String key) {
        int listPos = getStringBucket(key);
        return listsArray[listPos] == null ? null : listsArray[listPos].get(key);
    }

    public String put(String key, String value) {
        int listPos = getStringBucket(key);
        if (listsArray[listPos] == null) {
            listsArray[listPos] = new MyList();
        }
        String respond = listsArray[listPos].put(new MyHashMapEntry(key, value));
        if (respond == null) {
            size++;
            if (size > listsArray.length * loadFactor) {
                reconstruct();
            }
        }
        return respond;
    }

    public String remove(String key) {
        int listPos = getStringBucket(key);
        if (listsArray[listPos] == null) {
            return null;
        }
        String respond = listsArray[listPos].remove(key);
        if (respond != null) {
            size--;
        }
        return respond;
    }

    public void clear() {
        for (MyList list : listsArray) {
            if (list != null) {
                list.clear();
            }
        }
        size = 0;
    }

    private int getStringBucket(String key) {
        return key.hashCode() % listsArray.length;
    }

    private void reconstruct() {
        MyList[] newListsArray = new MyList[listsArray.length * 2];
        for (MyList list : listsArray) {
            while (!list.isEmpty()) {
                MyHashMapEntry entry = list.removeHead();
                int listPos = entry.getKey().hashCode() % newListsArray.length;
                if (newListsArray[listPos] == null) {
                    newListsArray[listPos] = new MyList();
                }
                newListsArray[listPos].put(entry);
            }
        }
        listsArray = newListsArray;
    }

}
