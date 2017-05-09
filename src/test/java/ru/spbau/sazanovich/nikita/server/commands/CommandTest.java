package ru.spbau.sazanovich.nikita.server.commands;

import org.junit.Test;

import static org.junit.Assert.*;

public class CommandTest {

    @Test
    public void networkCommunicationCodes() throws Exception {
        //noinspection ConstantConditions
        assertTrue(ListCommand.CODE != GetCommand.CODE);
    }
}