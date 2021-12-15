import java.io.Serializable;

public class Data implements Serializable {

    byte[] signatureTeacher;
    byte[] signatureChief;
    String fileName;
    Object data;
    String id;
    String idChief;

    public Data() {
    }

    public byte[] getSignatureTeacher() {
        return signatureTeacher;
    }
    public void setSignatureTeacher(byte[] signature) {
        this.signatureTeacher = signature;
    }

    public byte[] getSignatureChief() { return signatureChief; }
    public void setSignatureChief(byte[] signatureChief) { this.signatureChief = signatureChief; }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getIdChief() {
        return idChief;
    }

    public void setIdChief(String idChief) {
        this.idChief = idChief;
    }
}