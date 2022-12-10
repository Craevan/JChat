package com.crevan.jchatclient;

import com.crevan.jchatserver.Connection;
import com.crevan.jchatserver.ConsoleHelper;
import com.crevan.jchatserver.Message;
import com.crevan.jchatserver.MessageType;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.Socket;

/**
 * The client must request for a server address and port each time client is launched.<br>
 * Client receives a username request from the server right after connection was approved.<br>
 * Client responds with a username and stands by until the response is accepted by the server.<br>
 */

public class Client {

    private static final Logger log = Logger.getLogger(Client.class);
    protected Connection connection;

    private volatile boolean clientConnected = false;

    public void run() {
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        try {
            synchronized (this) {
                wait();
            }
        } catch (InterruptedException e) {
            log.error("Thread waiting error. " + e.getMessage());
            return;
        }
        if (clientConnected) {
            log.info("Connection success. Type 'exit' to exit.");
        } else {
            log.error("An error occurred.");
        }
        while (clientConnected) {
            String message = ConsoleHelper.readString();
            if ("exit".equalsIgnoreCase(message)) {
                break;
            }
            if (shouldSendTextFromConsole()) {
                sendTextMessage(message);
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    protected String getServerAddress() {
        ConsoleHelper.writeMessage("Enter server address:");
        return ConsoleHelper.readString();
    }

    protected int getServerPort() {
        ConsoleHelper.writeMessage("Enter server port:");
        return ConsoleHelper.readInt();
    }

    protected String getUserName() {
        ConsoleHelper.writeMessage("Enter username:");
        return ConsoleHelper.readString();
    }

    protected boolean shouldSendTextFromConsole() {
        return true;
    }

    protected SocketThread getSocketThread() {
        return new SocketThread();
    }

    protected void sendTextMessage(String text) {
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            log.error("Error: " + e.getMessage());
            clientConnected = false;
        }
    }

    public class SocketThread extends Thread {
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            log.info(userName + " Connected to chat");
            ConsoleHelper.writeMessage(userName + " Connected to chat");
        }

        protected void informAboutDeletingNewUser(String userName) {
            log.info(userName + " Disconnected from chat");
            ConsoleHelper.writeMessage(userName + " Disconnected from chat");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this) {
                Client.this.notify();
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getMessageType() == MessageType.NAME_REQUEST) {
                    String userName = Client.this.getUserName();
                    connection.send(new Message(MessageType.USER_NAME, userName));
                } else if (message.getMessageType() == MessageType.NAME_ACCEPTED) {
                    notifyConnectionStatusChanged(true);
                    return;
                } else {
                    log.debug("Unexpected MessageType");
                    throw new IOException("Unexpected MessageType");
                }
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getMessageType() == MessageType.TEXT) {
                    processIncomingMessage(message.getData());
                } else if (message.getMessageType() == MessageType.USER_ADDED) {
                    informAboutAddingNewUser(message.getData());
                } else if (message.getMessageType() == MessageType.USER_REMOVED) {
                    informAboutDeletingNewUser(message.getData());
                } else {
                    log.debug("Unexpected MessageType");
                    throw new IOException("Unexpected MessageType");
                }
            }
        }

        @Override
        public void run() {
            String address = getServerAddress();
            int port = getServerPort();
            try {
                Socket socket = new Socket(address, port);
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
                log.error("Error: " + e.getMessage());
            }
        }
    }
}
