package message;

import entity.User;

import java.io.Serializable;

public class Message implements Serializable {

    public static final long serialVersionUID = 125L;

    private MessageType messageType;
    private String message;
    private User user;

    public Message(MessageType messageType, String message, User user) {
        this.messageType = messageType;
        this.message = message;
        this.user = user;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageType=" + messageType +
                ", user=" + user +
                '}';
    }
}
