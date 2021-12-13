import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Server {

    private final Integer port = 9393;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private DataBaseHandler dbHandler;

    public Server(){
        log("Connecting to the DB");
        dbHandler = new DataBaseHandler();
        log("Starting server...");
        ServerSocket ss = null;

        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            log("Waiting for clients");

            while(true){
                Socket socket = ss.accept();
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());
                log("Client connected");
                login();
                while(true){
                    String action = this.receiveMessage();
                    if(action.equals("upload")){
                        Data d = (Data) this.receiveObject();
                        FileUtils.writeByteArrayToFile(new File(Paths.get("files", d.getFileName()).toString()), (byte[]) d.getData());
                        //save the data of the file in the db
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void login(){
        User toValidate = (User) this.receiveObject();
        User result = dbHandler.getUser(toValidate.getId());
        if (result!=null && toValidate.getId().equals(result.getId()) && toValidate.getPassword().equals(result.getPassword())){
            this.sendObject(result);
        }else{
            this.sendObject(null);
        }
    }

    public void sendMessage(String mes){
        try {
            oos.writeUTF(mes);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String receiveMessage(){
        try {
            String res = ois.readUTF();
            return res;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void sendObject(Object toSend){
        try {
            oos.writeObject(toSend);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Object receiveObject(){
        try {
            Object rec = ois.readObject();
            return rec;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void log(String men) {
        System.out.println(men);
    }

    public static void main(String[] args) {

        new Server();

    }

}