package ru.spbau.sazanovich.nikita.mygit.utils;

import org.junit.Test;
import ru.spbau.sazanovich.nikita.mygit.exceptions.MyGitIllegalArgumentException;

import static org.junit.Assert.assertEquals;

public class SHA1HashingTest {

    private static final Object TEST_OBJECT = "People have broken SHA-1 in practice.";

    @Test
    public void SHA1PartsTest() throws Exception {
        final String hash = SHA1Hasher.getHashFromObject(TEST_OBJECT);
        final SHA1Parts parts = new SHA1Parts(hash);
        assertEquals(hash.substring(0, 2), parts.getFirst());
        assertEquals(hash.substring(2), parts.getLast());
    }

    @Test(expected = MyGitIllegalArgumentException.class)
    public void SHA1PartsCrashTest() throws Exception {
        final String hash = SHA1Hasher.getHashFromObject(TEST_OBJECT);
        new SHA1Parts(hash.substring(1));
    }

    @Test
    public void SHA1HasherTest() throws Exception {
        final String firstHash = SHA1Hasher.getHashFromObject(TEST_OBJECT);
        final String secondHash = SHA1Hasher.getHashFromObject(TEST_OBJECT);
        assertEquals(firstHash, secondHash);

    }
}