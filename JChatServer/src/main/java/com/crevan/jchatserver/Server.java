package com.crevan.jchatserver;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * One creates a server socket connection.<br>
 * Looped, waiting for a client to connect the socket.<br>
 * Creates a new handler thread to exchange messages with the client.<br>
 * Waiting for the next client to connect.
 */

public class Server {

    private static final Map<String, Connection> CONNECTIONS_MAP = new ConcurrentHashMap<>();

    private static final Logger log = Logger.getLogger(Server.class);

    public static void main(String[] args) {
        ConsoleHelper.writeMessage("Enter port number:");
        int portNumber = ConsoleHelper.readInt();
        try (ServerSocket srvSocket = new ServerSocket(portNumber)) {
            log.info("Server started on port: " + portNumber);
            while (true) {
                new Handler(srvSocket.accept()).start();
            }
        } catch (IOException ioe) {
            log.error("Error" + ioe.getMessage());
        }
    }

    public static void sendBroadcastMessage(Message message) {
        for (Map.Entry<String, Connection> connectionEntry : CONNECTIONS_MAP.entrySet()) {
            try {
                connectionEntry.getValue().send(message);
            } catch (IOException e) {
                log.error("Message was not delivered: " + e.getMessage());
            }
        }
    }

    private static class Handler extends Thread {
        private final Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            Message receivedMessage;
            do {
                connection.send(new Message(MessageType.NAME_REQUEST));
                receivedMessage = connection.receive();
            } while (receivedMessage.getData() == null || receivedMessage.getMessageType() != MessageType.USER_NAME
                    || "".equals(receivedMessage.getData()) || CONNECTIONS_MAP.containsKey(receivedMessage.getData()));
            CONNECTIONS_MAP.put(receivedMessage.getData(), connection);
            connection.send(new Message(MessageType.NAME_ACCEPTED));
            log.info("User: " + receivedMessage.getData() + " connected.");
            return receivedMessage.getData();
        }

        private void notifyUsers(Connection connection, String userName) throws IOException {
            for (Map.Entry<String, Connection> entry : CONNECTIONS_MAP.entrySet()) {
                String clientName = entry.getKey();
                if (!clientName.equals(userName)) {
                    connection.send(new Message(MessageType.USER_ADDED, clientName));
                }
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getMessageType() == MessageType.TEXT) {
                    log.info(userName + " sent a message");
                    String textWithAuthor = userName + ": " + message.getData();
                    sendBroadcastMessage(new Message(MessageType.TEXT, textWithAuthor));
                }
            }
        }

        @Override
        public void run() {
            log.info("Connected to: " + socket.getRemoteSocketAddress());
            String userName = null;
            try (Connection connection = new Connection(socket)) {
                userName = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                notifyUsers(connection, userName);
                serverMainLoop(connection, userName);
            } catch (IOException | ClassNotFoundException e) {
                log.error("Error: " + e.getMessage());
            }
            if (userName != null) {
                try {
                    CONNECTIONS_MAP.remove(userName).close();
                } catch (IOException e) {
                    log.error("Error: " + e.getMessage());
                }
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
            }
            log.info("Connection with " + socket.getRemoteSocketAddress() + " closed");
        }
    }
}
