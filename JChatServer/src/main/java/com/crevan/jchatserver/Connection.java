package com.crevan.jchatserver;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * The Connection class acts as a wrapper over the java.net.Socket class,
 * which has to be able to serialize and deserialize objects of type Message into a socket.
 */

public class Connection implements Closeable {

    private final Socket socket;

    private final ObjectOutputStream out;

    private final ObjectInputStream in;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    public void send(Message message) throws IOException {
        synchronized (out) {
            out.writeObject(message);
        }
    }

    public Message receive() throws IOException, ClassNotFoundException {
        Message message;
        synchronized (in) {
            message = (Message) in.readObject();
        }
        return message;
    }

    @Override
    public void close() throws IOException {
        socket.close();
        out.close();
        in.close();
    }
}
