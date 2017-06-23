package network;

import application.exception.InternalServerErrorException;
import controller.UserController;
import dao.exception.DaoException;
import entity.User;
import message.Message;
import message.MessageType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * Handle one request by processing received message and sending response back to client's socket.
 */
public class RequestHandler implements Runnable {
    private static final Logger errorLogger = LogManager.getLogger("errorLogger");
    private static final Logger infoLogger = LogManager.getLogger("infoLogger");

    private Socket socket;

    public RequestHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

            Message message = (Message) ois.readObject();
            infoLogger.info("Message received");
            User user = null;
            MessageType messageType = MessageType.FAIL_MESSAGE;
            String messageString = "Internal server error occurred. Try again latter";

            switch (message.getMessageType()) {
                case LOGIN_MESSAGE:
                    try {
                        user = UserController.login(message.getUser());
                        if (user != null) {
                            messageType = MessageType.LOGIN_SUCCESS_MESSAGE;
                        } else {
                            messageString = "Wrong login or password";
                        }
                        break;
                    } catch (InternalServerErrorException | DaoException e) {
                        errorLogger.error(e.getStackTrace(), e);
                        break;
                    }
                case CREATE_MESSAGE:
                    try {
                        user = UserController.create(message.getUser());
                        if (user != null) {
                            messageType = MessageType.CREATE_SUCCESS_MESSAGE;
                        } else {
                            messageString = "User exists";
                        }
                        break;
                    } catch (InternalServerErrorException | DaoException e) {
                        errorLogger.error(e.getStackTrace(), e);
                        break;
                    }
                case UPDATE_MESSAGE:
                    try {
                        user = UserController.update(message.getUser());
                        if (user != null) {
                            messageType = MessageType.UPDATE_SUCCESS_MESSAGE;
                        } else {
                            messageString = "Update failed";
                        }
                        break;
                    } catch (InternalServerErrorException | DaoException e) {
                        errorLogger.error(e.getStackTrace(), e);
                        break;
                    }
                case FAIL_MESSAGE:
                    return;
                default:
                    messageString = "Received message have wrong type";
            }

            Message responseMessage = new Message(messageType, messageString, user);

            oos.writeObject(responseMessage);
            oos.flush();
            infoLogger.info("Response message sent");

        } catch (SocketException e) {
            // При закрытии клиентского приложения происходит обновление,
            // которое не дожидается ответа и закрывает сокет,
            // в результате чего выбрасывается исключение.
            // Однако, данное исключение не является ошибкой в работе сервера.
        } catch (IOException | ClassNotFoundException e) {
            errorLogger.error(e.getStackTrace(), e);
        }
    }
}