import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;

public class DataBaseHandler {

    static Connection SAEPConn = null;
    static PreparedStatement SAEPPrepareStat = null;

    public DataBaseHandler(){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            SAEPConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/saep", "root", "root");
            if(SAEPConn!=null) log("Connected");
            else log("Error connecting the BD");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public User getUser(String id){
        try {
            SAEPPrepareStat = SAEPConn.prepareStatement("SELECT * FROM users WHERE user_id = ?");
            SAEPPrepareStat.setString(1, id);
            ResultSet rs = SAEPPrepareStat.executeQuery();
            while (rs.next()){
                User temp = new User();
                temp.setId(rs.getString("user_id"));
                temp.setName(rs.getString("user_name"));
                temp.setRole(rs.getString("user_role"));
                temp.setPassword(rs.getString("user_password"));
                return temp;
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<Data> getUnauthorizedNotes(){
        try {
            SAEPPrepareStat = SAEPConn.prepareStatement("SELECT * FROM notes WHERE note_chief_sign IS NULL");
            ResultSet rs = SAEPPrepareStat.executeQuery();
            ArrayList<Data> result = new ArrayList<>();
            while (rs.next()){
                Data temp = new Data();
                temp.setFileName(rs.getString("note_filename"));
                temp.setSignatureTeacher(Base64.getDecoder().decode(rs.getString("note_professor_sign")));
                temp.setSignatureChief(null);
                temp.setId(rs.getString("note_professor_id"));
                temp.setIdChief(rs.getString("note_chief_id"));
                result.add(temp);
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Data getNote(String filename){
        try {
            SAEPPrepareStat = SAEPConn.prepareStatement("SELECT * FROM notes WHERE note_filename = ?");
            SAEPPrepareStat.setString(1, filename);
            ResultSet rs = SAEPPrepareStat.executeQuery();
            while (rs.next()){
                Data temp = new Data();
                temp.setFileName(rs.getString("note_filename"));
                temp.setSignatureTeacher(Base64.getDecoder().decode(rs.getString("note_professor_sign")));
                temp.setSignatureChief(null);
                temp.setId(rs.getString("note_professor_id"));
                temp.setIdChief(rs.getString("note_chief_id"));
                return temp;
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void insertInDB(Data data){
        System.out.println("Saving to DB");
        try {
            PreparedStatement stm = SAEPConn.prepareStatement("INSERT INTO notes (note_filename, note_professor_sign, note_chief_sign, note_professor_id, note_chief_id) values (?, ?, ?, ?, ?);");
            stm.setString(1, data.getFileName());
            stm.setString(2, new String(Base64.getEncoder().encode(data.getSignatureTeacher())));
            if(data.getSignatureChief()==null)
                stm.setNull(3, Types.NULL);
            else
                stm.setString(3, new String(Base64.getEncoder().encode(data.getSignatureChief())));
            stm.setString(4, data.getId());
            stm.setString(5, data.getIdChief());
            stm.executeUpdate();
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }

    }

    public void insertEncInfo(EncData encData){
        System.out.println("Saving to DB");
        try {
            PreparedStatement stm = SAEPConn.prepareStatement("INSERT INTO backup_notes (backup_filename, backup_original_filename, backup_iv, backup_chief_id) VALUES(?, ?, ?, ?);");
            stm.setString(1, encData.getEncFilename());
            stm.setString(2, encData.getOriginalFilename());
            stm.setString(3, encData.getIv());
            stm.setString(4, encData.getChiefId());
            stm.executeUpdate();

        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
    }

    private void log(String men) {
        System.out.println(men);
    }

}
