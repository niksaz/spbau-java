package ru.spbau.sazanovich.nikita.server.commands;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import org.jetbrains.annotations.NotNull;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Interface which represents server commands for requests.
 */
public abstract class Command {

    @NotNull
    public abstract byte[] execute() throws UnsuccessfulCommandExecution;

    public static byte[] errorResponse() {
        try (ByteOutputStream byteStream = new ByteOutputStream();
             DataOutputStream outputStream = new DataOutputStream(byteStream)
        ) {
            outputStream.writeInt(-1);
            outputStream.flush();
            return byteStream.getBytes();
        } catch (IOException e) {
            throw new IllegalStateException("Exception in writing to ByteOutputStream");
        }
    }
}
