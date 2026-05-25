package util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
	
	private static final String URL = "jdbc:mysql://localhost:3306/green_elec?useSSL=false&serverTimezone=Asia/Colombo";
	private static String USER = "root";
	private static String PASSWORD = "Pa$$w0rd";
	
	public static Connection getConnection() {
		
		Connection con = null;
		
		try {
			
			Class.forName("com.mysql.cj.jdbc.Driver");
			con = DriverManager.getConnection(URL,USER,PASSWORD);
			
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		
		return con;
		
	}

}
