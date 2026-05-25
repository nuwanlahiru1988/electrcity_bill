package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import model.Station;
import util.DBConnection;

public class StationDAO {
	
	public List<Station> getStationList(){
		List<Station> stations =new ArrayList<>();
 		String sql = "SELECT id,station_name FROM station WHERE status != ? ";
 		
		try {
			Connection con = DBConnection.getConnection();
			PreparedStatement ps = con.prepareStatement(sql);
			
			ps.setInt(1, 0);
			
			ResultSet rs = ps.executeQuery();
			
			while(rs.next()) {
			
				Station st = new Station();
				st.setId(rs.getInt("id"));
				st.setStationName(rs.getString("station_name"));
				stations.add(st);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return stations;
		
	}

}
