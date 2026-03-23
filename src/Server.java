import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static ServerSocket serverSocket;
    private static ExecutorService pool = Executors.newFixedThreadPool(5);
    private static boolean isRunning = true;

    public static void main(String[] args) {
        int port = 9000;

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Starting server using port: " + port);

            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    pool.execute(new ClientHandler(clientSocket));
                } catch (SocketException e) {
                    if (!isRunning) {
                        System.out.println("Closing server.");
                    } else {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            stopServer();
        }
    }

    public static void stopServer() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        pool.shutdown();
    }
}
