package ru.spbau.sazanovich.nikita.mygit.objects;

import org.junit.Test;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CommitTest {

    @Test
    public void equalsTest() throws Exception {
        final Commit commit1 = new Commit("treeHash1");
        sleep(100);
        final Commit commit2 = new Commit("treeHash1");
        assertFalse(commit1.equals(commit2));
    }

    @Test
    public void compareToTest() throws Exception {
        final Commit commit1 = new Commit("treeHash1");
        final Commit commit2 = new Commit("treeHash2");
        sleep(100);
        final Commit commit3 = new Commit("treeHash3");
        assertTrue(commit1.compareTo(commit2) < 0);
        assertTrue(commit2.compareTo(commit3) < 0);
        assertTrue(commit1.compareTo(commit3) < 0);
        assertFalse(commit2.compareTo(commit1) < 0);
        assertFalse(commit3.compareTo(commit2) < 0);
        assertFalse(commit3.compareTo(commit1) < 0);
    }

}