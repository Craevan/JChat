package com.crevan.jchat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
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
            return receivedMessage.getData();
        }

        private void notifyUsers(Connection connection, String userName) throws IOException {
            for (Map.Entry<String, Connection> entry : CONNECTIONS_MAP.entrySet()) {
                String clientName = entry.getKey();
                if (!clientName.equals(userName)) {
                    connection.send(new Message(MessageType.USER_ADDED, clientName));
                } else {
                    ConsoleHelper.writeMessage("Ошибка");
                }
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getMessageType() == MessageType.TEXT) {
                    String authorText = userName + ": " + message.getData();
                    sendBroadcastMessage(new Message(MessageType.TEXT, authorText));
                }
            }
        }

        @Override
        public void run() {
            ConsoleHelper.writeMessage("Установлено соединение с: " + socket.getRemoteSocketAddress());
            String userName = null;
            try (Connection connection = new Connection(socket)) {
                userName = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                notifyUsers(connection, userName);
                serverMainLoop(connection, userName);
            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Error: " + e.getMessage());
            }
            if (userName != null) {
                try {
                    CONNECTIONS_MAP.remove(userName).close();
                } catch (IOException e) {
                    ConsoleHelper.writeMessage("Error: " + e.getMessage());
                }
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
            }
            ConsoleHelper.writeMessage("Connection with " + socket.getRemoteSocketAddress() + " closed");
        }
    }

    private static final Map<String, Connection> CONNECTIONS_MAP = new ConcurrentHashMap<>();

    public static void sendBroadcastMessage(Message message) {
        for (Map.Entry<String, Connection> connectionEntry : CONNECTIONS_MAP.entrySet()) {
            try {
                connectionEntry.getValue().send(message);
            } catch (IOException e) {
                ConsoleHelper.writeMessage("Сообщение не было доставлено");
            }
        }
    }

    public static void main(String[] args) {
        ConsoleHelper.writeMessage("Введите номер порта:");
        int portNumber = ConsoleHelper.readInt();
        try (ServerSocket srvSocket = new ServerSocket(portNumber)) {
            ConsoleHelper.writeMessage("Сервер запущен");
            while (true) {
                new Handler(srvSocket.accept()).start();
            }
        } catch (IOException ioe) {
            ConsoleHelper.writeMessage(ioe.getMessage());
        }
    }
}
