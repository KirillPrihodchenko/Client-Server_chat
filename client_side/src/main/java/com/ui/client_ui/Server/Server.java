package com.ui.client_ui.Server;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private List<ServerThread> clients;

    public Server() {
        clients = new ArrayList<>();
    }

    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(8888);
            System.out.println("Server started on port 8888.");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ServerThread serverThread = new ServerThread(clientSocket, this);
                clients.add(serverThread);
                serverThread.start();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeClient(ServerThread client) {
        clients.remove(client);
    }

    public void broadcast(String message) {
        for (ServerThread client : clients) {
            client.sendMessage(message);
        }
    }

    public List<String> getUserList() {
        List<String> userList = new ArrayList<>();
        for (ServerThread client : clients) {
            userList.add(client.getNickname());
        }
        return userList;
    }

    static class ServerThread extends Thread {
        private Socket clientSocket;
        private Server server;
        private PrintWriter out;
        private BufferedReader in;
        private String nickname;

        public ServerThread(Socket clientSocket, Server server) {
            this.clientSocket = clientSocket;
            this.server = server;
        }

        public String getNickname() {
            return nickname;
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                out.println("Enter nickname:");
                nickname = in.readLine();
                server.broadcast(nickname + " has joined the server.");

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    server.broadcast(nickname + ": " + inputLine);
                }

                server.broadcast(nickname + " has left the server.");
                server.removeClient(this);
                out.close();
                in.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}
