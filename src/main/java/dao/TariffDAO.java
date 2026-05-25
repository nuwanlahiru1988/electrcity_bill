package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import model.OtherConsumer;
import model.Station;
import util.DBConnection;

public class TariffDAO {
	
	public List<OtherTariffListDAO> getOtherConsumerList(){
		List<OtherTariffListDAO> lists = new ArrayList<>();
		String sql = "SELECT * FROM other_consumer";
		
		try {
			Connection con = DBConnection.getConnection();
			PreparedStatement ps = con.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			
			
			while(rs.next()) {
				OtherTariffListDAO daoObj = new OtherTariffListDAO();
				daoObj.setId(rs.getInt("id"));
				daoObj.setCategory(rs.getString("category"));
				
				lists.add(daoObj);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return lists;
		
		}
	}


