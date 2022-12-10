package com.crevan.jchatserver;

import java.io.Serializable;

/**
 * Message is data that client and server exchange with.<br>
 * Each message must be of MessageType type.<br>
 * An additional data, such as text message, must contain text.<br>
 */

public class Message implements Serializable {
    private final MessageType messageType;

    private final String data;

    public Message(MessageType messageType) {
        this(messageType, null);
    }

    public Message(MessageType messageType, String data) {
        this.messageType = messageType;
        this.data = data;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public String getData() {
        return data;
    }
}
