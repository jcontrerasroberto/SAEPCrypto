public class Data {

    byte[] signatureTeacher;
    byte[] signatureChief;
    Object data;
    String id;

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
}