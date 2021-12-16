import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Scanner;

public class BlockCipher {

    static String dirActual = "files";
    static Path path = Paths.get("");
    static String directoryName = Paths.get(String.valueOf(path.toAbsolutePath()), dirActual, "enc").toString();

    public BlockCipher(){
        super();
    }

    static void saveFile(byte[] content,String ext) throws IOException {
        System.out.print("(Without extension): ");
        Scanner reader = new Scanner(System.in);
        String name = reader.next();

        String dir = directoryName+"\\"+name+"."+ext;

        File f = new File(dir);
        FileOutputStream stream = new FileOutputStream(f.getAbsolutePath());
        stream.write(content);
        System.out.println("Data saved successfully in file "+ f.getName());
    }

    static byte[] readFile(String path) throws IOException {

        String dir = Paths.get(path).toString();

        byte[] source = Files.readAllBytes(Path.of(dir));
        String message = new String(source);

        return Base64.getDecoder().decode(message);
    }

    static void createKey () {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            SecretKey key = keyGen.generateKey();
            System.out.print("Choose a file name to save the key");
            saveFile(Base64.getEncoder().encode(key.getEncoded()),"key");
        } catch (NoSuchAlgorithmException | IOException e) {
            System.out.println("ERROR");
        }
    }

    static IvParameterSpec createIv() {
        System.out.println("Creating IV");
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        System.out.println("Created iv = " + Arrays.toString(iv));
        return new IvParameterSpec(iv);
    }

    public EncData encrypt(byte[] data, String filename) throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeyException {
        byte[] cypherData;
        SecretKey key = new SecretKeySpec(readFile("sec/AESkey.key"), "AES" );
        IvParameterSpec iv = createIv();

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE,key,iv);
        cypherData= cipher.doFinal(data);

        ByteToFile(cypherData,filename+".aes");

        EncData result = new EncData();
        result.setEncFilename(filename+".aes");
        result.setOriginalFilename(filename);
        result.setIv(new String(Base64.getEncoder().encode(iv.getIV())));

        return result;
    }

    public static byte[] decrypt(byte[] data, String iv) throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeyException {
        byte[] originalData;
        SecretKey key = new SecretKeySpec(readFile("sec/AESkey.key"), "AES" );

        IvParameterSpec ivDec= new IvParameterSpec(Base64.getDecoder().decode(iv));

        Cipher cipher= Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE,key,ivDec);
        originalData= cipher.doFinal(data);
        return originalData;
    }

    static void ByteToFile (byte[] bytes, String name) {
        try {
            String dir = Paths.get(directoryName, name).toString();
            writeBytesToFile(dir, bytes);
            System.out.println("Enc file saved");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void writeBytesToFile(String fileOutput, byte[] bytes) throws IOException {
        FileOutputStream fos = new FileOutputStream(fileOutput);
        fos.write(bytes);
        fos.close();
    }

    /*public static void main (String[]args){
        createKey();
    }*/
}
