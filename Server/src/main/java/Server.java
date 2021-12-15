import org.apache.commons.io.FileUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class Server {

    private final Integer port = 9393;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private DataBaseHandler dbHandler;
    private DigitalSignature digitalSignature;
    private BlockCipher blockCipher;

    public Server() {
        log("Connecting to the DB");
        dbHandler = new DataBaseHandler();
        digitalSignature = new DigitalSignature();
        blockCipher = new BlockCipher();
        log("Starting server...");
        ServerSocket ss = null;

        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            log("Waiting for clients");

            while (true) {
                Socket socket = ss.accept();
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());
                log("Client connected");
                login();
                while (true) {
                    String action = this.receiveMessage();
                    if (action.equals("upload")) receiveNote(false);
                    if (action.equals("listUnauthorizedNotes")) listUnauthorizedNotes();
                    if (action.equals("listAuthorizedNotes")) listAuthorizedNotes();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void listAuthorizedNotes() throws IOException {
        ArrayList<Data> notes = new ArrayList<>();
        notes = dbHandler.getNotes(true);
        this.sendObject(notes);
        String file = this.receiveMessage();
        String type = this.receiveMessage();
        Data toSend;
        if(type.equals("Y")) {
            Data ciphered = dbHandler.getCipheredNote(file);
            toSend =
        }
        else
            toSend = dbHandler.getNote(file, true);

        byte[] fileBytes = new byte[0];
        try {
            fileBytes = Files.readAllBytes(Paths.get("files", toSend.getFileName()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        toSend.setData(fileBytes);
        this.sendObject(toSend);
    }

    private void listUnauthorizedNotes() throws IOException {
        ArrayList<Data> notes = new ArrayList<>();
        notes = dbHandler.getNotes(false);
        this.sendObject(notes);
        String file = this.receiveMessage();
        Data toSend = dbHandler.getNote(file, false);

        byte[] fileBytes = new byte[0];
        try {
            fileBytes = Files.readAllBytes(Paths.get("files", toSend.getFileName()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        toSend.setData(fileBytes);
        this.sendObject(toSend);

        //Data authorizedData = (Data) this.receiveObject();
        receiveNote(true);
    }

    public void receiveNote(boolean bothSignatures) throws IOException {
        Data d = (Data) this.receiveObject();

        if (digitalSignature.verifySignature(d, bothSignatures)) {
            if (bothSignatures) {
                //UPDATE FIRMA CHIEF
                dbHandler.updateInDB(d);
                try {
                    EncData res = blockCipher.encrypt((byte[]) d.getData(), d.getFileName());
                    res.setChiefId(d.getIdChief());
                    dbHandler.insertEncInfo(res);
                } catch (NoSuchPaddingException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException | InvalidKeyException e) {
                    e.printStackTrace();
                }
            } else {
                dbHandler.insertInDB(d);
                FileUtils.writeByteArrayToFile(new File(Paths.get("files", d.getFileName()).toString()), (byte[]) d.getData());
            }
        } else {
            log("Archivo corrupto");
        }

    }

    public void login() {
        User toValidate = (User) this.receiveObject();
        User result = dbHandler.getUser(toValidate.getId());
        if (result != null && toValidate.getId().equals(result.getId()) && toValidate.getPassword().equals(result.getPassword())) {
            System.out.println("Client identified");
            this.sendObject(result);
        } else {
            System.out.println("Client NOT identified");
            this.sendObject(null);
        }
    }

    public void sendMessage(String mes) {
        try {
            oos.writeUTF(mes);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String receiveMessage() {
        try {
            String res = ois.readUTF();
            return res;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void sendObject(Object toSend) {
        try {
            oos.writeObject(toSend);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Object receiveObject() {
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