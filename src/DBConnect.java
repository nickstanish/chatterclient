import java.sql.Connection;
import java.sql.DriverManager;


public class DBConnect {

	public DBConnect() {
		// TODO Auto-generated constructor stub
		Connection conn = null;

        try
        {
            String userName = "nstanish_user";
            String password = "xiySbU1[qk~n";
            String url = "jdbc:mysql://javafilter.heliohost.org:3306/" + "nstanish_chatterbox";
            Class.forName ("com.mysql.jdbc.Driver").newInstance ();
            conn = DriverManager.getConnection (url, userName, password);
            System.out.println ("Database connection established");
        }
        catch (Exception e)
        {
            System.err.println ("Cannot connect to database server" + e);
        }
        finally
        {
            if (conn != null)
            {
                try
                {
                    conn.close ();
                    System.out.println ("Database connection terminated");
                }
                catch (Exception e) { /* ignore close errors */ 
                	
                }
            }
        }
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new DBConnect();
	}

}
