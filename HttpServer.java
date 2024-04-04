import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {
    private int port;
    private final String STATIC_DIR = "static";

    public HttpServer(int port) {
        this.port = port;
    }
    // En anglais c est mieux , je vais changer le francais et traduire en anglais
    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server is running on port " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                new HttpRequestHandler(socket, STATIC_DIR).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}