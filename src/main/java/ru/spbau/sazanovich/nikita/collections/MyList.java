package ru.spbau.sazanovich.nikita.collections;

public class MyList {

    private MyListNode head = null;

    public boolean contains(String key) {
        return get(key) != null;
    }

    public String get(String key) {
        for (MyListNode it = head; it != null; it = it.getNext()) {
            if (it.getEntry().getKey().equals(key)) {
                return it.getEntry().getValue();
            }
        }
        return null;
    }

    public String put(MyHashMapEntry entry) {
        for (MyListNode it = head; it != null; it = it.getNext()) {
            if (it.getEntry().getKey().equals(entry.getKey())) {
                String respond = it.getEntry().getValue();
                it.getEntry().setValue(entry.getValue());
                return respond;
            }
        }

        MyListNode newNode = new MyListNode(entry);
        newNode.setNext(head);
        head = newNode;
        return null;
    }

    public String remove(String key) {
        MyListNode last = null;
        MyListNode it = head;
        while (it != null) {
            if (it.getEntry().getKey().equals(key)) {
                String respond = it.getEntry().getValue();
                if (last == null) {
                    head = it.getNext();
                } else {
                    last.setNext(it.getNext());
                }
                return respond;
            }
            last = it;
            it = it.getNext();
        }
        return null;
    }

    public void clear() {
        head = null;
    }

    public MyHashMapEntry removeHead() {
        MyHashMapEntry entry = head.getEntry();
        head = head.getNext();
        return entry;
    }

    public boolean isEmpty() {
        return head == null;
    }

}
