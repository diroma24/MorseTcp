import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedReader in = null;
    private PrintWriter out = null;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (
                Socket socket = this.clientSocket;
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
        ) {
            System.out.println("Client connected.");
            String message;

            while((message = in.readLine()) != null) {
                System.out.println(message);
                if (message.equals("exit")) break;
            }
        } catch (IOException e) {
            System.err.println("Error occurred while trying to connect to client: " + e.getMessage());
        } finally{}
    }
}
