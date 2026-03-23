import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import static java.lang.System.out;

public class Client {
    private static String serverIP = "127.0.0.1";
    private static int serverPort = 9000;
    private static final MorseTranslator translator = new MorseTranslator();

    public static void main(String[] args) {
        int maxTry = 15;
        int waitTime = 10000;
        int tryCounter = 0;
        boolean done = false;
        Socket socket = null;

        while (!done && tryCounter < maxTry) {
            try {
                socket = new Socket(serverIP, serverPort);
                done = true;
            } catch (UnknownHostException e) {
                System.err.println("Unknown host: " + serverIP + ":" + serverPort);
            } catch (IOException e) {
                System.err.println("Offline server: " + e.getMessage());
                tryCounter++;

                if (tryCounter < maxTry) {
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ex) {
                        System.err.println("Sleep interrupted.");
                    }
                }
            }
        }

        if (!done) {
            System.err.println("No able to connect to " + serverIP + ":" + serverPort);
            return;
        }

        try (
                Socket clientSocket = socket;
                Scanner sc = new Scanner(System.in);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {
            IOLoop(sc,in,out);
        } catch (IOException e) {
            System.err.println("Error");
        }
    }

    private static void IOLoop(Scanner sc, BufferedReader in, PrintWriter out){
        Thread readingThread = new Thread(() -> {
            try{
                String serverMessage;
                while ((serverMessage = in.readLine()) != null){
                    System.out.print("\r" + serverMessage + "\n> ");
                    if (serverMessage.startsWith("Morse: ")){
                        String code = serverMessage.substring(7);
                        translator.playMorse(code);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error while reading server message: " + e.getMessage());
            }
        });

        readingThread.setDaemon(true);
        readingThread.start();

        while (true){
            System.out.print("> ");
            if (!sc.hasNextLine()) break;

            String message = sc.nextLine();
            if (message.trim().isEmpty()) continue;

            out.println(message);

            if (message.trim().equals("exit")) break;
        }
    }
}
