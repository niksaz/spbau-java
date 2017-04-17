package ru.spbau.sazanovich.nikita.mygit.objects;

import org.junit.Test;

import static org.junit.Assert.*;

public class TreeEdgeTest {

    @Test
    public void isDirectory() throws Exception {
        final Tree.TreeEdge edge = new Tree.TreeEdge("hash", "git", Tree.TYPE);
        assertTrue(edge.isDirectory());
    }
}