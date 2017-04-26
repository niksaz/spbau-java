package ru.spbau.sazanovich.nikita.client;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class ClientTokenHandlerTest {

    @Test
    public void handleTokensFrom() throws Exception {
        ClientSaverFactory factory = new ClientSaverFactory();
        ClientTokenHandler handler = new ClientTokenHandler(factory, mock(PrintStream.class));
        //noinspection unchecked
        Iterator<String> iterator = (Iterator<String>) mock(Iterator.class);
        when(iterator.next())
                .thenReturn("list").thenReturn("/Users")
                .thenReturn("get").thenReturn("/from").thenReturn("/to")
                .thenReturn("exit");
        handler.handleTokensFrom(iterator);

        assertEquals(1, factory.clients.size());
        Client client = factory.clients.get(0);
        verify(client, times(1)).list("/Users");
        verify(client, times(1)).get("/from", "/to");
    }

    private static class ClientSaverFactory implements ClientFactory {

        @NotNull
        final List<Client> clients;

        ClientSaverFactory() {
            this.clients = new LinkedList<>();
        }

        @Override
        public Client createClient() {
            Client client = mock(Client.class);
            clients.add(client);
            return client;
        }
    }
}