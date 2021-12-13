import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private final Integer port = 9393;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    public Server(){
        System.out.println("Starting server...");
        ServerSocket ss = null;

        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            System.out.println("Waiting for clients");

            while(true){
                Socket socket = ss.accept();
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());
                System.out.println("Client connected");
                String res = ois.readUTF();
                System.out.println(res);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {

        new Server();

    }

}