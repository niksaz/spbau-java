package ru.spbau.sazanovich.nikita.server;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class ServerTokenHandlerTest {

    @Test
    public void handleTokensFrom() throws Exception {
        ServerSaverFactory factory = new ServerSaverFactory();
        final ServerTokenHandler handler = new ServerTokenHandler(factory, mock(PrintStream.class));
        //noinspection unchecked
        Iterator<String> iterator = (Iterator<String>) mock(Iterator.class);
        when(iterator.next()).thenReturn("start").thenReturn("stop").thenReturn("start").thenReturn("exit");
        handler.handleTokensFrom(iterator);

        assertEquals(2, factory.servers.size());
        for (Server server : factory.servers) {
            verify(server, times(1)).start();
            verify(server, times(1)).stop();
        }
    }

    private static class ServerSaverFactory implements ServerFactory {

        @NotNull
        final List<Server> servers;

        ServerSaverFactory() {
            this.servers = new LinkedList<>();
        }

        @Override
        public Server createServer() {
            Server server = mock(Server.class);
            servers.add(server);
            return server;
        }
    }
}