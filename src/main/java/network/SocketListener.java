package network;

import message.Message;
import message.MessageType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * At start opens ServerSocket and delegate incoming connections to RequestHandler,
 * which executing in separate thread from thread pool.
 */
public class SocketListener extends Thread {
    private static final Logger infoLogger = LogManager.getLogger("infoLogger");
    private static final Logger errorLogger = LogManager.getLogger("errorLogger");

    private boolean serverStopping = false;
    private ExecutorService pool;
    @Override
    public void run() {
        pool = new ThreadPoolExecutor(
                8, 64, 60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(256));

        try (ServerSocket listener = new ServerSocket(5555)) {
            infoLogger.info("Server socket ready");
            while (!serverStopping) {
                Socket socket = listener.accept();
                RequestHandler requestHandler = new RequestHandler(socket);
                pool.submit(requestHandler);
            }
        } catch (IOException e) {
            errorLogger.error("Socket connection error.", e);
        }
    }

    public void close() {
        serverStopping = true;
        try (
                Socket socket = new Socket("localhost", 5555);
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())
        ) {
            Message message = new Message(MessageType.FAIL_MESSAGE, null, null);

            oos.writeObject(message);
            oos.flush();
            Thread.sleep(10);
        } catch (IOException e) {
            errorLogger.error("Can't send stop message", e);
        } catch (InterruptedException e) {
            errorLogger.error(e.getStackTrace(), e);
        }
        pool.shutdown();

        infoLogger.info("Server socket closed");
    }
}
