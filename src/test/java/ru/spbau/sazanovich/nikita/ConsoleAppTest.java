package ru.spbau.sazanovich.nikita;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class ConsoleAppTest {

    @Test
    public void suffixArgsToList() throws Exception {
        final String arg1 = "thank you";
        final String arg2 = "gracias";
        final String arg3 = "mersi";
        final String[] args = {arg1, arg2, arg3};
        final List<String> suffixList = ConsoleApp.suffixArgsToList(args);
        assertEquals(Arrays.asList(arg2, arg3), suffixList);
    }

    @Test
    public void filterSubclass() throws Exception {
        final Object object1 = new Object();
        final Object object2 = "filtering";
        final Object object3 = new Date();
        final Object object4 = "subclass";
        final List<Object> objects = new ArrayList<>();
        objects.add(object1);
        objects.add(object2);
        objects.add(object3);
        objects.add(object4);
        final List<String> filtered = ConsoleApp.filterSubclass(objects, String.class);
        final Set<String> filteredSet = new HashSet<>(filtered);
        final Set<String> stringSet = new HashSet<>();
        stringSet.add((String) object2);
        stringSet.add((String) object4);
        assertEquals(stringSet, filteredSet);
    }

}