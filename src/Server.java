import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static void main(String[] args) {
        int port = 9000;
        ExecutorService pool = Executors.newFixedThreadPool(5);

        //Creación del socket
        try(ServerSocket serverSocket = new ServerSocket(port)){
            while(true){
                Socket clientSocket = serverSocket.accept();
                pool.execute(new ClientHandler(clientSocket));
            }
        }catch(IOException e){
            System.err.println("Server error: " + e.getMessage());
        }finally {
            pool.shutdown();
        }
    }
}
