package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import model.User;
import util.DBConnection;

public class UserDAO {
	
	public User loginUser(String username,String password) {
		User user = null;
		String sql="SELECT * FROM user WHERE username = ? AND password = ?";
		
		try {
			Connection con = DBConnection.getConnection();
			PreparedStatement ps = con.prepareStatement(sql);
			
			ps.setString(1, username);
			ps.setString(2, password);
			
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				user = new User();
				user.setId(rs.getInt("id"));
				user.setUsername(rs.getString("username"));
				user.setRole(rs.getString("role"));
				user.setPassword(rs.getString("password"));
			}			
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return user;
	}

}
