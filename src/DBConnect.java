import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class DBConnect {

	public DBConnect() {
		// TODO Auto-generated constructor stub
		Connection conn = null;
		System.out.println(md5("encrypt password before storing"));

        try
        {
        	//standard user
            String userName = "nstanish_user";
            String password = "xiySbU1[qk~n";
            //all access user
            userName = "nstanish_kagui";
            password = "xFcMC8V9MQT7";
            String url = "jdbc:mysql://javafilter.heliohost.org:3306/" + "nstanish_chatterbox";
            Class.forName ("com.mysql.jdbc.Driver").newInstance ();
            conn = DriverManager.getConnection (url, userName, password);
            System.out.println ("Database connection established");
            try{
            	  Statement st = conn.createStatement();
            	  String username = "my chosen username";
            	  String pass = md5("password");
            	  String name = "my whole name";
            	  String email = "myemail@mydomain.com";
            	  int admin = 0;
            	  int val = st.executeUpdate("INSERT INTO auth (username, password, name, email, admin) VALUES ('"+ username +"','"+ pass +"','" + name + "','" + email + "','" + admin +"')");
            	  System.out.println("1 row affected");
            	  }
            	  catch (SQLException s){
            	  System.out.println("SQL statement is not executed!");
            	  }
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
	public static String hex(byte[] array) {
		  StringBuffer sb = new StringBuffer();
		  for (int i = 0; i < array.length; ++i) {
		    sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).toUpperCase().substring(1,3));
		  }
		  return sb.toString();
		}
		 
		 
		public static String md5(String message) { 
		  try { 
		    MessageDigest md = MessageDigest.getInstance("MD5"); 
		    return hex (md.digest(message.getBytes("CP1252"))); 
		  } catch (NoSuchAlgorithmException e) { } catch (UnsupportedEncodingException e) { } 
		  return null;
		}

}
