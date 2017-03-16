package ru.spbau.sazanovich.nikita.mygit.utils;

import org.junit.Test;
import ru.spbau.sazanovich.nikita.mygit.exceptions.MyGitIllegalArgumentException;
import ru.spbau.sazanovich.nikita.mygit.utils.MyGitHasher.HashParts;

import static org.junit.Assert.assertEquals;

public class SHA1HashingTest {

    private static final Object TEST_OBJECT = "People have broken SHA-1 in practice.";
    private static final MyGitHasher HASHER = new SHA1Hasher();

    @Test
    public void SHA1PartsTest() throws Exception {
        final String hash = HASHER.getHashFromObject(TEST_OBJECT);
        final HashParts parts = HASHER.splitHash(hash);
        assertEquals(hash.substring(0, 2), parts.getFirst());
        assertEquals(hash.substring(2), parts.getLast());
    }

    @Test(expected = MyGitIllegalArgumentException.class)
    public void SHA1PartsCrashTest() throws Exception {
        final String hash = HASHER.getHashFromObject(TEST_OBJECT);
        HASHER.splitHash(hash.substring(1));
    }

    @Test
    public void SHA1HasherTest() throws Exception {
        final String firstHash = HASHER.getHashFromObject(TEST_OBJECT);
        final String secondHash = HASHER.getHashFromObject(TEST_OBJECT);
        assertEquals(firstHash, secondHash);
    }
}