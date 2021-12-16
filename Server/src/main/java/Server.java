import org.apache.commons.io.FileUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class Server {

    private final Integer port = 9393;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private final DataBaseHandler dbHandler;
    private final DigitalSignature digitalSignature;
    private final BlockCipher blockCipher;

    public Server() {
        log("Connecting to the DB");
        dbHandler = new DataBaseHandler();
        digitalSignature = new DigitalSignature();
        blockCipher = new BlockCipher();
        log("Starting server...");
        ServerSocket ss;

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
        ArrayList<Data> notes;
        notes = dbHandler.getNotes(true);
        this.sendObject(notes);
        String file = this.receiveMessage();
        String type = this.receiveMessage();
        Data toSend;
        byte[] fileBytes = new byte[0];
        String directory = "files";
        if(type.equals("Y")) {
            toSend = dbHandler.getCipheredNote(file);
            directory += "\\"+"enc";
        }
        else {
            toSend = dbHandler.getNote(file, true);
        }

        try {
            fileBytes = Files.readAllBytes(Paths.get(directory, toSend.getFileName()));
            if(type.equals("Y"))
                fileBytes = blockCipher.decrypt(fileBytes, toSend.getIv());
        } catch (IOException | InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }

        toSend.setData(fileBytes);
        this.sendObject(toSend);
    }

    private void listUnauthorizedNotes() throws IOException {
        ArrayList<Data> notes;
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

        receiveNote(true);
    }

    public void receiveNote(boolean bothSignatures) throws IOException {
        Data d = (Data) this.receiveObject();

        if (digitalSignature.verifySignature(d, bothSignatures)) {
            if (bothSignatures) {
                dbHandler.updateInDB(d);
                try {
                    EncData res = blockCipher.encrypt((byte[]) d.getData(), d.getFileName());
                    res.setChiefId(d.getIdChief());
                    dbHandler.insertEncInfo(res, d);
                } catch (NoSuchPaddingException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException | InvalidKeyException e) {
                    e.printStackTrace();
                }
            } else {
                dbHandler.insertInDB(d);
                FileUtils.writeByteArrayToFile(new File(Paths.get("files", d.getFileName()).toString()), (byte[]) d.getData());
            }
        } else {
            log("Corrupted file!");
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

/*    public void sendMessage(String mes) {
        try {
            oos.writeUTF(mes);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    public String receiveMessage() {
        try {
            return ois.readUTF();
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
            return ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
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