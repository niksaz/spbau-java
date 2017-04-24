package ru.spbau.sazanovich.nikita.server.commands;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Interface which represents server commands for requests.
 */
public abstract class Command {

    /**
     * Executes the command and converts its output to byte[] array.
     *
     * @return response's bytes
     * @throws UnsuccessfulCommandExecutionException if command can not be processed correctly
     */
    @NotNull
    public abstract byte[] execute() throws UnsuccessfulCommandExecutionException;

    /**
     * Converts {@code -1} to byte array as an unsuccessful response code.
     *
     * @return -1's bytes
     */
    @NotNull
    public static byte[] errorResponseBytes() {
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             DataOutputStream outputStream = new DataOutputStream(byteStream)
        ) {
            outputStream.writeInt(-1);
            outputStream.flush();
            return byteStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Exception in writing to ByteOutputStream");
        }
    }
}
