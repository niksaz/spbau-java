package ru.spbau.sazanovich.nikita.collections;

public class MyHashMapEntry {

    private String key;
    private String value;

    public MyHashMapEntry(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
