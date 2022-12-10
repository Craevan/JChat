package com.crevan.jchatserver;

/**
 * Communication protocol between client and server.<br>
 *
 * As a response on a client's connection attempt server requests a username.<br>
 * Client sends filled in "username" request back to the server.<br>
 * Server has to accept the "username" or declare it and repeat request.<br>
 * Server informs connected clients about last connected client.<br>
 * Server informs connected clients about last disconnected client.<br>
 * Every message received from a connected client is forwarded to all clients currently connected to the server.<br>
 */

public enum MessageType {
    NAME_REQUEST,
    USER_NAME,
    NAME_ACCEPTED,
    TEXT,
    USER_ADDED,
    USER_REMOVED
}
