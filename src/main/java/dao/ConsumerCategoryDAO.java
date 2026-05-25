package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import model.ConsumerCategory;
import util.DBConnection;

public class ConsumerCategoryDAO {
	
	
	public List<ConsumerCategory> getCategoryList() {
		
		String sql = "SELECT * FROM consumer_category";
		List<ConsumerCategory> arr = new ArrayList<ConsumerCategory>();
		try {
			Connection con = DBConnection.getConnection();
			PreparedStatement ps = con.prepareStatement(sql);		
			ResultSet rs = ps.executeQuery();
			
			
			while(rs.next()) {
			
				ConsumerCategory category = new ConsumerCategory();
				category.setId(rs.getInt("id"));
				category.setDescription(rs.getString("description"));
				category.setType(rs.getInt("type"));
				arr.add(category);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return arr;
		
	}

}
