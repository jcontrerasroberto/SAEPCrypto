import java.sql.*;

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

    public void insertInDB(Data data){

    }

    private void log(String men) {
        System.out.println(men);
    }

}
