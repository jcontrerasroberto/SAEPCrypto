import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;

public class DigitalSignature {
    //ATRIBUTOS
    //no hay, no existen :c

    //METODOS COMO FIRMAR, VERIFICAR FIRMA
    public byte[] sign(Data data){
        byte[] fileBytes = (byte[]) data.getData();
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
            outputStream.write( data.getId().getBytes() );
            if(data.getSignatureTeacher() != null)//when the Signatory user is the chief
                outputStream.write(data.getSignatureTeacher());
            outputStream.write( fileBytes );

            byte[] bytesToSign = outputStream.toByteArray( ); // ID + signatureTeacher (if applicable) + fileBytes

            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            //Each instance of SAEP (the client) must have its own and unique sender_keystore2048+ID+.p12 file
            keyStore.load(new FileInputStream("../sender_keystore2048"+data.getId()+".p12"), "procastinadores".toCharArray());
            PrivateKey privateKey = (PrivateKey) keyStore.getKey("senderKeyPair", "procastinadores".toCharArray());
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(bytesToSign);
            return signature.sign();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean verifySignature(Data data, boolean verifyChiefSignature){
        //if verifyChiefSignature=false, then the method verifies the teacher signature

        byte[] fileBytes = (byte[]) data.getData();
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
            outputStream.write( data.getId().getBytes() );
            if(verifyChiefSignature)
                outputStream.write(data.getSignatureTeacher());
            outputStream.write( fileBytes );

            byte[] signedBytes = outputStream.toByteArray( ); // ID + signatureTeacher (if applicable) + fileBytes

            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            //Every instance of SAEP (every client) and server must have all pubKey+ID+.p12 files (all the pub keys)
            keyStore.load(new FileInputStream("../pubKey"+data.getId()+".p12"), "procastinadores".toCharArray());
            Certificate certificate = keyStore.getCertificate("receiverKeyPair");
            PublicKey publicKey = certificate.getPublicKey();

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(signedBytes);
            if (verifyChiefSignature)
                return signature.verify(data.getSignatureChief());
            else
                return signature.verify(data.getSignatureTeacher());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
