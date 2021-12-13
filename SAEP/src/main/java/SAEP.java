import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class SAEP {

    private User user;
    private final int port = 9393;
    private final String dir = "localhost";
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    public SAEP(){

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

    public void login(){
        Scanner in = new Scanner(System.in);
        //COMO VAMOS A MANEJAR EL LOGIN?
        System.out.println("WELCOME TO SAEP");
        System.out.print("ID:");
        String id = in.nextLine();
        this.user.setId(id);
        this.user.setName("Procrastinadores");
        this.user.setRole("PROFESOR");
        try {
            oos.writeUTF(id);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        menu();
        in.close();
    }

    public void menu(){
        Scanner in = new Scanner(System.in);
        if(this.user.getRole().equals("PROFESOR")){
            System.out.println("1. Subir calificaciones");
            System.out.print("Opcion: ");
            Integer opt = in.nextInt();
            switch (opt){
                case 1:
                    this.uploadNotes();
                    break;
            }

        }
    }

    //This method is only used by teacher and not by chief
    public void uploadNotes() {
        Scanner in = new Scanner(System.in);
        String filepath;
        System.out.println("SUBIR NOTAS");
        System.out.print("File path: ");
        filepath = in.nextLine();
        byte[] fileBytes = new byte[0];
        try {
            fileBytes = Files.readAllBytes(Paths.get(filepath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Data data = new Data();
        data.setData(fileBytes);
        data.setId(user.getId());
        //SIGN FILE WITH THE DATA OF THE USER TOO.
        data.setSignatureTeacher(new DigitalSignature().sign(data));
        //CREATE DATA INSTANCE WITH THE FILE BYTES AND THE SIGNATURE

        //SEND DATA VIA SOCKET
    }

    public static void main(String[] args) {

        SAEP saep = new SAEP();


    }
}