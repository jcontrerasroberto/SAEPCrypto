import org.apache.commons.io.FileUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Scanner;

public class SAEP {

    private User user;
    private final int port = 9393;
    private final String dir = "localhost";
    private final DigitalSignature digitalSignature;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    public SAEP() throws IOException {
        digitalSignature = new DigitalSignature();
        try {
            Socket socketcon = new Socket(dir, port);
            oos = new ObjectOutputStream(socketcon.getOutputStream());
            oos.flush();
            ois = new ObjectInputStream(socketcon.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.user = new User();
        this.login();
    }

    public void login() throws IOException {
        Scanner in = new Scanner(System.in);
        System.out.println("WELCOME TO SAEP");
        System.out.print("ID:");
        String id = in.nextLine();
        System.out.print("Password:");
        String password = in.nextLine();
        this.user.setId(id);
        this.user.setName(null);
        this.user.setRole(null);
        this.user.setPassword(hashPassword(password));
        try {
            this.sendObject(this.user);
            oos.flush();
            User result = (User) this.receiveObject();
            if(result==null) login();
            else {
                user = result;
                menu();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        menu();
        in.close();
    }

    public void menu() throws IOException {
        Scanner in = new Scanner(System.in);
        if(this.user.getRole().equals("PROFESSOR")){
            System.out.println("Please approve all your students :c");
            this.uploadNotes();
        }
        else if(this.user.getRole().equals("CHIEF")){
            System.out.println("1. List unauthorized notes");
            System.out.println("2. List authorized notes");
            System.out.print("Option: ");
            Integer opt = in.nextInt();
            switch (opt){
                case 1:
                    this.listUnauthorizedNotes();
                    break;
                case 2:
                    this.listAuthorizedNotes();
                    break;
            }
        }
        else if(this.user.getRole().equals("PRINCIPAL")){
            System.out.println("Listing notes");
            this.listAuthorizedNotes();
        }
    }

    public void listAuthorizedNotes() throws IOException {
        Scanner in = new Scanner(System.in);
        this.sendMessage("listAuthorizedNotes");
        ArrayList<Data> res = (ArrayList<Data>) this.receiveObject();
        System.out.println("Authorized lists:\n");
        for (Data d : res) {
            System.out.println(d.getFileName() + " - " + d.getId());
        }
        if(!res.isEmpty()){
            System.out.print("\nSelect the list to download: ");
            this.sendMessage(in.nextLine());
            System.out.print("Do you want to download a backup? Y/n: ");
            String backup = in.nextLine();
            this.sendMessage(backup);

            Data r = (Data) this.receiveObject();
            if(backup.equals("Y"))
                r.setFileName(r.getFileName()+".pdf");
            if (digitalSignature.verifySignature(r, true)){
                FileUtils.writeByteArrayToFile(new File(Paths.get("files", r.getFileName()).toString()), (byte[]) r.getData());
                //Open the downloaded file
                if(Desktop.isDesktopSupported()){
                    try{
                        File f = new File(Paths.get("files", r.getFileName()).toString());
                        Desktop.getDesktop().open(f);
                    }catch (IOException E){
                        E.printStackTrace();
                    }
                }
            }else{
                System.out.println("Corrupted file!");
                this.sendObject(null);
            }
        }else{
            this.sendMessage("EMPTY");
        }
    }

    public void listUnauthorizedNotes() throws IOException {
        Scanner in = new Scanner(System.in);
        this.sendMessage("listUnauthorizedNotes");
        ArrayList<Data> res = (ArrayList<Data>) this.receiveObject();
        System.out.println("Unauthorized lists:");
        for(Data d : res){
            System.out.println(d.getFileName() + " - " + d.getId());
        }
        if(!res.isEmpty()){
            System.out.print("Select the list to sign: ");
            this.sendMessage(in.nextLine());
            Data r = (Data) this.receiveObject();
            if (digitalSignature.verifySignature(r, false)){
                FileUtils.writeByteArrayToFile(new File(Paths.get("files", r.getFileName()).toString()), (byte[]) r.getData());
                //Open the downloaded file
                if(Desktop.isDesktopSupported()){
                    try{
                        File f = new File(Paths.get("files", r.getFileName()).toString());
                        Desktop.getDesktop().open(f);
                    }catch (IOException E){
                        E.printStackTrace();
                    }
                }
                System.out.print("Do you want to authorize the notes? Y/n: ");
                String auth = in.nextLine();
                if(auth.equals("Y")){
                    System.out.println("Signing file");
                    r.setIdChief(user.getId());
                    r.setSignatureChief(digitalSignature.sign(r));
                    System.out.println("File signed successfully by "+user.getId()+", digital signature: "+ new String(Base64.getEncoder().encode(r.getSignatureChief())));
                    this.sendObject(r);
                }else{
                    this.sendObject(null);
                }
            }else{
                System.out.println("Corrupted file!");
                this.sendObject(null);
            }
        }else{
            this.sendMessage("EMPTY");
        }
    }

    //This method is only used by teacher and not by chief
    public void uploadNotes() {
        this.sendMessage("upload");
        Scanner in = new Scanner(System.in);
        String filepath;
        System.out.println("UPLOAD AND SIGN GRADES LIST");
        System.out.print("File path (name should be GROUP_SUBJECT, Example: 3CM9_MATH) >> ");
        filepath = in.nextLine();
        byte[] fileBytes = new byte[0];
        try {
            fileBytes = Files.readAllBytes(Paths.get(filepath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Data data = new Data();
        data.setFileName(Paths.get(filepath).getFileName().toString());
        data.setData(fileBytes);
        //SIGN FILE WITH THE DATA OF THE USER TOO.
        data.setId(user.getId());
        data.setSignatureTeacher(new DigitalSignature().sign(data));
        //SEND DATA VIA SOCKET
        this.sendObject(data);

        System.out.println("File signed successfully by "+data.getId()+", digital signature: "+ new String(Base64.getEncoder().encode(data.getSignatureTeacher())));

    }

    public void sendMessage(String mes){
        try {
            oos.writeUTF(mes);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

/*    public String receiveMessage(){
        try {
            String res = ois.readUTF();
            return res;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }*/

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

    public String hashPassword(String password){
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(password.getBytes());
            String digest = Base64.getEncoder().encodeToString(hash);
            System.out.println("digest password = " + digest);
            return digest;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) throws IOException {

        SAEP saep = new SAEP();

    }
}