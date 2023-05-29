package com.ui.client_ui.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Server {

    private static final int PORT = 8888;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private ByteBuffer buffer;
    private List<String> users;

    public Server() {
        users = new ArrayList<>();
    }

    public void start() throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        ServerSocket serverSocket = serverSocketChannel.socket();
        serverSocket.bind(new InetSocketAddress(PORT));

        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server started on port " + PORT);

        buffer = ByteBuffer.allocate(1024);

        while (true) {
            selector.select();

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                keyIterator.remove();

                if (key.isAcceptable()) {
                    handleAccept(key);
                } else if (key.isReadable()) {
                    handleRead(key);
                }
            }
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
        System.out.println("New connection: " + clientChannel.getRemoteAddress());
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        buffer.clear();
        int bytesRead = clientChannel.read(buffer);

        if (bytesRead == -1) {
            // Client closed connection
            String clientAddress = clientChannel.getRemoteAddress().toString();
            System.out.println("Connection closed by client: " + clientAddress);
            removeUser(clientAddress);
            clientChannel.close();
            return;
        }

        buffer.flip();
        String request = new String(buffer.array(), 0, bytesRead).trim();
        System.out.println("Received request from client: " + request);

        // Process request and send response
        String response = processRequest(request);
        ByteBuffer responseBuffer = ByteBuffer.wrap(response.getBytes());
        clientChannel.write(responseBuffer);
    }

    private String processRequest(String request) {
        // Assuming request is the username sent by the client
        addUser(request);
        return "Welcome, " + request + "!";
    }

    private synchronized void addUser(String username) {
        users.add(username);
        System.out.println("User added: " + username);
    }

    private synchronized void removeUser(String username) {
        users.remove(username);
        System.out.println("User removed: " + username);
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.start();
    }
}
