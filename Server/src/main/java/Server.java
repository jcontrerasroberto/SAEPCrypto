import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Server {

    private final Integer port = 9393;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private DataBaseHandler dbHandler;
    private DigitalSignature digitalSignature;

    public Server(){
        log("Connecting to the DB");
        dbHandler = new DataBaseHandler();
        digitalSignature = new DigitalSignature();
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
                    if(action.equals("upload")) receiveNote();
                    if (action.equals("listUnauthorizedNotes")) listUnauthorizedNotes();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void listUnauthorizedNotes() {
        ArrayList<Data> notes = new ArrayList<>();
        notes = dbHandler.getUnauthorizedNotes();
        this.sendObject(notes);
        String file = this.receiveMessage();
        Data toSend = dbHandler.getNote(file);

        byte[] fileBytes = new byte[0];
        try {
            fileBytes = Files.readAllBytes(Paths.get("files", toSend.getFileName()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        toSend.setData(fileBytes);

        this.sendObject(toSend);

        Data authorizedData = (Data) this.receiveObject();
    }

    public void receiveNote() throws IOException {
        Data d = (Data) this.receiveObject();
        if (digitalSignature.verifySignature(d, false)){
            FileUtils.writeByteArrayToFile(new File(Paths.get("files", d.getFileName()).toString()), (byte[]) d.getData());
            //save the data of the file in the db
        }else{
            log("Archivo corrupto");
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