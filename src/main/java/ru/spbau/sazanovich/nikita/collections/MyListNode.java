package ru.spbau.sazanovich.nikita.collections;

public class MyListNode {

    private MyListNode next = null;

    private MyHashMapEntry entry;

    public MyListNode(MyHashMapEntry entry) {
        this.entry = entry;
    }

    public MyListNode getNext() {
        return next;
    }

    public void setNext(MyListNode next) {
        this.next = next;
    }

    public MyHashMapEntry getEntry() {
        return entry;
    }

}
