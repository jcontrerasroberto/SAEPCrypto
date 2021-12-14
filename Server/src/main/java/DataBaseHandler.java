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

    private void log(String men) {
        System.out.println(men);
    }

}
