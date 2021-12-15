import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.Scanner;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

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
        //System.out.println(dir);
                //directoryName+"\\"+name+"."+ext;

        byte[] source = Files.readAllBytes(Path.of(dir));
        String message = new String(source);

        byte[] decodedBytes = Base64.getDecoder().decode(message);
        return decodedBytes;
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

    static IvParameterSpec createIv() throws IOException {
        System.out.println("Creating IV");
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        //System.out.print("Choose a file name to save the iv");
        //saveFile(Base64.getEncoder().encode(iv),"txt");
        return new IvParameterSpec(iv);
    }

    public EncData encrypt(byte[] data, String filename) throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeyException {
        byte[] cypherData;
        //System.out.print("Choose a file name to read the key");
        SecretKey key = new SecretKeySpec(readFile("sec/AESkey.key"), "AES" );
        IvParameterSpec iv = createIv();

        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE,key,iv);
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
        //System.out.print("Choose a file name to read the iv");

        IvParameterSpec ivDec= new IvParameterSpec(Base64.getDecoder().decode(iv));

        javax.crypto.Cipher cipher= javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(javax.crypto.Cipher.DECRYPT_MODE,key,ivDec);
        originalData= cipher.doFinal(data);
        //ByteToFile(originalData,"OriginalData.pdf");
        return originalData;
    }

    public static byte[] loadFile(String sourcePath) throws IOException
    {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(sourcePath);
            return readFully(inputStream);
        }finally {
            if (inputStream != null)
                inputStream.close();
        }
    }

    public static byte[] readFully(InputStream stream) throws IOException
    {
        byte[] buffer = new byte[8192];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int bytesRead;
        while ((bytesRead = stream.read(buffer)) != -1)
        {
            baos.write(buffer, 0, bytesRead);
        }
        return baos.toByteArray();
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

    /*public static void main (String[]args) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        createKey();
        Scanner in = new Scanner(System.in);
        System.out.println("absolute path");
        String namefile = in.nextLine();

        byte[] aux = encrypt(loadFile(namefile));
        decrypt(aux);
    }*/
}
