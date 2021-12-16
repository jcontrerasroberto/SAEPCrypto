public class EncData {

    private String encFilename;
    private String originalFilename;
    private String iv;
    private String chiefId;

    public EncData() {
        super();
    }

    public String getEncFilename() {
        return encFilename;
    }
    public void setEncFilename(String encFilename) {
        this.encFilename = encFilename;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }
    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getIv() {
        return iv;
    }
    public void setIv(String iv) {
        this.iv = iv;
    }

    public String getChiefId() {
        return chiefId;
    }
    public void setChiefId(String chiefId) {
        this.chiefId = chiefId;
    }
}
