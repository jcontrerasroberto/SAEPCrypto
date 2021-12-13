import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Scanner;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class BlockCipher {

    static void saveFile(byte[] content,String ext) throws IOException {
        String dirActual = "CipherThings";
        Path path = Paths.get("");
        String directoryName = path.toAbsolutePath().toString();

        System.out.print("Choose a file name to save the key/iv (Without extension): ");
        Scanner reader = new Scanner(System.in);
        String name = reader.next();

        directoryName+="\\"+dirActual+"\\"+name+"."+ext;

        File f = new File(directoryName);
        FileOutputStream stream = new FileOutputStream(f.getAbsolutePath());
        stream.write(content);
        System.out.println("Data saved successfully in file "+ f.getName());
    }

    static byte[] readFile(String ext) throws IOException {
        String dirActual = "CipherThings";
        Path path = Paths.get("");
        String directoryName = path.toAbsolutePath().toString();

        System.out.print("Choose the file name to read the key/iv (Without extension): ");
        Scanner reader = new Scanner(System.in);
        String name = reader.next();
        directoryName+="\\"+dirActual+"\\"+name+"."+ext;

        byte[] source = Files.readAllBytes(Path.of(directoryName));
        String message = new String(source);

        byte[] decodedBytes = Base64.getDecoder().decode(message);
        return decodedBytes;
    }

    static void createKey () {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            SecretKey key = keyGen.generateKey();
            saveFile(Base64.getEncoder().encode(key.getEncoded()),"key");
        } catch (NoSuchAlgorithmException | IOException e) {
            System.out.println("ERROR");
        }
    }

    static IvParameterSpec createIv() throws IOException {
        System.out.println("Creating IV");
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        saveFile(Base64.getEncoder().encode(iv),"txt");
        return new IvParameterSpec(iv);
    }

    static byte[] encrypt(byte[] data) throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeyException {
        byte[] cypherData;
        SecretKey key = new SecretKeySpec(readFile("key"), "AES" );
        IvParameterSpec iv = createIv();


        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE,key,iv);
        cypherData= cipher.doFinal(data);

        ByteToFile(cypherData,"cypherDATA.aes");
        return cypherData;
    }

    public static void decrypt(byte[] data) throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeyException {
        byte[] originalData;
        SecretKey key = new SecretKeySpec(readFile("key"), "AES" );
        IvParameterSpec iv= new IvParameterSpec(readFile("txt"));

        javax.crypto.Cipher cipher= javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(javax.crypto.Cipher.DECRYPT_MODE,key,iv);
        originalData= cipher.doFinal(data);
        ByteToFile(originalData,"OriginalData.pdf");
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
            String dirActual = "CipherThings";
            Path path = Paths.get("");
            String directoryName = path.toAbsolutePath().toString();
            directoryName+="\\"+dirActual+"\\"+name;

            writeBytesToFile(directoryName, bytes);

            System.out.println("Done");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    static void writeBytesToFile(String fileOutput, byte[] bytes) throws IOException {
        FileOutputStream fos = new FileOutputStream(fileOutput);
        fos.write(bytes);
        fos.close();
    }

    public static void main (String[]args) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        createKey();
        Scanner in = new Scanner(System.in);
        System.out.println("absolute path");
        String namefile = in.nextLine();

        byte[] aux = encrypt(loadFile(namefile));
        decrypt(aux);
    }
}
