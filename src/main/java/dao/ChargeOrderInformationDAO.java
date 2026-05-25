package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import dto.ChargeOrderInformationDTO;
import dto.GeneralPurposeRateDTO;
import dto.HotelRateDTO;
import dto.IndustrialChargerCalculationDTO;
import dto.ReportDomasticNormalDTO;
import model.ChargeOrderInformation;
import model.Station;
import util.DBConnection;
import util.Util;

public class ChargeOrderInformationDAO{
	
	public List<ChargeOrderInformationDTO> getChargerOrderInformationListByDateRangeANDStationName(String station_name,String filter_start_date,String filter_end_date){
		
		String sql="SELECT * FROM charge_order_information WHERE start_charging_time >= ? AND end_charging_time <= ? AND station_name = ?";
		List<ChargeOrderInformationDTO> dtosList = new ArrayList<>();
				
		try {
			
			Connection con = DBConnection.getConnection();
			PreparedStatement ps = con.prepareStatement(sql);
			
			ps.setString(1, filter_start_date);
			ps.setString(2, filter_end_date);
			ps.setString(3, station_name);		
			ResultSet rs = ps.executeQuery();
			
			while(rs.next()) {
				
//				System.out.println(rs.getObject("start_charging_time", LocalDateTime.class));
//				System.out.println(rs.getObject("end_charging_time", LocalDateTime.class));
			
			
				ChargeOrderInformationDTO chargeOrderInformationDTO = new ChargeOrderInformationDTO();
				chargeOrderInformationDTO.setActual_payment_amount(rs.getDouble("actual_payment_amount"));
				chargeOrderInformationDTO.setCharge_duration(rs.getString("charge_duration"));
				chargeOrderInformationDTO.setCharge_kwh(rs.getDouble("charge_kwh"));
				chargeOrderInformationDTO.setDelay_duration(rs.getString("delay_duration"));
				chargeOrderInformationDTO.setStart_chaging_time(rs.getObject("start_charging_time",LocalDateTime.class));
				chargeOrderInformationDTO.setEnd_charging_time(rs.getObject("end_charging_time",LocalDateTime.class));
				chargeOrderInformationDTO.setTotal_consumption_amount(rs.getDouble("total_consumption_amount"));
				dtosList.add(chargeOrderInformationDTO);
				
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return dtosList;
			
	}
	
	public double getTotalCharginHours(String station_name,String filter_start_date,String filter_end_date) {
		
		double total_power = 0.0;
		String sql="SELECT SUM(charge_kwh) as total_power FROM charge_order_information WHERE start_charging_time >= ? AND end_charging_time <= ? AND station_name = ?";
		
		try {
			
				Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(sql);
				ps.setString(1, filter_start_date);
				ps.setString(2, filter_end_date);
				ps.setString(3, station_name);	
				ResultSet rs = ps.executeQuery();

			if(rs.next()) {
				total_power = rs.getDouble("total_power");
			}
		
		}catch(Exception e) {
			e.printStackTrace();
		}
		return total_power;
	}
	
	public List<ReportDomasticNormalDTO> getDomesticNormalReport(
			String charging_station_name,
			String charging_lifecycle_start_date,
			String charging_lifecycle_end_date,
			int category
			) {
			
		
		String sql="SELECT * FROM charge_order_information WHERE start_charging_time >= ? AND end_charging_time <= ? AND station_name = ?";
		List<ReportDomasticNormalDTO> domestic = new ArrayList<>();
		double price_per_unit  = 0.0; 
		
		
		String  sqlcategory = "SELECT * FROM domestic WHERE id = ?";
		try {
			
			Connection con = DBConnection.getConnection();
			PreparedStatement ps = con.prepareStatement(sql);
			
			ps.setString(1, charging_lifecycle_start_date);
			ps.setString(2, charging_lifecycle_end_date);
			ps.setString(3, charging_station_name);		
			ResultSet rs = ps.executeQuery();
			
			
			PreparedStatement psn = con.prepareStatement(sqlcategory);
			psn.setString(1, Integer.toString(category));	
			ResultSet rsn = psn.executeQuery();
			
			if(rsn.next()) {
				
				price_per_unit = rsn.getDouble("price");
			}

			while(rs.next()) {
						
			//	System.out.println(price_per_unit);
				
				double cost_per_charging_session  = rs.getDouble("charge_kwh")*price_per_unit;
				double profit = rs.getDouble("total_consumption_amount")-cost_per_charging_session;
				
				ReportDomasticNormalDTO dto = new ReportDomasticNormalDTO();
				dto.setCar_plate_number(rs.getString("car_plate_number"));
				dto.setCharge_duration(rs.getString("charge_duration"));
				dto.setStart_charging_time(rs.getString("start_charging_time"));
				dto.setEnd_charging_time(rs.getString("end_charging_time"));
				dto.setCharge_kWh(rs.getDouble("charge_kwh"));
				dto.setElectricity_charge(cost_per_charging_session);
				dto.setTotal_consumption_amount(rs.getDouble("total_consumption_amount"));
				dto.setRevenue_without_vat_and_ssl(rs.getDouble("total_consumption_amount"));
				dto.setEstimated_profit(profit);				
				domestic.add(dto);
				
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return domestic;
			
	}
	

	
public List<IndustrialChargerCalculationDTO> getChargerIndustrialData(String station,String start_date,String end_date,int category,int subcategory,String gun_type){
		
		String sql="SELECT * FROM charge_order_information WHERE start_charging_time >= ? AND end_charging_time <= ? AND station_name = ?";
		List<IndustrialChargerCalculationDTO> domestic = new ArrayList<>();
		
		double price_per_unit  = 0.0;
		
		try {
			Connection con = DBConnection.getConnection();
			PreparedStatement ps = con.prepareStatement(sql);			
				ps.setString(1, start_date);
				ps.setString(2, end_date);
				ps.setString(3, station);		
				ResultSet rs = ps.executeQuery();	
				
				String total_kwh_query = "SELECT SUM(charge_kwh) as total FROM charge_order_information WHERE station_name = ? ";
				PreparedStatement ps_tkq = con.prepareStatement(total_kwh_query);
				ps_tkq.setString(1, station);
				ResultSet resultSet = ps_tkq.executeQuery();
				
				double charge_kwh = 0.0;
				double day_price = 0.0;
				double peak_price = 0.0;
				double off_peak_price = 0.0;
				double fixed_charge = 0.0;
				double demand_charge = 0.0;
				
				if(resultSet.next()) {
					
					
			
						if(resultSet.getDouble("total") <= 300) {
							
							String  sqlcategory = "SELECT * FROM other_consumer WHERE type = 1";
							PreparedStatement psn = con.prepareStatement(sqlcategory);
							ResultSet rsn = psn.executeQuery();
							//System.out.println(rsn.get);
							
		while(rsn.next()) {
								
								day_price = rsn.getDouble("day_price");
								peak_price = rsn.getDouble("peak_price");
								off_peak_price = rsn.getDouble("off_peak_price");
								fixed_charge =  rsn.getDouble("fixed_charge");
								demand_charge =  rsn.getDouble("demand_charge");
							}
							
							while(rs.next()) {
								//check Day
								Util util = new Util();
								charge_kwh = rs.getDouble("charge_kwh");
								//charging start time
								Timestamp start_timestamp = rs.getTimestamp("start_charging_time");
								LocalTime charging_start_time = start_timestamp.toLocalDateTime().toLocalTime();
								
								//charging end time 
								Timestamp end_timestamp = rs.getTimestamp("end_charging_time");
								LocalTime charging_end_time = end_timestamp.toLocalDateTime().toLocalTime();
								
								LocalTime day_start_tariff = LocalTime.parse(util.DAY_TIME_START);
								LocalTime day_end_tariff = LocalTime.parse(util.DAY_TIME_END);
								
								LocalTime peak_time_start = LocalTime.parse(util.PEAK_TIME_START);
								LocalTime peak_time_end = LocalTime.parse(util.PEAK_TIME_END);
								
								LocalTime off_peak_time_start = LocalTime.parse(util.OFF_PEAK_TIME_START);
								LocalTime off_peak_time_end = LocalTime.parse(util.OFF_PEAK_TIME_END);
								
								// FIRST CHECK START AND END TIME IN WHICH TARIFF
								
								int _charging_start_type = 0; // 1 = DAY, 2 = PEAK , 3 = OFF PEAK 
								int _charging_end_type = 0; // 1 = DAY, 2 = PEAK , 3 = OFF PEAK 
								
								if(charging_start_time.isAfter(day_start_tariff) && charging_start_time.isBefore(peak_time_start)) { // DAY
									
									_charging_start_type = 1;
																		
								}else if(charging_start_time.isAfter(peak_time_start) && charging_start_time.isBefore(off_peak_time_start)) { // PEAKE
									
									_charging_start_type = 2;
									
								}else if(charging_start_time.isAfter(off_peak_time_start) && charging_start_time.isBefore(day_start_tariff)) { // OFF PEAKE
									
									_charging_start_type = 3;
									
								}
								
								
								if(charging_end_time.isAfter(day_start_tariff) && charging_end_time.isBefore(peak_time_start)) { // DAY
									
									_charging_end_type = 1;
																		
								}else if(charging_end_time.isAfter(peak_time_start) && charging_end_time.isBefore(off_peak_time_start)) { // PEAKE
									
									_charging_end_type = 2;
									
								}else if(charging_end_time.isAfter(off_peak_time_start) && charging_end_time.isBefore(day_start_tariff)) { // OFF PEAKE
									
									_charging_end_type = 3;
									
								}
								
								
								//THE CYCLE PROCESS 
								
								if(_charging_start_type == 1 && _charging_end_type ==  1) { //{1}
																									
									long diff_1_tariff = Duration.between(charging_start_time,charging_end_time).toMinutes();
									double electricity = rs.getDouble("charge_kwh")*day_price;
									double consume = rs.getDouble("total_consumption_amount");
									double estimate_profit = consume-electricity;
									IndustrialChargerCalculationDTO dtoReport = new IndustrialChargerCalculationDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(rs.getDouble("charge_kwh"));
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(consume);
									dtoReport.setTime_zone("DAY");
									domestic.add(dtoReport);
									
									// System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 1");
									
								}else if(_charging_start_type == 2 && _charging_end_type ==  2) { //{2}
									
									long diff_1_tariff = Duration.between(charging_start_time,charging_end_time).toMinutes();
									double electricity = rs.getDouble("charge_kwh")*peak_price;
									double consume = rs.getDouble("total_consumption_amount");
									double estimate_profit = consume-electricity;
									IndustrialChargerCalculationDTO dtoReport = new IndustrialChargerCalculationDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(rs.getDouble("charge_kwh"));
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(consume);
									dtoReport.setTime_zone("PEAK");
									domestic.add(dtoReport);
									
									// System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 2");
									
								}else if(_charging_start_type == 3 && _charging_end_type ==  3) { //{3}
									
									long diff_1_tariff = Duration.between(charging_start_time,charging_end_time).toMinutes();
									double electricity = rs.getDouble("charge_kwh")*off_peak_price;
									double consume = rs.getDouble("total_consumption_amount");
									double estimate_profit = consume-electricity;
									IndustrialChargerCalculationDTO dtoReport = new IndustrialChargerCalculationDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(rs.getDouble("charge_kwh"));
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(consume);
									dtoReport.setTime_zone("OFF PEAK");
									domestic.add(dtoReport);
									
								//	System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 3");
									
								}else if(_charging_start_type == 1 && _charging_end_type ==  2) { //{1,2}
									
									long diffe = end_timestamp.getTime()-start_timestamp.getTime();
									System.out.println(diffe);
									long total_minutes = diffe/(1000*60);
									System.out.println(total_minutes);
									
									long diff_1_tariff = Duration.between(charging_start_time,day_end_tariff).toMinutes();
									long diff_2_tariff = Duration.between(peak_time_start, charging_end_time).toMinutes();
									
									System.out.println(diff_1_tariff);
									System.out.println(diff_2_tariff);
									// Calculation Process
									System.out.println(charge_kwh);
									double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
									double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
									
									System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);
									System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
									
									double consume = rs.getDouble("total_consumption_amount");
									System.out.println("total_consume "+consume);
									double _session_1_price = (consume/total_minutes)*diff_1_tariff;
									double _session_2_price = (consume/total_minutes)*diff_2_tariff;
									
									System.out.println("1 Session Price "+_session_1_price);
									System.out.println("2 Session Price "+_session_2_price);
									
									// price 
									double electricity = kwh_1*day_price;
									
									
									double estimate_profit_session_1 = _session_1_price-electricity;
									IndustrialChargerCalculationDTO dtoReport = new IndustrialChargerCalculationDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(kwh_1);
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit_session_1);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(_session_1_price);
									dtoReport.setTime_zone("DAY");
									domestic.add(dtoReport);
									
									double electricityn = kwh_2*peak_price;
									
									double estimate_profitn = _session_2_price-electricityn;
									IndustrialChargerCalculationDTO dtoReport1 = new IndustrialChargerCalculationDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_2_tariff);
									dtoReport1.setCharge_kWh(kwh_2);
									dtoReport1.setElectricity_charge(electricityn);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profitn);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_2_price);
									dtoReport1.setTime_zone("PEAK");
									domestic.add(dtoReport1);
									
									
									System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 4");
																
								}else if(_charging_start_type == 2 && _charging_end_type ==  3) { //{2,3}
									
									long diffe = end_timestamp.getTime()-start_timestamp.getTime();
									System.out.println(diffe);
									long total_minutes = diffe/(1000*60);
									System.out.println(total_minutes);
									
									long diff_1_tariff = Duration.between(charging_start_time,peak_time_end).toMinutes();
									long diff_2_tariff = Duration.between(off_peak_time_start, charging_end_time).toMinutes();
									
									System.out.println(diff_1_tariff);
									System.out.println(diff_2_tariff);
									// Calculation Process
									System.out.println(charge_kwh);
									double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
									double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
									
									System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);
									System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
									
									double consume = rs.getDouble("total_consumption_amount");
									System.out.println("total_consume "+consume);
									double _session_1_price = (consume/total_minutes)*diff_1_tariff;
									double _session_2_price = (consume/total_minutes)*diff_2_tariff;
									
									System.out.println("1 Session Price "+_session_1_price);
									System.out.println("2 Session Price "+_session_2_price);
									
									// price 
									double electricity = kwh_1*day_price;
									
									
									double estimate_profit_session_1 = _session_1_price-electricity;
									IndustrialChargerCalculationDTO dtoReport = new IndustrialChargerCalculationDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(kwh_1);
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit_session_1);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(_session_1_price);
									dtoReport.setTime_zone("DAY");
									domestic.add(dtoReport);
									
									double electricityn = kwh_2*peak_price;
									
									double estimate_profitn = _session_2_price-electricityn;
									IndustrialChargerCalculationDTO dtoReport1 = new IndustrialChargerCalculationDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_2_tariff);
									dtoReport1.setCharge_kWh(kwh_2);
									dtoReport1.setElectricity_charge(electricityn);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profitn);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_2_price);
									dtoReport1.setTime_zone("PEAK");
									domestic.add(dtoReport1);
									
									System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 5");
									
								}else if(_charging_start_type == 3 && _charging_end_type ==  1) { //{3,1}
									
									long diffe = end_timestamp.getTime()-start_timestamp.getTime();
									System.out.println(diffe);
									long total_minutes = diffe/(1000*60);
									System.out.println(total_minutes);
									
									long diff_1_tariff = Duration.between(charging_start_time,off_peak_time_end).toMinutes();
									long diff_2_tariff = Duration.between(day_start_tariff, charging_end_time).toMinutes();
									
									System.out.println(diff_1_tariff);
									System.out.println(diff_2_tariff);
									// Calculation Process
									System.out.println(charge_kwh);
									double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
									double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
									
									System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);
									System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
									
									double consume = rs.getDouble("total_consumption_amount");
									System.out.println("total_consume "+consume);
									double _session_1_price = (consume/total_minutes)*diff_1_tariff;
									double _session_2_price = (consume/total_minutes)*diff_2_tariff;
									
									System.out.println("1 Session Price "+_session_1_price);
									System.out.println("2 Session Price "+_session_2_price);
									
									// price 
									double electricity = kwh_1*day_price;
									
									
									double estimate_profit_session_1 = _session_1_price-electricity;
									IndustrialChargerCalculationDTO dtoReport = new IndustrialChargerCalculationDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(kwh_1);
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit_session_1);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(_session_1_price);
									dtoReport.setTime_zone("DAY");
									domestic.add(dtoReport);
									
									double electricityn = kwh_2*peak_price;
									
									double estimate_profitn = _session_2_price-electricityn;
									IndustrialChargerCalculationDTO dtoReport1 = new IndustrialChargerCalculationDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_2_tariff);
									dtoReport1.setCharge_kWh(kwh_2);
									dtoReport1.setElectricity_charge(electricityn);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profitn);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_2_price);
									dtoReport1.setTime_zone("PEAK");
									domestic.add(dtoReport1);
									
									
									
									System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 6");
									
								}else if(_charging_start_type == 1 && _charging_end_type ==  3) { //{1,2,3}
									
									long diffe = end_timestamp.getTime()-start_timestamp.getTime();
									System.out.println(diffe);
									long total_minutes = diffe/(1000*60);
									System.out.println(total_minutes);
									
									long diff_1_tariff = Duration.between(charging_start_time,day_end_tariff).toMinutes();
									long diff_2_tariff = Duration.between(peak_time_start,peak_time_end).toMinutes();
									long diff_3_tariff = Duration.between(off_peak_time_start, charging_end_time).toMinutes();
									
									System.out.println(diff_1_tariff);
									System.out.println(diff_2_tariff);
									// Calculation Process
									System.out.println(charge_kwh);
									double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
									double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
									Double kwh_3 = (charge_kwh/total_minutes)*diff_3_tariff;
									
									System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);									
									System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
									System.out.println("Off peak "+off_peak_price+"-"+kwh_3*off_peak_price);
									
									double consume = rs.getDouble("total_consumption_amount");
									System.out.println("total_consume "+consume);
									double _session_1_price = (consume/total_minutes)*diff_1_tariff;
									double _session_2_price = (consume/total_minutes)*diff_2_tariff;
									double _session_3_price = (consume/total_minutes)*diff_2_tariff;
									System.out.println("1 Session Price "+_session_1_price);
									System.out.println("2 Session Price "+_session_2_price);
									System.out.println("3 Session Price "+_session_3_price);
									// price 
									double electricity = kwh_1*day_price;
									
									
									double estimate_profit_session_1 = _session_1_price-electricity;
									IndustrialChargerCalculationDTO dtoReport = new IndustrialChargerCalculationDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(kwh_1);
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit_session_1);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(_session_1_price);
									dtoReport.setTime_zone("DAY");
									domestic.add(dtoReport);
									
									double electricityn = kwh_2*peak_price;
									
									double estimate_profitn = _session_2_price-electricityn;
									IndustrialChargerCalculationDTO dtoReport1 = new IndustrialChargerCalculationDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_2_tariff);
									dtoReport1.setCharge_kWh(kwh_2);
									dtoReport1.setElectricity_charge(electricityn);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profitn);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_2_price);
									dtoReport1.setTime_zone("PEAK");
									domestic.add(dtoReport1);
									
									double electricity_session_3 = kwh_3*off_peak_price;
									double estimate_profit_sessin_3 = _session_3_price-electricity_session_3;
									IndustrialChargerCalculationDTO dtoReport2 = new IndustrialChargerCalculationDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_3_tariff);
									dtoReport1.setCharge_kWh(kwh_3);
									dtoReport1.setElectricity_charge(electricity_session_3);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profit_sessin_3);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profit_sessin_3);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_3_price);
									dtoReport1.setTime_zone("OFF PEAK");
									domestic.add(dtoReport2);
									
									
									System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 7");
									
								}else if(_charging_start_type == 3 && _charging_end_type ==  2) { //{3,1,2}
									
									long diffe = end_timestamp.getTime()-start_timestamp.getTime();
									System.out.println(diffe);
									long total_minutes = diffe/(1000*60);
									System.out.println(total_minutes);
									
									long diff_1_tariff = Duration.between(charging_start_time,off_peak_time_end).toMinutes();
									long diff_2_tariff = Duration.between(day_start_tariff,day_start_tariff).toMinutes();
									long diff_3_tariff = Duration.between(peak_time_start, charging_end_time).toMinutes();
									
									System.out.println(diff_1_tariff);
									System.out.println(diff_2_tariff);
									// Calculation Process
									System.out.println(charge_kwh);
									double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
									double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
									Double kwh_3 = (charge_kwh/total_minutes)*diff_3_tariff;
									
									
									double consume = rs.getDouble("total_consumption_amount");
									System.out.println("total_consume "+consume);
									double _session_1_price = (consume/total_minutes)*diff_1_tariff;
									double _session_2_price = (consume/total_minutes)*diff_2_tariff;
									double _session_3_price = (consume/total_minutes)*diff_2_tariff;
									System.out.println("1 Session Price "+_session_1_price);
									System.out.println("2 Session Price "+_session_2_price);
									System.out.println("3 Session Price "+_session_3_price);
									// price 
									double electricity = kwh_1*off_peak_price;
									
									
									double estimate_profit_session_1 = _session_1_price-electricity;
									IndustrialChargerCalculationDTO dtoReport = new IndustrialChargerCalculationDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(kwh_1);
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit_session_1);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(_session_1_price);
									dtoReport.setTime_zone("OFF PEAK");
									domestic.add(dtoReport);
									
									double electricityn = kwh_2*day_price;
									
									double estimate_profitn = _session_2_price-electricityn;
									IndustrialChargerCalculationDTO dtoReport1 = new IndustrialChargerCalculationDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_2_tariff);
									dtoReport1.setCharge_kWh(kwh_2);
									dtoReport1.setElectricity_charge(electricityn);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profitn);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_2_price);
									dtoReport1.setTime_zone("DAY");
									domestic.add(dtoReport1);
									
									double electricity_session_3 = kwh_3*peak_price;
									double estimate_profit_sessin_3 = _session_3_price-electricity_session_3;
									IndustrialChargerCalculationDTO dtoReport2 = new IndustrialChargerCalculationDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_3_tariff);
									dtoReport1.setCharge_kWh(kwh_3);
									dtoReport1.setElectricity_charge(electricity_session_3);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profit_sessin_3);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profit_sessin_3);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_3_price);
									dtoReport1.setTime_zone("PEAK");
									domestic.add(dtoReport2);
									
									
									System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 8");
									
								}else if(_charging_start_type == 2 && _charging_end_type ==  1) { //{2,3,1}
									
									long diffe = end_timestamp.getTime()-start_timestamp.getTime();
									System.out.println(diffe);
									long total_minutes = diffe/(1000*60);
									System.out.println(total_minutes);
									
									long diff_1_tariff = Duration.between(charging_start_time,peak_time_end).toMinutes();
									long diff_2_tariff = Duration.between(off_peak_time_start,off_peak_time_end).toMinutes();
									long diff_3_tariff = Duration.between(day_start_tariff, charging_end_time).toMinutes();
									
									System.out.println(diff_1_tariff);
									System.out.println(diff_2_tariff);
									// Calculation Process
									System.out.println(charge_kwh);
									double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
									double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
									Double kwh_3 = (charge_kwh/total_minutes)*diff_3_tariff;
									
									
									double consume = rs.getDouble("total_consumption_amount");
									System.out.println("total_consume "+consume);
									double _session_1_price = (consume/total_minutes)*diff_1_tariff;
									double _session_2_price = (consume/total_minutes)*diff_2_tariff;
									double _session_3_price = (consume/total_minutes)*diff_2_tariff;
									System.out.println("1 Session Price "+_session_1_price);
									System.out.println("2 Session Price "+_session_2_price);
									System.out.println("3 Session Price "+_session_3_price);
									// price 
									double electricity = kwh_1*peak_price;
									
									
									double estimate_profit_session_1 = _session_1_price-electricity;
									IndustrialChargerCalculationDTO dtoReport = new IndustrialChargerCalculationDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(kwh_1);
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit_session_1);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(_session_1_price);
									dtoReport.setTime_zone("PEAK");
									domestic.add(dtoReport);
									
									double electricityn = kwh_2*off_peak_price;
									
									double estimate_profitn = _session_2_price-electricityn;
									IndustrialChargerCalculationDTO dtoReport1 = new IndustrialChargerCalculationDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_2_tariff);
									dtoReport1.setCharge_kWh(kwh_2);
									dtoReport1.setElectricity_charge(electricityn);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profitn);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_2_price);
									dtoReport1.setTime_zone("OFF PEAK");
									domestic.add(dtoReport1);
									
									double electricity_session_3 = kwh_3*day_price;
									double estimate_profit_sessin_3 = _session_3_price-electricity_session_3;
									IndustrialChargerCalculationDTO dtoReport2 = new IndustrialChargerCalculationDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_3_tariff);
									dtoReport1.setCharge_kWh(kwh_3);
									dtoReport1.setElectricity_charge(electricity_session_3);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profit_sessin_3);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profit_sessin_3);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_3_price);
									dtoReport1.setTime_zone("DAY");
									domestic.add(dtoReport2);
									
									System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 9");
								}
																							
						}
							
							
							
						}else if(resultSet.getDouble("total") > 300) {
							
							String  sqlcategory = "SELECT * FROM other_consumer WHERE type = 2";
							PreparedStatement psn = con.prepareStatement(sqlcategory);
							ResultSet rsn = psn.executeQuery();
							//System.out.println(rsn.get);
							
							while(rsn.next()) {
								
								day_price = rsn.getDouble("day_price");
								peak_price = rsn.getDouble("peak_price");
								off_peak_price = rsn.getDouble("off_peak_price");
								fixed_charge =  rsn.getDouble("fixed_charge");
								demand_charge =  rsn.getDouble("demand_charge");
							}
							
							while(rs.next()) {
								//check Day
								Util util = new Util();
								charge_kwh = rs.getDouble("charge_kwh");
								//charging start time
								Timestamp start_timestamp = rs.getTimestamp("start_charging_time");
								LocalTime charging_start_time = start_timestamp.toLocalDateTime().toLocalTime();
								
								//charging end time 
								Timestamp end_timestamp = rs.getTimestamp("end_charging_time");
								LocalTime charging_end_time = end_timestamp.toLocalDateTime().toLocalTime();
								
								LocalTime day_start_tariff = LocalTime.parse(util.DAY_TIME_START);
								LocalTime day_end_tariff = LocalTime.parse(util.DAY_TIME_END);
								
								LocalTime peak_time_start = LocalTime.parse(util.PEAK_TIME_START);
								LocalTime peak_time_end = LocalTime.parse(util.PEAK_TIME_END);
								
								LocalTime off_peak_time_start = LocalTime.parse(util.OFF_PEAK_TIME_START);
								LocalTime off_peak_time_end = LocalTime.parse(util.OFF_PEAK_TIME_END);
								
								// FIRST CHECK START AND END TIME IN WHICH TARIFF
								
								int _charging_start_type = 0; // 1 = DAY, 2 = PEAK , 3 = OFF PEAK 
								int _charging_end_type = 0; // 1 = DAY, 2 = PEAK , 3 = OFF PEAK 
								
								if(charging_start_time.isAfter(day_start_tariff) && charging_start_time.isBefore(peak_time_start)) { // DAY
									
									_charging_start_type = 1;
																		
								}else if(charging_start_time.isAfter(peak_time_start) && charging_start_time.isBefore(off_peak_time_start)) { // PEAKE
									
									_charging_start_type = 2;
									
								}else if(charging_start_time.isAfter(off_peak_time_start) && charging_start_time.isBefore(day_start_tariff)) { // OFF PEAKE
									
									_charging_start_type = 3;
									
								}
								
								
								if(charging_end_time.isAfter(day_start_tariff) && charging_end_time.isBefore(peak_time_start)) { // DAY
									
									_charging_end_type = 1;
																		
								}else if(charging_end_time.isAfter(peak_time_start) && charging_end_time.isBefore(off_peak_time_start)) { // PEAKE
									
									_charging_end_type = 2;
									
								}else if(charging_end_time.isAfter(off_peak_time_start) && charging_end_time.isBefore(day_start_tariff)) { // OFF PEAKE
									
									_charging_end_type = 3;
									
								}
								
								
								//THE CYCLE PROCESS 
								
								if(_charging_start_type == 1 && _charging_end_type ==  1) { //{1}
																									
									long diff_1_tariff = Duration.between(charging_start_time,charging_end_time).toMinutes();
									double electricity = rs.getDouble("charge_kwh")*day_price;
									double consume = rs.getDouble("total_consumption_amount");
									double estimate_profit = consume-electricity;
									IndustrialChargerCalculationDTO dtoReport = new IndustrialChargerCalculationDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(rs.getDouble("charge_kwh"));
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(consume);
									dtoReport.setTime_zone("DAY");
									domestic.add(dtoReport);
									
									// System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 1");
									
								}else if(_charging_start_type == 2 && _charging_end_type ==  2) { //{2}
									
									long diff_1_tariff = Duration.between(charging_start_time,charging_end_time).toMinutes();
									double electricity = rs.getDouble("charge_kwh")*peak_price;
									double consume = rs.getDouble("total_consumption_amount");
									double estimate_profit = consume-electricity;
									IndustrialChargerCalculationDTO dtoReport = new IndustrialChargerCalculationDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(rs.getDouble("charge_kwh"));
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(consume);
									dtoReport.setTime_zone("PEAK");
									domestic.add(dtoReport);
									
									// System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 2");
									
								}else if(_charging_start_type == 3 && _charging_end_type ==  3) { //{3}
									
									long diff_1_tariff = Duration.between(charging_start_time,charging_end_time).toMinutes();
									double electricity = rs.getDouble("charge_kwh")*off_peak_price;
									double consume = rs.getDouble("total_consumption_amount");
									double estimate_profit = consume-electricity;
									IndustrialChargerCalculationDTO dtoReport = new IndustrialChargerCalculationDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(rs.getDouble("charge_kwh"));
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(consume);
									dtoReport.setTime_zone("OFF PEAK");
									domestic.add(dtoReport);
									
								//	System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 3");
									
								}else if(_charging_start_type == 1 && _charging_end_type ==  2) { //{1,2}
									
									long diffe = end_timestamp.getTime()-start_timestamp.getTime();
									System.out.println(diffe);
									long total_minutes = diffe/(1000*60);
									System.out.println(total_minutes);
									
									long diff_1_tariff = Duration.between(charging_start_time,day_end_tariff).toMinutes();
									long diff_2_tariff = Duration.between(peak_time_start, charging_end_time).toMinutes();
									
									System.out.println(diff_1_tariff);
									System.out.println(diff_2_tariff);
									// Calculation Process
									System.out.println(charge_kwh);
									double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
									double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
									
									System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);
									System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
									
									double consume = rs.getDouble("total_consumption_amount");
									System.out.println("total_consume "+consume);
									double _session_1_price = (consume/total_minutes)*diff_1_tariff;
									double _session_2_price = (consume/total_minutes)*diff_2_tariff;
									
									System.out.println("1 Session Price "+_session_1_price);
									System.out.println("2 Session Price "+_session_2_price);
									
									// price 
									double electricity = kwh_1*day_price;
									
									
									double estimate_profit_session_1 = _session_1_price-electricity;
									IndustrialChargerCalculationDTO dtoReport = new IndustrialChargerCalculationDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(kwh_1);
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit_session_1);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(_session_1_price);
									dtoReport.setTime_zone("DAY");
									domestic.add(dtoReport);
									
									double electricityn = kwh_2*peak_price;
									
									double estimate_profitn = _session_2_price-electricityn;
									IndustrialChargerCalculationDTO dtoReport1 = new IndustrialChargerCalculationDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_2_tariff);
									dtoReport1.setCharge_kWh(kwh_2);
									dtoReport1.setElectricity_charge(electricityn);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profitn);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_2_price);
									dtoReport1.setTime_zone("PEAK");
									domestic.add(dtoReport1);
									
									
									System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 4");
																
								}else if(_charging_start_type == 2 && _charging_end_type ==  3) { //{2,3}
									
									long diffe = end_timestamp.getTime()-start_timestamp.getTime();
									System.out.println(diffe);
									long total_minutes = diffe/(1000*60);
									System.out.println(total_minutes);
									
									long diff_1_tariff = Duration.between(charging_start_time,peak_time_end).toMinutes();
									long diff_2_tariff = Duration.between(off_peak_time_start, charging_end_time).toMinutes();
									
									System.out.println(diff_1_tariff);
									System.out.println(diff_2_tariff);
									// Calculation Process
									System.out.println(charge_kwh);
									double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
									double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
									
									System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);
									System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
									
									double consume = rs.getDouble("total_consumption_amount");
									System.out.println("total_consume "+consume);
									double _session_1_price = (consume/total_minutes)*diff_1_tariff;
									double _session_2_price = (consume/total_minutes)*diff_2_tariff;
									
									System.out.println("1 Session Price "+_session_1_price);
									System.out.println("2 Session Price "+_session_2_price);
									
									// price 
									double electricity = kwh_1*day_price;
									
									
									double estimate_profit_session_1 = _session_1_price-electricity;
									IndustrialChargerCalculationDTO dtoReport = new IndustrialChargerCalculationDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(kwh_1);
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit_session_1);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(_session_1_price);
									dtoReport.setTime_zone("DAY");
									domestic.add(dtoReport);
									
									double electricityn = kwh_2*peak_price;
									
									double estimate_profitn = _session_2_price-electricityn;
									IndustrialChargerCalculationDTO dtoReport1 = new IndustrialChargerCalculationDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_2_tariff);
									dtoReport1.setCharge_kWh(kwh_2);
									dtoReport1.setElectricity_charge(electricityn);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profitn);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_2_price);
									dtoReport1.setTime_zone("PEAK");
									domestic.add(dtoReport1);
									
									System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 5");
									
								}else if(_charging_start_type == 3 && _charging_end_type ==  1) { //{3,1}
									
									long diffe = end_timestamp.getTime()-start_timestamp.getTime();
									System.out.println(diffe);
									long total_minutes = diffe/(1000*60);
									System.out.println(total_minutes);
									
									long diff_1_tariff = Duration.between(charging_start_time,off_peak_time_end).toMinutes();
									long diff_2_tariff = Duration.between(day_start_tariff, charging_end_time).toMinutes();
									
									System.out.println(diff_1_tariff);
									System.out.println(diff_2_tariff);
									// Calculation Process
									System.out.println(charge_kwh);
									double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
									double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
									
									System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);
									System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
									
									double consume = rs.getDouble("total_consumption_amount");
									System.out.println("total_consume "+consume);
									double _session_1_price = (consume/total_minutes)*diff_1_tariff;
									double _session_2_price = (consume/total_minutes)*diff_2_tariff;
									
									System.out.println("1 Session Price "+_session_1_price);
									System.out.println("2 Session Price "+_session_2_price);
									
									// price 
									double electricity = kwh_1*day_price;
									
									
									double estimate_profit_session_1 = _session_1_price-electricity;
									IndustrialChargerCalculationDTO dtoReport = new IndustrialChargerCalculationDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(kwh_1);
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit_session_1);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(_session_1_price);
									dtoReport.setTime_zone("DAY");
									domestic.add(dtoReport);
									
									double electricityn = kwh_2*peak_price;
									
									double estimate_profitn = _session_2_price-electricityn;
									IndustrialChargerCalculationDTO dtoReport1 = new IndustrialChargerCalculationDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_2_tariff);
									dtoReport1.setCharge_kWh(kwh_2);
									dtoReport1.setElectricity_charge(electricityn);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profitn);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_2_price);
									dtoReport1.setTime_zone("PEAK");
									domestic.add(dtoReport1);
									
									
									
									System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 6");
									
								}else if(_charging_start_type == 1 && _charging_end_type ==  3) { //{1,2,3}
									
									long diffe = end_timestamp.getTime()-start_timestamp.getTime();
									System.out.println(diffe);
									long total_minutes = diffe/(1000*60);
									System.out.println(total_minutes);
									
									long diff_1_tariff = Duration.between(charging_start_time,day_end_tariff).toMinutes();
									long diff_2_tariff = Duration.between(peak_time_start,peak_time_end).toMinutes();
									long diff_3_tariff = Duration.between(off_peak_time_start, charging_end_time).toMinutes();
									
									System.out.println(diff_1_tariff);
									System.out.println(diff_2_tariff);
									// Calculation Process
									System.out.println(charge_kwh);
									double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
									double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
									Double kwh_3 = (charge_kwh/total_minutes)*diff_3_tariff;
									
									System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);									
									System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
									System.out.println("Off peak "+off_peak_price+"-"+kwh_3*off_peak_price);
									
									double consume = rs.getDouble("total_consumption_amount");
									System.out.println("total_consume "+consume);
									double _session_1_price = (consume/total_minutes)*diff_1_tariff;
									double _session_2_price = (consume/total_minutes)*diff_2_tariff;
									double _session_3_price = (consume/total_minutes)*diff_2_tariff;
									System.out.println("1 Session Price "+_session_1_price);
									System.out.println("2 Session Price "+_session_2_price);
									System.out.println("3 Session Price "+_session_3_price);
									// price 
									double electricity = kwh_1*day_price;
									
									
									double estimate_profit_session_1 = _session_1_price-electricity;
									IndustrialChargerCalculationDTO dtoReport = new IndustrialChargerCalculationDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(kwh_1);
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit_session_1);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(_session_1_price);
									dtoReport.setTime_zone("DAY");
									domestic.add(dtoReport);
									
									double electricityn = kwh_2*peak_price;
									
									double estimate_profitn = _session_2_price-electricityn;
									IndustrialChargerCalculationDTO dtoReport1 = new IndustrialChargerCalculationDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_2_tariff);
									dtoReport1.setCharge_kWh(kwh_2);
									dtoReport1.setElectricity_charge(electricityn);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profitn);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_2_price);
									dtoReport1.setTime_zone("PEAK");
									domestic.add(dtoReport1);
									
									double electricity_session_3 = kwh_3*off_peak_price;
									double estimate_profit_sessin_3 = _session_3_price-electricity_session_3;
									IndustrialChargerCalculationDTO dtoReport2 = new IndustrialChargerCalculationDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_3_tariff);
									dtoReport1.setCharge_kWh(kwh_3);
									dtoReport1.setElectricity_charge(electricity_session_3);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profit_sessin_3);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profit_sessin_3);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_3_price);
									dtoReport1.setTime_zone("OFF PEAK");
									domestic.add(dtoReport2);
									
									
									System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 7");
									
								}else if(_charging_start_type == 3 && _charging_end_type ==  2) { //{3,1,2}
									
									long diffe = end_timestamp.getTime()-start_timestamp.getTime();
									System.out.println(diffe);
									long total_minutes = diffe/(1000*60);
									System.out.println(total_minutes);
									
									long diff_1_tariff = Duration.between(charging_start_time,off_peak_time_end).toMinutes();
									long diff_2_tariff = Duration.between(day_start_tariff,day_start_tariff).toMinutes();
									long diff_3_tariff = Duration.between(peak_time_start, charging_end_time).toMinutes();
									
									System.out.println(diff_1_tariff);
									System.out.println(diff_2_tariff);
									// Calculation Process
									System.out.println(charge_kwh);
									double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
									double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
									Double kwh_3 = (charge_kwh/total_minutes)*diff_3_tariff;
									
									
									double consume = rs.getDouble("total_consumption_amount");
									System.out.println("total_consume "+consume);
									double _session_1_price = (consume/total_minutes)*diff_1_tariff;
									double _session_2_price = (consume/total_minutes)*diff_2_tariff;
									double _session_3_price = (consume/total_minutes)*diff_2_tariff;
									System.out.println("1 Session Price "+_session_1_price);
									System.out.println("2 Session Price "+_session_2_price);
									System.out.println("3 Session Price "+_session_3_price);
									// price 
									double electricity = kwh_1*off_peak_price;
									
									
									double estimate_profit_session_1 = _session_1_price-electricity;
									IndustrialChargerCalculationDTO dtoReport = new IndustrialChargerCalculationDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(kwh_1);
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit_session_1);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(_session_1_price);
									dtoReport.setTime_zone("OFF PEAK");
									domestic.add(dtoReport);
									
									double electricityn = kwh_2*day_price;
									
									double estimate_profitn = _session_2_price-electricityn;
									IndustrialChargerCalculationDTO dtoReport1 = new IndustrialChargerCalculationDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_2_tariff);
									dtoReport1.setCharge_kWh(kwh_2);
									dtoReport1.setElectricity_charge(electricityn);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profitn);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_2_price);
									dtoReport1.setTime_zone("DAY");
									domestic.add(dtoReport1);
									
									double electricity_session_3 = kwh_3*peak_price;
									double estimate_profit_sessin_3 = _session_3_price-electricity_session_3;
									IndustrialChargerCalculationDTO dtoReport2 = new IndustrialChargerCalculationDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_3_tariff);
									dtoReport1.setCharge_kWh(kwh_3);
									dtoReport1.setElectricity_charge(electricity_session_3);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profit_sessin_3);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profit_sessin_3);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_3_price);
									dtoReport1.setTime_zone("PEAK");
									domestic.add(dtoReport2);
									
									
									System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 8");
									
								}else if(_charging_start_type == 2 && _charging_end_type ==  1) { //{2,3,1}
									
									long diffe = end_timestamp.getTime()-start_timestamp.getTime();
									System.out.println(diffe);
									long total_minutes = diffe/(1000*60);
									System.out.println(total_minutes);
									
									long diff_1_tariff = Duration.between(charging_start_time,peak_time_end).toMinutes();
									long diff_2_tariff = Duration.between(off_peak_time_start,off_peak_time_end).toMinutes();
									long diff_3_tariff = Duration.between(day_start_tariff, charging_end_time).toMinutes();
									
									System.out.println(diff_1_tariff);
									System.out.println(diff_2_tariff);
									// Calculation Process
									System.out.println(charge_kwh);
									double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
									double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
									Double kwh_3 = (charge_kwh/total_minutes)*diff_3_tariff;
									
									
									double consume = rs.getDouble("total_consumption_amount");
									System.out.println("total_consume "+consume);
									double _session_1_price = (consume/total_minutes)*diff_1_tariff;
									double _session_2_price = (consume/total_minutes)*diff_2_tariff;
									double _session_3_price = (consume/total_minutes)*diff_2_tariff;
									System.out.println("1 Session Price "+_session_1_price);
									System.out.println("2 Session Price "+_session_2_price);
									System.out.println("3 Session Price "+_session_3_price);
									// price 
									double electricity = kwh_1*peak_price;
									
									
									double estimate_profit_session_1 = _session_1_price-electricity;
									IndustrialChargerCalculationDTO dtoReport = new IndustrialChargerCalculationDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(kwh_1);
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit_session_1);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(_session_1_price);
									dtoReport.setTime_zone("PEAK");
									domestic.add(dtoReport);
									
									double electricityn = kwh_2*off_peak_price;
									
									double estimate_profitn = _session_2_price-electricityn;
									IndustrialChargerCalculationDTO dtoReport1 = new IndustrialChargerCalculationDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_2_tariff);
									dtoReport1.setCharge_kWh(kwh_2);
									dtoReport1.setElectricity_charge(electricityn);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profitn);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_2_price);
									dtoReport1.setTime_zone("OFF PEAK");
									domestic.add(dtoReport1);
									
									double electricity_session_3 = kwh_3*day_price;
									double estimate_profit_sessin_3 = _session_3_price-electricity_session_3;
									IndustrialChargerCalculationDTO dtoReport2 = new IndustrialChargerCalculationDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_3_tariff);
									dtoReport1.setCharge_kWh(kwh_3);
									dtoReport1.setElectricity_charge(electricity_session_3);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profit_sessin_3);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profit_sessin_3);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_3_price);
									dtoReport1.setTime_zone("DAY");
									domestic.add(dtoReport2);
									
									System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 9");
								}
																							
						}
						
				}
			
				}
		
		}catch(Exception e) {
				e.printStackTrace();
		}
		return domestic;
	}
		
public List<IndustrialChargerCalculationDTO> getChargerIndustrialDataII(String station,String start_date,String end_date,int category,int subcategory,String gun_type){
		
		String sql="SELECT * FROM charge_order_information WHERE start_charging_time >= ? AND end_charging_time <= ? AND station_name = ?";
		List<IndustrialChargerCalculationDTO> domestic = new ArrayList<>();
		
		double price_per_unit  = 0.0;
		
		try {
			Connection con = DBConnection.getConnection();
			PreparedStatement ps = con.prepareStatement(sql);			
				ps.setString(1, start_date);
				ps.setString(2, end_date);
				ps.setString(3, station);		
				ResultSet rs = ps.executeQuery();	
				
				
				double charge_kwh = 0.0;
				double day_price = 0.0;
				double peak_price = 0.0;
				double off_peak_price = 0.0;
				double fixed_charge = 0.0;
				double demand_charge = 0.0;
				
		
		String  sqlcategory = "SELECT * FROM other_consumer WHERE type = 3";
							PreparedStatement psn = con.prepareStatement(sqlcategory);
							ResultSet rsn = psn.executeQuery();
							//System.out.println(rsn.get);
							
							while(rsn.next()) {
								
								day_price = rsn.getDouble("day_price");
								peak_price = rsn.getDouble("peak_price");
								off_peak_price = rsn.getDouble("off_peak_price");
								fixed_charge =  rsn.getDouble("fixed_charge");
								demand_charge =  rsn.getDouble("demand_charge");
							}
							
							while(rs.next()) {
								//check Day
								Util util = new Util();
								charge_kwh = rs.getDouble("charge_kwh");
								//charging start time
								Timestamp start_timestamp = rs.getTimestamp("start_charging_time");
								LocalTime charging_start_time = start_timestamp.toLocalDateTime().toLocalTime();
								
								//charging end time 
								Timestamp end_timestamp = rs.getTimestamp("end_charging_time");
								LocalTime charging_end_time = end_timestamp.toLocalDateTime().toLocalTime();
								
								LocalTime day_start_tariff = LocalTime.parse(util.DAY_TIME_START);
								LocalTime day_end_tariff = LocalTime.parse(util.DAY_TIME_END);
								
								LocalTime peak_time_start = LocalTime.parse(util.PEAK_TIME_START);
								LocalTime peak_time_end = LocalTime.parse(util.PEAK_TIME_END);
								
								LocalTime off_peak_time_start = LocalTime.parse(util.OFF_PEAK_TIME_START);
								LocalTime off_peak_time_end = LocalTime.parse(util.OFF_PEAK_TIME_END);
								
								// FIRST CHECK START AND END TIME IN WHICH TARIFF
								
								int _charging_start_type = 0; // 1 = DAY, 2 = PEAK , 3 = OFF PEAK 
								int _charging_end_type = 0; // 1 = DAY, 2 = PEAK , 3 = OFF PEAK 
								
								if(charging_start_time.isAfter(day_start_tariff) && charging_start_time.isBefore(peak_time_start)) { // DAY
									
									_charging_start_type = 1;
																		
								}else if(charging_start_time.isAfter(peak_time_start) && charging_start_time.isBefore(off_peak_time_start)) { // PEAKE
									
									_charging_start_type = 2;
									
								}else if(charging_start_time.isAfter(off_peak_time_start) && charging_start_time.isBefore(day_start_tariff)) { // OFF PEAKE
									
									_charging_start_type = 3;
									
								}
								
								
								if(charging_end_time.isAfter(day_start_tariff) && charging_end_time.isBefore(peak_time_start)) { // DAY
									
									_charging_end_type = 1;
																		
								}else if(charging_end_time.isAfter(peak_time_start) && charging_end_time.isBefore(off_peak_time_start)) { // PEAKE
									
									_charging_end_type = 2;
									
								}else if(charging_end_time.isAfter(off_peak_time_start) && charging_end_time.isBefore(day_start_tariff)) { // OFF PEAKE
									
									_charging_end_type = 3;
									
								}
								
								
								//THE CYCLE PROCESS 
								
								if(_charging_start_type == 1 && _charging_end_type ==  1) { //{1}
																									
									long diff_1_tariff = Duration.between(charging_start_time,charging_end_time).toMinutes();
									double electricity = rs.getDouble("charge_kwh")*day_price;
									double consume = rs.getDouble("total_consumption_amount");
									double estimate_profit = consume-electricity;
									IndustrialChargerCalculationDTO dtoReport = new IndustrialChargerCalculationDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(rs.getDouble("charge_kwh"));
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(consume);
									dtoReport.setTime_zone("DAY");
									domestic.add(dtoReport);
									
									// System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 1");
									
								}else if(_charging_start_type == 2 && _charging_end_type ==  2) { //{2}
									
									long diff_1_tariff = Duration.between(charging_start_time,charging_end_time).toMinutes();
									double electricity = rs.getDouble("charge_kwh")*peak_price;
									double consume = rs.getDouble("total_consumption_amount");
									double estimate_profit = consume-electricity;
									IndustrialChargerCalculationDTO dtoReport = new IndustrialChargerCalculationDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(rs.getDouble("charge_kwh"));
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(consume);
									dtoReport.setTime_zone("PEAK");
									domestic.add(dtoReport);
									
									// System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 2");
									
								}else if(_charging_start_type == 3 && _charging_end_type ==  3) { //{3}
									
									long diff_1_tariff = Duration.between(charging_start_time,charging_end_time).toMinutes();
									double electricity = rs.getDouble("charge_kwh")*off_peak_price;
									double consume = rs.getDouble("total_consumption_amount");
									double estimate_profit = consume-electricity;
									IndustrialChargerCalculationDTO dtoReport = new IndustrialChargerCalculationDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(rs.getDouble("charge_kwh"));
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(consume);
									dtoReport.setTime_zone("OFF PEAK");
									domestic.add(dtoReport);
									
								//	System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 3");
									
								}else if(_charging_start_type == 1 && _charging_end_type ==  2) { //{1,2}
									
									long diffe = end_timestamp.getTime()-start_timestamp.getTime();
									System.out.println(diffe);
									long total_minutes = diffe/(1000*60);
									System.out.println(total_minutes);
									
									long diff_1_tariff = Duration.between(charging_start_time,day_end_tariff).toMinutes();
									long diff_2_tariff = Duration.between(peak_time_start, charging_end_time).toMinutes();
									
									System.out.println(diff_1_tariff);
									System.out.println(diff_2_tariff);
									// Calculation Process
									System.out.println(charge_kwh);
									double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
									double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
									
									System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);
									System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
									
									double consume = rs.getDouble("total_consumption_amount");
									System.out.println("total_consume "+consume);
									double _session_1_price = (consume/total_minutes)*diff_1_tariff;
									double _session_2_price = (consume/total_minutes)*diff_2_tariff;
									
									System.out.println("1 Session Price "+_session_1_price);
									System.out.println("2 Session Price "+_session_2_price);
									
									// price 
									double electricity = kwh_1*day_price;
									
									
									double estimate_profit_session_1 = _session_1_price-electricity;
									IndustrialChargerCalculationDTO dtoReport = new IndustrialChargerCalculationDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(kwh_1);
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit_session_1);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(_session_1_price);
									dtoReport.setTime_zone("DAY");
									domestic.add(dtoReport);
									
									double electricityn = kwh_2*peak_price;
									
									double estimate_profitn = _session_2_price-electricityn;
									IndustrialChargerCalculationDTO dtoReport1 = new IndustrialChargerCalculationDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_2_tariff);
									dtoReport1.setCharge_kWh(kwh_2);
									dtoReport1.setElectricity_charge(electricityn);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profitn);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_2_price);
									dtoReport1.setTime_zone("PEAK");
									domestic.add(dtoReport1);
									
									
									System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 4");
																
								}else if(_charging_start_type == 2 && _charging_end_type ==  3) { //{2,3}
									
									long diffe = end_timestamp.getTime()-start_timestamp.getTime();
									System.out.println(diffe);
									long total_minutes = diffe/(1000*60);
									System.out.println(total_minutes);
									
									long diff_1_tariff = Duration.between(charging_start_time,peak_time_end).toMinutes();
									long diff_2_tariff = Duration.between(off_peak_time_start, charging_end_time).toMinutes();
									
									System.out.println(diff_1_tariff);
									System.out.println(diff_2_tariff);
									// Calculation Process
									System.out.println(charge_kwh);
									double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
									double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
									
									System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);
									System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
									
									double consume = rs.getDouble("total_consumption_amount");
									System.out.println("total_consume "+consume);
									double _session_1_price = (consume/total_minutes)*diff_1_tariff;
									double _session_2_price = (consume/total_minutes)*diff_2_tariff;
									
									System.out.println("1 Session Price "+_session_1_price);
									System.out.println("2 Session Price "+_session_2_price);
									
									// price 
									double electricity = kwh_1*day_price;
									
									
									double estimate_profit_session_1 = _session_1_price-electricity;
									IndustrialChargerCalculationDTO dtoReport = new IndustrialChargerCalculationDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(kwh_1);
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit_session_1);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(_session_1_price);
									dtoReport.setTime_zone("DAY");
									domestic.add(dtoReport);
									
									double electricityn = kwh_2*peak_price;
									
									double estimate_profitn = _session_2_price-electricityn;
									IndustrialChargerCalculationDTO dtoReport1 = new IndustrialChargerCalculationDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_2_tariff);
									dtoReport1.setCharge_kWh(kwh_2);
									dtoReport1.setElectricity_charge(electricityn);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profitn);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_2_price);
									dtoReport1.setTime_zone("PEAK");
									domestic.add(dtoReport1);
									
									System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 5");
									
								}else if(_charging_start_type == 3 && _charging_end_type ==  1) { //{3,1}
									
									long diffe = end_timestamp.getTime()-start_timestamp.getTime();
									System.out.println(diffe);
									long total_minutes = diffe/(1000*60);
									System.out.println(total_minutes);
									
									long diff_1_tariff = Duration.between(charging_start_time,off_peak_time_end).toMinutes();
									long diff_2_tariff = Duration.between(day_start_tariff, charging_end_time).toMinutes();
									
									System.out.println(diff_1_tariff);
									System.out.println(diff_2_tariff);
									// Calculation Process
									System.out.println(charge_kwh);
									double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
									double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
									
									System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);
									System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
									
									double consume = rs.getDouble("total_consumption_amount");
									System.out.println("total_consume "+consume);
									double _session_1_price = (consume/total_minutes)*diff_1_tariff;
									double _session_2_price = (consume/total_minutes)*diff_2_tariff;
									
									System.out.println("1 Session Price "+_session_1_price);
									System.out.println("2 Session Price "+_session_2_price);
									
									// price 
									double electricity = kwh_1*day_price;
									
									
									double estimate_profit_session_1 = _session_1_price-electricity;
									IndustrialChargerCalculationDTO dtoReport = new IndustrialChargerCalculationDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(kwh_1);
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit_session_1);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(_session_1_price);
									dtoReport.setTime_zone("DAY");
									domestic.add(dtoReport);
									
									double electricityn = kwh_2*peak_price;
									
									double estimate_profitn = _session_2_price-electricityn;
									IndustrialChargerCalculationDTO dtoReport1 = new IndustrialChargerCalculationDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_2_tariff);
									dtoReport1.setCharge_kWh(kwh_2);
									dtoReport1.setElectricity_charge(electricityn);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profitn);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_2_price);
									dtoReport1.setTime_zone("PEAK");
									domestic.add(dtoReport1);
									
									
									
									System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 6");
									
								}else if(_charging_start_type == 1 && _charging_end_type ==  3) { //{1,2,3}
									
									long diffe = end_timestamp.getTime()-start_timestamp.getTime();
									System.out.println(diffe);
									long total_minutes = diffe/(1000*60);
									System.out.println(total_minutes);
									
									long diff_1_tariff = Duration.between(charging_start_time,day_end_tariff).toMinutes();
									long diff_2_tariff = Duration.between(peak_time_start,peak_time_end).toMinutes();
									long diff_3_tariff = Duration.between(off_peak_time_start, charging_end_time).toMinutes();
									
									System.out.println(diff_1_tariff);
									System.out.println(diff_2_tariff);
									// Calculation Process
									System.out.println(charge_kwh);
									double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
									double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
									Double kwh_3 = (charge_kwh/total_minutes)*diff_3_tariff;
									
									System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);									
									System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
									System.out.println("Off peak "+off_peak_price+"-"+kwh_3*off_peak_price);
									
									double consume = rs.getDouble("total_consumption_amount");
									System.out.println("total_consume "+consume);
									double _session_1_price = (consume/total_minutes)*diff_1_tariff;
									double _session_2_price = (consume/total_minutes)*diff_2_tariff;
									double _session_3_price = (consume/total_minutes)*diff_2_tariff;
									System.out.println("1 Session Price "+_session_1_price);
									System.out.println("2 Session Price "+_session_2_price);
									System.out.println("3 Session Price "+_session_3_price);
									// price 
									double electricity = kwh_1*day_price;
									
									
									double estimate_profit_session_1 = _session_1_price-electricity;
									IndustrialChargerCalculationDTO dtoReport = new IndustrialChargerCalculationDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(kwh_1);
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit_session_1);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(_session_1_price);
									dtoReport.setTime_zone("DAY");
									domestic.add(dtoReport);
									
									double electricityn = kwh_2*peak_price;
									
									double estimate_profitn = _session_2_price-electricityn;
									IndustrialChargerCalculationDTO dtoReport1 = new IndustrialChargerCalculationDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_2_tariff);
									dtoReport1.setCharge_kWh(kwh_2);
									dtoReport1.setElectricity_charge(electricityn);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profitn);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_2_price);
									dtoReport1.setTime_zone("PEAK");
									domestic.add(dtoReport1);
									
									double electricity_session_3 = kwh_3*off_peak_price;
									double estimate_profit_sessin_3 = _session_3_price-electricity_session_3;
									IndustrialChargerCalculationDTO dtoReport2 = new IndustrialChargerCalculationDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_3_tariff);
									dtoReport1.setCharge_kWh(kwh_3);
									dtoReport1.setElectricity_charge(electricity_session_3);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profit_sessin_3);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profit_sessin_3);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_3_price);
									dtoReport1.setTime_zone("OFF PEAK");
									domestic.add(dtoReport2);
									
									
									System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 7");
									
								}else if(_charging_start_type == 3 && _charging_end_type ==  2) { //{3,1,2}
									
									long diffe = end_timestamp.getTime()-start_timestamp.getTime();
									System.out.println(diffe);
									long total_minutes = diffe/(1000*60);
									System.out.println(total_minutes);
									
									long diff_1_tariff = Duration.between(charging_start_time,off_peak_time_end).toMinutes();
									long diff_2_tariff = Duration.between(day_start_tariff,day_start_tariff).toMinutes();
									long diff_3_tariff = Duration.between(peak_time_start, charging_end_time).toMinutes();
									
									System.out.println(diff_1_tariff);
									System.out.println(diff_2_tariff);
									// Calculation Process
									System.out.println(charge_kwh);
									double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
									double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
									Double kwh_3 = (charge_kwh/total_minutes)*diff_3_tariff;
									
									
									double consume = rs.getDouble("total_consumption_amount");
									System.out.println("total_consume "+consume);
									double _session_1_price = (consume/total_minutes)*diff_1_tariff;
									double _session_2_price = (consume/total_minutes)*diff_2_tariff;
									double _session_3_price = (consume/total_minutes)*diff_2_tariff;
									System.out.println("1 Session Price "+_session_1_price);
									System.out.println("2 Session Price "+_session_2_price);
									System.out.println("3 Session Price "+_session_3_price);
									// price 
									double electricity = kwh_1*off_peak_price;
									
									
									double estimate_profit_session_1 = _session_1_price-electricity;
									IndustrialChargerCalculationDTO dtoReport = new IndustrialChargerCalculationDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(kwh_1);
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit_session_1);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(_session_1_price);
									dtoReport.setTime_zone("OFF PEAK");
									domestic.add(dtoReport);
									
									double electricityn = kwh_2*day_price;
									
									double estimate_profitn = _session_2_price-electricityn;
									IndustrialChargerCalculationDTO dtoReport1 = new IndustrialChargerCalculationDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_2_tariff);
									dtoReport1.setCharge_kWh(kwh_2);
									dtoReport1.setElectricity_charge(electricityn);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profitn);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_2_price);
									dtoReport1.setTime_zone("DAY");
									domestic.add(dtoReport1);
									
									double electricity_session_3 = kwh_3*peak_price;
									double estimate_profit_sessin_3 = _session_3_price-electricity_session_3;
									IndustrialChargerCalculationDTO dtoReport2 = new IndustrialChargerCalculationDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_3_tariff);
									dtoReport1.setCharge_kWh(kwh_3);
									dtoReport1.setElectricity_charge(electricity_session_3);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profit_sessin_3);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profit_sessin_3);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_3_price);
									dtoReport1.setTime_zone("PEAK");
									domestic.add(dtoReport2);
									
									
									System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 8");
									
								}else if(_charging_start_type == 2 && _charging_end_type ==  1) { //{2,3,1}
									
									long diffe = end_timestamp.getTime()-start_timestamp.getTime();
									System.out.println(diffe);
									long total_minutes = diffe/(1000*60);
									System.out.println(total_minutes);
									
									long diff_1_tariff = Duration.between(charging_start_time,peak_time_end).toMinutes();
									long diff_2_tariff = Duration.between(off_peak_time_start,off_peak_time_end).toMinutes();
									long diff_3_tariff = Duration.between(day_start_tariff, charging_end_time).toMinutes();
									
									System.out.println(diff_1_tariff);
									System.out.println(diff_2_tariff);
									// Calculation Process
									System.out.println(charge_kwh);
									double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
									double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
									Double kwh_3 = (charge_kwh/total_minutes)*diff_3_tariff;
									
									
									double consume = rs.getDouble("total_consumption_amount");
									System.out.println("total_consume "+consume);
									double _session_1_price = (consume/total_minutes)*diff_1_tariff;
									double _session_2_price = (consume/total_minutes)*diff_2_tariff;
									double _session_3_price = (consume/total_minutes)*diff_2_tariff;
									System.out.println("1 Session Price "+_session_1_price);
									System.out.println("2 Session Price "+_session_2_price);
									System.out.println("3 Session Price "+_session_3_price);
									// price 
									double electricity = kwh_1*peak_price;
									
									
									double estimate_profit_session_1 = _session_1_price-electricity;
									IndustrialChargerCalculationDTO dtoReport = new IndustrialChargerCalculationDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(kwh_1);
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit_session_1);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(_session_1_price);
									dtoReport.setTime_zone("PEAK");
									domestic.add(dtoReport);
									
									double electricityn = kwh_2*off_peak_price;
									
									double estimate_profitn = _session_2_price-electricityn;
									IndustrialChargerCalculationDTO dtoReport1 = new IndustrialChargerCalculationDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_2_tariff);
									dtoReport1.setCharge_kWh(kwh_2);
									dtoReport1.setElectricity_charge(electricityn);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profitn);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_2_price);
									dtoReport1.setTime_zone("OFF PEAK");
									domestic.add(dtoReport1);
									
									double electricity_session_3 = kwh_3*day_price;
									double estimate_profit_sessin_3 = _session_3_price-electricity_session_3;
									IndustrialChargerCalculationDTO dtoReport2 = new IndustrialChargerCalculationDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_3_tariff);
									dtoReport1.setCharge_kWh(kwh_3);
									dtoReport1.setElectricity_charge(electricity_session_3);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profit_sessin_3);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profit_sessin_3);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_3_price);
									dtoReport1.setTime_zone("DAY");
									domestic.add(dtoReport2);
									
									System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 9");
								}
																							
						}
						
				
			
				
		
		}catch(Exception e) {
				e.printStackTrace();
		}
		return domestic;
	}

public List<IndustrialChargerCalculationDTO> getChargerIndustrialDataIII(String station,String start_date,String end_date,int category,int subcategory,String gun_type){
	
	String sql="SELECT * FROM charge_order_information WHERE start_charging_time >= ? AND end_charging_time <= ? AND station_name = ?";
	List<IndustrialChargerCalculationDTO> domestic = new ArrayList<>();
	
	double price_per_unit  = 0.0;
	
	try {
		Connection con = DBConnection.getConnection();
		PreparedStatement ps = con.prepareStatement(sql);			
			ps.setString(1, start_date);
			ps.setString(2, end_date);
			ps.setString(3, station);		
			ResultSet rs = ps.executeQuery();	
			
			
			double charge_kwh = 0.0;
			double day_price = 0.0;
			double peak_price = 0.0;
			double off_peak_price = 0.0;
			double fixed_charge = 0.0;
			double demand_charge = 0.0;
			
	
	String  sqlcategory = "SELECT * FROM other_consumer WHERE type = 4";
						PreparedStatement psn = con.prepareStatement(sqlcategory);
						ResultSet rsn = psn.executeQuery();
						//System.out.println(rsn.get);
						
						while(rsn.next()) {
							
							day_price = rsn.getDouble("day_price");
							peak_price = rsn.getDouble("peak_price");
							off_peak_price = rsn.getDouble("off_peak_price");
							fixed_charge =  rsn.getDouble("fixed_charge");
							demand_charge =  rsn.getDouble("demand_charge");
						}
						
						while(rs.next()) {
							//check Day
							Util util = new Util();
							charge_kwh = rs.getDouble("charge_kwh");
							//charging start time
							Timestamp start_timestamp = rs.getTimestamp("start_charging_time");
							LocalTime charging_start_time = start_timestamp.toLocalDateTime().toLocalTime();
							
							//charging end time 
							Timestamp end_timestamp = rs.getTimestamp("end_charging_time");
							LocalTime charging_end_time = end_timestamp.toLocalDateTime().toLocalTime();
							
							LocalTime day_start_tariff = LocalTime.parse(util.DAY_TIME_START);
							LocalTime day_end_tariff = LocalTime.parse(util.DAY_TIME_END);
							
							LocalTime peak_time_start = LocalTime.parse(util.PEAK_TIME_START);
							LocalTime peak_time_end = LocalTime.parse(util.PEAK_TIME_END);
							
							LocalTime off_peak_time_start = LocalTime.parse(util.OFF_PEAK_TIME_START);
							LocalTime off_peak_time_end = LocalTime.parse(util.OFF_PEAK_TIME_END);
							
							// FIRST CHECK START AND END TIME IN WHICH TARIFF
							
							int _charging_start_type = 0; // 1 = DAY, 2 = PEAK , 3 = OFF PEAK 
							int _charging_end_type = 0; // 1 = DAY, 2 = PEAK , 3 = OFF PEAK 
							
							if(charging_start_time.isAfter(day_start_tariff) && charging_start_time.isBefore(peak_time_start)) { // DAY
								
								_charging_start_type = 1;
																	
							}else if(charging_start_time.isAfter(peak_time_start) && charging_start_time.isBefore(off_peak_time_start)) { // PEAKE
								
								_charging_start_type = 2;
								
							}else if(charging_start_time.isAfter(off_peak_time_start) && charging_start_time.isBefore(day_start_tariff)) { // OFF PEAKE
								
								_charging_start_type = 3;
								
							}
							
							
							if(charging_end_time.isAfter(day_start_tariff) && charging_end_time.isBefore(peak_time_start)) { // DAY
								
								_charging_end_type = 1;
																	
							}else if(charging_end_time.isAfter(peak_time_start) && charging_end_time.isBefore(off_peak_time_start)) { // PEAKE
								
								_charging_end_type = 2;
								
							}else if(charging_end_time.isAfter(off_peak_time_start) && charging_end_time.isBefore(day_start_tariff)) { // OFF PEAKE
								
								_charging_end_type = 3;
								
							}
							
							
							//THE CYCLE PROCESS 
							
							if(_charging_start_type == 1 && _charging_end_type ==  1) { //{1}
																								
								long diff_1_tariff = Duration.between(charging_start_time,charging_end_time).toMinutes();
								double electricity = rs.getDouble("charge_kwh")*day_price;
								double consume = rs.getDouble("total_consumption_amount");
								double estimate_profit = consume-electricity;
								IndustrialChargerCalculationDTO dtoReport = new IndustrialChargerCalculationDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(rs.getDouble("charge_kwh"));
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(consume);
								dtoReport.setTime_zone("DAY");
								domestic.add(dtoReport);
								
								// System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 1");
								
							}else if(_charging_start_type == 2 && _charging_end_type ==  2) { //{2}
								
								long diff_1_tariff = Duration.between(charging_start_time,charging_end_time).toMinutes();
								double electricity = rs.getDouble("charge_kwh")*peak_price;
								double consume = rs.getDouble("total_consumption_amount");
								double estimate_profit = consume-electricity;
								IndustrialChargerCalculationDTO dtoReport = new IndustrialChargerCalculationDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(rs.getDouble("charge_kwh"));
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(consume);
								dtoReport.setTime_zone("PEAK");
								domestic.add(dtoReport);
								
								// System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 2");
								
							}else if(_charging_start_type == 3 && _charging_end_type ==  3) { //{3}
								
								long diff_1_tariff = Duration.between(charging_start_time,charging_end_time).toMinutes();
								double electricity = rs.getDouble("charge_kwh")*off_peak_price;
								double consume = rs.getDouble("total_consumption_amount");
								double estimate_profit = consume-electricity;
								IndustrialChargerCalculationDTO dtoReport = new IndustrialChargerCalculationDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(rs.getDouble("charge_kwh"));
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(consume);
								dtoReport.setTime_zone("OFF PEAK");
								domestic.add(dtoReport);
								
							//	System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 3");
								
							}else if(_charging_start_type == 1 && _charging_end_type ==  2) { //{1,2}
								
								long diffe = end_timestamp.getTime()-start_timestamp.getTime();
								System.out.println(diffe);
								long total_minutes = diffe/(1000*60);
								System.out.println(total_minutes);
								
								long diff_1_tariff = Duration.between(charging_start_time,day_end_tariff).toMinutes();
								long diff_2_tariff = Duration.between(peak_time_start, charging_end_time).toMinutes();
								
								System.out.println(diff_1_tariff);
								System.out.println(diff_2_tariff);
								// Calculation Process
								System.out.println(charge_kwh);
								double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
								double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
								
								System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);
								System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
								
								double consume = rs.getDouble("total_consumption_amount");
								System.out.println("total_consume "+consume);
								double _session_1_price = (consume/total_minutes)*diff_1_tariff;
								double _session_2_price = (consume/total_minutes)*diff_2_tariff;
								
								System.out.println("1 Session Price "+_session_1_price);
								System.out.println("2 Session Price "+_session_2_price);
								
								// price 
								double electricity = kwh_1*day_price;
								
								
								double estimate_profit_session_1 = _session_1_price-electricity;
								IndustrialChargerCalculationDTO dtoReport = new IndustrialChargerCalculationDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(kwh_1);
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit_session_1);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(_session_1_price);
								dtoReport.setTime_zone("DAY");
								domestic.add(dtoReport);
								
								double electricityn = kwh_2*peak_price;
								
								double estimate_profitn = _session_2_price-electricityn;
								IndustrialChargerCalculationDTO dtoReport1 = new IndustrialChargerCalculationDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_2_tariff);
								dtoReport1.setCharge_kWh(kwh_2);
								dtoReport1.setElectricity_charge(electricityn);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profitn);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_2_price);
								dtoReport1.setTime_zone("PEAK");
								domestic.add(dtoReport1);
								
								
								System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 4");
															
							}else if(_charging_start_type == 2 && _charging_end_type ==  3) { //{2,3}
								
								long diffe = end_timestamp.getTime()-start_timestamp.getTime();
								System.out.println(diffe);
								long total_minutes = diffe/(1000*60);
								System.out.println(total_minutes);
								
								long diff_1_tariff = Duration.between(charging_start_time,peak_time_end).toMinutes();
								long diff_2_tariff = Duration.between(off_peak_time_start, charging_end_time).toMinutes();
								
								System.out.println(diff_1_tariff);
								System.out.println(diff_2_tariff);
								// Calculation Process
								System.out.println(charge_kwh);
								double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
								double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
								
								System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);
								System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
								
								double consume = rs.getDouble("total_consumption_amount");
								System.out.println("total_consume "+consume);
								double _session_1_price = (consume/total_minutes)*diff_1_tariff;
								double _session_2_price = (consume/total_minutes)*diff_2_tariff;
								
								System.out.println("1 Session Price "+_session_1_price);
								System.out.println("2 Session Price "+_session_2_price);
								
								// price 
								double electricity = kwh_1*day_price;
								
								
								double estimate_profit_session_1 = _session_1_price-electricity;
								IndustrialChargerCalculationDTO dtoReport = new IndustrialChargerCalculationDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(kwh_1);
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit_session_1);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(_session_1_price);
								dtoReport.setTime_zone("DAY");
								domestic.add(dtoReport);
								
								double electricityn = kwh_2*peak_price;
								
								double estimate_profitn = _session_2_price-electricityn;
								IndustrialChargerCalculationDTO dtoReport1 = new IndustrialChargerCalculationDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_2_tariff);
								dtoReport1.setCharge_kWh(kwh_2);
								dtoReport1.setElectricity_charge(electricityn);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profitn);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_2_price);
								dtoReport1.setTime_zone("PEAK");
								domestic.add(dtoReport1);
								
								System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 5");
								
							}else if(_charging_start_type == 3 && _charging_end_type ==  1) { //{3,1}
								
								long diffe = end_timestamp.getTime()-start_timestamp.getTime();
								System.out.println(diffe);
								long total_minutes = diffe/(1000*60);
								System.out.println(total_minutes);
								
								long diff_1_tariff = Duration.between(charging_start_time,off_peak_time_end).toMinutes();
								long diff_2_tariff = Duration.between(day_start_tariff, charging_end_time).toMinutes();
								
								System.out.println(diff_1_tariff);
								System.out.println(diff_2_tariff);
								// Calculation Process
								System.out.println(charge_kwh);
								double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
								double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
								
								System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);
								System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
								
								double consume = rs.getDouble("total_consumption_amount");
								System.out.println("total_consume "+consume);
								double _session_1_price = (consume/total_minutes)*diff_1_tariff;
								double _session_2_price = (consume/total_minutes)*diff_2_tariff;
								
								System.out.println("1 Session Price "+_session_1_price);
								System.out.println("2 Session Price "+_session_2_price);
								
								// price 
								double electricity = kwh_1*day_price;
								
								
								double estimate_profit_session_1 = _session_1_price-electricity;
								IndustrialChargerCalculationDTO dtoReport = new IndustrialChargerCalculationDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(kwh_1);
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit_session_1);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(_session_1_price);
								dtoReport.setTime_zone("DAY");
								domestic.add(dtoReport);
								
								double electricityn = kwh_2*peak_price;
								
								double estimate_profitn = _session_2_price-electricityn;
								IndustrialChargerCalculationDTO dtoReport1 = new IndustrialChargerCalculationDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_2_tariff);
								dtoReport1.setCharge_kWh(kwh_2);
								dtoReport1.setElectricity_charge(electricityn);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profitn);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_2_price);
								dtoReport1.setTime_zone("PEAK");
								domestic.add(dtoReport1);
								
								
								
								System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 6");
								
							}else if(_charging_start_type == 1 && _charging_end_type ==  3) { //{1,2,3}
								
								long diffe = end_timestamp.getTime()-start_timestamp.getTime();
								System.out.println(diffe);
								long total_minutes = diffe/(1000*60);
								System.out.println(total_minutes);
								
								long diff_1_tariff = Duration.between(charging_start_time,day_end_tariff).toMinutes();
								long diff_2_tariff = Duration.between(peak_time_start,peak_time_end).toMinutes();
								long diff_3_tariff = Duration.between(off_peak_time_start, charging_end_time).toMinutes();
								
								System.out.println(diff_1_tariff);
								System.out.println(diff_2_tariff);
								// Calculation Process
								System.out.println(charge_kwh);
								double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
								double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
								Double kwh_3 = (charge_kwh/total_minutes)*diff_3_tariff;
								
								System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);									
								System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
								System.out.println("Off peak "+off_peak_price+"-"+kwh_3*off_peak_price);
								
								double consume = rs.getDouble("total_consumption_amount");
								System.out.println("total_consume "+consume);
								double _session_1_price = (consume/total_minutes)*diff_1_tariff;
								double _session_2_price = (consume/total_minutes)*diff_2_tariff;
								double _session_3_price = (consume/total_minutes)*diff_2_tariff;
								System.out.println("1 Session Price "+_session_1_price);
								System.out.println("2 Session Price "+_session_2_price);
								System.out.println("3 Session Price "+_session_3_price);
								// price 
								double electricity = kwh_1*day_price;
								
								
								double estimate_profit_session_1 = _session_1_price-electricity;
								IndustrialChargerCalculationDTO dtoReport = new IndustrialChargerCalculationDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(kwh_1);
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit_session_1);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(_session_1_price);
								dtoReport.setTime_zone("DAY");
								domestic.add(dtoReport);
								
								double electricityn = kwh_2*peak_price;
								
								double estimate_profitn = _session_2_price-electricityn;
								IndustrialChargerCalculationDTO dtoReport1 = new IndustrialChargerCalculationDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_2_tariff);
								dtoReport1.setCharge_kWh(kwh_2);
								dtoReport1.setElectricity_charge(electricityn);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profitn);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_2_price);
								dtoReport1.setTime_zone("PEAK");
								domestic.add(dtoReport1);
								
								double electricity_session_3 = kwh_3*off_peak_price;
								double estimate_profit_sessin_3 = _session_3_price-electricity_session_3;
								IndustrialChargerCalculationDTO dtoReport2 = new IndustrialChargerCalculationDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_3_tariff);
								dtoReport1.setCharge_kWh(kwh_3);
								dtoReport1.setElectricity_charge(electricity_session_3);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profit_sessin_3);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profit_sessin_3);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_3_price);
								dtoReport1.setTime_zone("OFF PEAK");
								domestic.add(dtoReport2);
								
								
								System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 7");
								
							}else if(_charging_start_type == 3 && _charging_end_type ==  2) { //{3,1,2}
								
								long diffe = end_timestamp.getTime()-start_timestamp.getTime();
								System.out.println(diffe);
								long total_minutes = diffe/(1000*60);
								System.out.println(total_minutes);
								
								long diff_1_tariff = Duration.between(charging_start_time,off_peak_time_end).toMinutes();
								long diff_2_tariff = Duration.between(day_start_tariff,day_start_tariff).toMinutes();
								long diff_3_tariff = Duration.between(peak_time_start, charging_end_time).toMinutes();
								
								System.out.println(diff_1_tariff);
								System.out.println(diff_2_tariff);
								// Calculation Process
								System.out.println(charge_kwh);
								double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
								double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
								Double kwh_3 = (charge_kwh/total_minutes)*diff_3_tariff;
								
								
								double consume = rs.getDouble("total_consumption_amount");
								System.out.println("total_consume "+consume);
								double _session_1_price = (consume/total_minutes)*diff_1_tariff;
								double _session_2_price = (consume/total_minutes)*diff_2_tariff;
								double _session_3_price = (consume/total_minutes)*diff_2_tariff;
								System.out.println("1 Session Price "+_session_1_price);
								System.out.println("2 Session Price "+_session_2_price);
								System.out.println("3 Session Price "+_session_3_price);
								// price 
								double electricity = kwh_1*off_peak_price;
								
								
								double estimate_profit_session_1 = _session_1_price-electricity;
								IndustrialChargerCalculationDTO dtoReport = new IndustrialChargerCalculationDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(kwh_1);
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit_session_1);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(_session_1_price);
								dtoReport.setTime_zone("OFF PEAK");
								domestic.add(dtoReport);
								
								double electricityn = kwh_2*day_price;
								
								double estimate_profitn = _session_2_price-electricityn;
								IndustrialChargerCalculationDTO dtoReport1 = new IndustrialChargerCalculationDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_2_tariff);
								dtoReport1.setCharge_kWh(kwh_2);
								dtoReport1.setElectricity_charge(electricityn);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profitn);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_2_price);
								dtoReport1.setTime_zone("DAY");
								domestic.add(dtoReport1);
								
								double electricity_session_3 = kwh_3*peak_price;
								double estimate_profit_sessin_3 = _session_3_price-electricity_session_3;
								IndustrialChargerCalculationDTO dtoReport2 = new IndustrialChargerCalculationDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_3_tariff);
								dtoReport1.setCharge_kWh(kwh_3);
								dtoReport1.setElectricity_charge(electricity_session_3);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profit_sessin_3);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profit_sessin_3);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_3_price);
								dtoReport1.setTime_zone("PEAK");
								domestic.add(dtoReport2);
								
								
								System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 8");
								
							}else if(_charging_start_type == 2 && _charging_end_type ==  1) { //{2,3,1}
								
								long diffe = end_timestamp.getTime()-start_timestamp.getTime();
								System.out.println(diffe);
								long total_minutes = diffe/(1000*60);
								System.out.println(total_minutes);
								
								long diff_1_tariff = Duration.between(charging_start_time,peak_time_end).toMinutes();
								long diff_2_tariff = Duration.between(off_peak_time_start,off_peak_time_end).toMinutes();
								long diff_3_tariff = Duration.between(day_start_tariff, charging_end_time).toMinutes();
								
								System.out.println(diff_1_tariff);
								System.out.println(diff_2_tariff);
								// Calculation Process
								System.out.println(charge_kwh);
								double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
								double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
								Double kwh_3 = (charge_kwh/total_minutes)*diff_3_tariff;
								
								
								double consume = rs.getDouble("total_consumption_amount");
								System.out.println("total_consume "+consume);
								double _session_1_price = (consume/total_minutes)*diff_1_tariff;
								double _session_2_price = (consume/total_minutes)*diff_2_tariff;
								double _session_3_price = (consume/total_minutes)*diff_2_tariff;
								System.out.println("1 Session Price "+_session_1_price);
								System.out.println("2 Session Price "+_session_2_price);
								System.out.println("3 Session Price "+_session_3_price);
								// price 
								double electricity = kwh_1*peak_price;
								
								
								double estimate_profit_session_1 = _session_1_price-electricity;
								IndustrialChargerCalculationDTO dtoReport = new IndustrialChargerCalculationDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(kwh_1);
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit_session_1);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(_session_1_price);
								dtoReport.setTime_zone("PEAK");
								domestic.add(dtoReport);
								
								double electricityn = kwh_2*off_peak_price;
								
								double estimate_profitn = _session_2_price-electricityn;
								IndustrialChargerCalculationDTO dtoReport1 = new IndustrialChargerCalculationDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_2_tariff);
								dtoReport1.setCharge_kWh(kwh_2);
								dtoReport1.setElectricity_charge(electricityn);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profitn);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_2_price);
								dtoReport1.setTime_zone("OFF PEAK");
								domestic.add(dtoReport1);
								
								double electricity_session_3 = kwh_3*day_price;
								double estimate_profit_sessin_3 = _session_3_price-electricity_session_3;
								IndustrialChargerCalculationDTO dtoReport2 = new IndustrialChargerCalculationDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_3_tariff);
								dtoReport1.setCharge_kWh(kwh_3);
								dtoReport1.setElectricity_charge(electricity_session_3);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profit_sessin_3);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profit_sessin_3);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_3_price);
								dtoReport1.setTime_zone("DAY");
								domestic.add(dtoReport2);
								
								System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 9");
							}
																						
					}
					
			
		
			
	
	}catch(Exception e) {
			e.printStackTrace();
	}
	return domestic;
}

public List<HotelRateDTO> StartBillCalculationProcessHotel(String station,String start_date,String end_date,int category,int subcategory,String gun_type){
		
		String sql="SELECT * FROM charge_order_information WHERE start_charging_time >= ? AND end_charging_time <= ? AND station_name = ?";
		List<HotelRateDTO> domestic = new ArrayList<>();
		
		double price_per_unit  = 0.0;
		
		try {
			Connection con = DBConnection.getConnection();
			PreparedStatement ps = con.prepareStatement(sql);			
				ps.setString(1, start_date);
				ps.setString(2, end_date);
				ps.setString(3, station);		
				ResultSet rs = ps.executeQuery();	
				
				String total_kwh_query = "SELECT SUM(charge_kwh) as total FROM charge_order_information WHERE station_name = ? ";
				PreparedStatement ps_tkq = con.prepareStatement(total_kwh_query);
				ps_tkq.setString(1, station);
				ResultSet resultSet = ps_tkq.executeQuery();
				
				double charge_kwh = 0.0;
				double day_price = 0.0;
				double peak_price = 0.0;
				double off_peak_price = 0.0;
				double fixed_charge = 0.0;
				double demand_charge = 0.0;
				
				if(resultSet.next()) {
					
					
			
						if(resultSet.getDouble("total") <= 300) {
							
							String  sqlcategory = "SELECT * FROM other_consumer WHERE type = 5";
							PreparedStatement psn = con.prepareStatement(sqlcategory);
							ResultSet rsn = psn.executeQuery();
							//System.out.println(rsn.get);
							
		while(rsn.next()) {
								
								day_price = rsn.getDouble("day_price");
								peak_price = rsn.getDouble("peak_price");
								off_peak_price = rsn.getDouble("off_peak_price");
								fixed_charge =  rsn.getDouble("fixed_charge");
								demand_charge =  rsn.getDouble("demand_charge");
							}
							
							while(rs.next()) {
								//check Day
								Util util = new Util();
								charge_kwh = rs.getDouble("charge_kwh");
								//charging start time
								Timestamp start_timestamp = rs.getTimestamp("start_charging_time");
								LocalTime charging_start_time = start_timestamp.toLocalDateTime().toLocalTime();
								
								//charging end time 
								Timestamp end_timestamp = rs.getTimestamp("end_charging_time");
								LocalTime charging_end_time = end_timestamp.toLocalDateTime().toLocalTime();
								
								LocalTime day_start_tariff = LocalTime.parse(util.DAY_TIME_START);
								LocalTime day_end_tariff = LocalTime.parse(util.DAY_TIME_END);
								
								LocalTime peak_time_start = LocalTime.parse(util.PEAK_TIME_START);
								LocalTime peak_time_end = LocalTime.parse(util.PEAK_TIME_END);
								
								LocalTime off_peak_time_start = LocalTime.parse(util.OFF_PEAK_TIME_START);
								LocalTime off_peak_time_end = LocalTime.parse(util.OFF_PEAK_TIME_END);
								
								// FIRST CHECK START AND END TIME IN WHICH TARIFF
								
								int _charging_start_type = 0; // 1 = DAY, 2 = PEAK , 3 = OFF PEAK 
								int _charging_end_type = 0; // 1 = DAY, 2 = PEAK , 3 = OFF PEAK 
								
								if(charging_start_time.isAfter(day_start_tariff) && charging_start_time.isBefore(peak_time_start)) { // DAY
									
									_charging_start_type = 1;
																		
								}else if(charging_start_time.isAfter(peak_time_start) && charging_start_time.isBefore(off_peak_time_start)) { // PEAKE
									
									_charging_start_type = 2;
									
								}else if(charging_start_time.isAfter(off_peak_time_start) && charging_start_time.isBefore(day_start_tariff)) { // OFF PEAKE
									
									_charging_start_type = 3;
									
								}
								
								
								if(charging_end_time.isAfter(day_start_tariff) && charging_end_time.isBefore(peak_time_start)) { // DAY
									
									_charging_end_type = 1;
																		
								}else if(charging_end_time.isAfter(peak_time_start) && charging_end_time.isBefore(off_peak_time_start)) { // PEAKE
									
									_charging_end_type = 2;
									
								}else if(charging_end_time.isAfter(off_peak_time_start) && charging_end_time.isBefore(day_start_tariff)) { // OFF PEAKE
									
									_charging_end_type = 3;
									
								}
								
								
								//THE CYCLE PROCESS 
								
								if(_charging_start_type == 1 && _charging_end_type ==  1) { //{1}
																									
									long diff_1_tariff = Duration.between(charging_start_time,charging_end_time).toMinutes();
									double electricity = rs.getDouble("charge_kwh")*day_price;
									double consume = rs.getDouble("total_consumption_amount");
									double estimate_profit = consume-electricity;
									HotelRateDTO dtoReport = new HotelRateDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(rs.getDouble("charge_kwh"));
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(consume);
									dtoReport.setTime_zone("DAY");
									domestic.add(dtoReport);
									
									// System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 1");
									
								}else if(_charging_start_type == 2 && _charging_end_type ==  2) { //{2}
									
									long diff_1_tariff = Duration.between(charging_start_time,charging_end_time).toMinutes();
									double electricity = rs.getDouble("charge_kwh")*peak_price;
									double consume = rs.getDouble("total_consumption_amount");
									double estimate_profit = consume-electricity;
									HotelRateDTO dtoReport = new HotelRateDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(rs.getDouble("charge_kwh"));
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(consume);
									dtoReport.setTime_zone("PEAK");
									domestic.add(dtoReport);
									
									// System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 2");
									
								}else if(_charging_start_type == 3 && _charging_end_type ==  3) { //{3}
									
									long diff_1_tariff = Duration.between(charging_start_time,charging_end_time).toMinutes();
									double electricity = rs.getDouble("charge_kwh")*off_peak_price;
									double consume = rs.getDouble("total_consumption_amount");
									double estimate_profit = consume-electricity;
									HotelRateDTO dtoReport = new HotelRateDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(rs.getDouble("charge_kwh"));
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(consume);
									dtoReport.setTime_zone("OFF PEAK");
									domestic.add(dtoReport);
									
								//	System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 3");
									
								}else if(_charging_start_type == 1 && _charging_end_type ==  2) { //{1,2}
									
									long diffe = end_timestamp.getTime()-start_timestamp.getTime();
									System.out.println(diffe);
									long total_minutes = diffe/(1000*60);
									System.out.println(total_minutes);
									
									long diff_1_tariff = Duration.between(charging_start_time,day_end_tariff).toMinutes();
									long diff_2_tariff = Duration.between(peak_time_start, charging_end_time).toMinutes();
									
									System.out.println(diff_1_tariff);
									System.out.println(diff_2_tariff);
									// Calculation Process
									System.out.println(charge_kwh);
									double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
									double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
									
									System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);
									System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
									
									double consume = rs.getDouble("total_consumption_amount");
									System.out.println("total_consume "+consume);
									double _session_1_price = (consume/total_minutes)*diff_1_tariff;
									double _session_2_price = (consume/total_minutes)*diff_2_tariff;
									
									System.out.println("1 Session Price "+_session_1_price);
									System.out.println("2 Session Price "+_session_2_price);
									
									// price 
									double electricity = kwh_1*day_price;
									
									
									double estimate_profit_session_1 = _session_1_price-electricity;
									HotelRateDTO dtoReport = new HotelRateDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(kwh_1);
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit_session_1);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(_session_1_price);
									dtoReport.setTime_zone("DAY");
									domestic.add(dtoReport);
									
									double electricityn = kwh_2*peak_price;
									
									double estimate_profitn = _session_2_price-electricityn;
									HotelRateDTO dtoReport1 = new HotelRateDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_2_tariff);
									dtoReport1.setCharge_kWh(kwh_2);
									dtoReport1.setElectricity_charge(electricityn);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profitn);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_2_price);
									dtoReport1.setTime_zone("PEAK");
									domestic.add(dtoReport1);
									
									
									System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 4");
																
								}else if(_charging_start_type == 2 && _charging_end_type ==  3) { //{2,3}
									
									long diffe = end_timestamp.getTime()-start_timestamp.getTime();
									System.out.println(diffe);
									long total_minutes = diffe/(1000*60);
									System.out.println(total_minutes);
									
									long diff_1_tariff = Duration.between(charging_start_time,peak_time_end).toMinutes();
									long diff_2_tariff = Duration.between(off_peak_time_start, charging_end_time).toMinutes();
									
									System.out.println(diff_1_tariff);
									System.out.println(diff_2_tariff);
									// Calculation Process
									System.out.println(charge_kwh);
									double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
									double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
									
									System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);
									System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
									
									double consume = rs.getDouble("total_consumption_amount");
									System.out.println("total_consume "+consume);
									double _session_1_price = (consume/total_minutes)*diff_1_tariff;
									double _session_2_price = (consume/total_minutes)*diff_2_tariff;
									
									System.out.println("1 Session Price "+_session_1_price);
									System.out.println("2 Session Price "+_session_2_price);
									
									// price 
									double electricity = kwh_1*day_price;
									
									
									double estimate_profit_session_1 = _session_1_price-electricity;
									HotelRateDTO dtoReport = new HotelRateDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(kwh_1);
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit_session_1);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(_session_1_price);
									dtoReport.setTime_zone("DAY");
									domestic.add(dtoReport);
									
									double electricityn = kwh_2*peak_price;
									
									double estimate_profitn = _session_2_price-electricityn;
									HotelRateDTO dtoReport1 = new HotelRateDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_2_tariff);
									dtoReport1.setCharge_kWh(kwh_2);
									dtoReport1.setElectricity_charge(electricityn);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profitn);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_2_price);
									dtoReport1.setTime_zone("PEAK");
									domestic.add(dtoReport1);
									
									System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 5");
									
								}else if(_charging_start_type == 3 && _charging_end_type ==  1) { //{3,1}
									
									long diffe = end_timestamp.getTime()-start_timestamp.getTime();
									System.out.println(diffe);
									long total_minutes = diffe/(1000*60);
									System.out.println(total_minutes);
									
									long diff_1_tariff = Duration.between(charging_start_time,off_peak_time_end).toMinutes();
									long diff_2_tariff = Duration.between(day_start_tariff, charging_end_time).toMinutes();
									
									System.out.println(diff_1_tariff);
									System.out.println(diff_2_tariff);
									// Calculation Process
									System.out.println(charge_kwh);
									double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
									double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
									
									System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);
									System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
									
									double consume = rs.getDouble("total_consumption_amount");
									System.out.println("total_consume "+consume);
									double _session_1_price = (consume/total_minutes)*diff_1_tariff;
									double _session_2_price = (consume/total_minutes)*diff_2_tariff;
									
									System.out.println("1 Session Price "+_session_1_price);
									System.out.println("2 Session Price "+_session_2_price);
									
									// price 
									double electricity = kwh_1*day_price;
									
									
									double estimate_profit_session_1 = _session_1_price-electricity;
									HotelRateDTO dtoReport = new HotelRateDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(kwh_1);
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit_session_1);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(_session_1_price);
									dtoReport.setTime_zone("DAY");
									domestic.add(dtoReport);
									
									double electricityn = kwh_2*peak_price;
									
									double estimate_profitn = _session_2_price-electricityn;
									HotelRateDTO dtoReport1 = new HotelRateDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_2_tariff);
									dtoReport1.setCharge_kWh(kwh_2);
									dtoReport1.setElectricity_charge(electricityn);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profitn);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_2_price);
									dtoReport1.setTime_zone("PEAK");
									domestic.add(dtoReport1);
									
									
									
									System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 6");
									
								}else if(_charging_start_type == 1 && _charging_end_type ==  3) { //{1,2,3}
									
									long diffe = end_timestamp.getTime()-start_timestamp.getTime();
									System.out.println(diffe);
									long total_minutes = diffe/(1000*60);
									System.out.println(total_minutes);
									
									long diff_1_tariff = Duration.between(charging_start_time,day_end_tariff).toMinutes();
									long diff_2_tariff = Duration.between(peak_time_start,peak_time_end).toMinutes();
									long diff_3_tariff = Duration.between(off_peak_time_start, charging_end_time).toMinutes();
									
									System.out.println(diff_1_tariff);
									System.out.println(diff_2_tariff);
									// Calculation Process
									System.out.println(charge_kwh);
									double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
									double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
									Double kwh_3 = (charge_kwh/total_minutes)*diff_3_tariff;
									
									System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);									
									System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
									System.out.println("Off peak "+off_peak_price+"-"+kwh_3*off_peak_price);
									
									double consume = rs.getDouble("total_consumption_amount");
									System.out.println("total_consume "+consume);
									double _session_1_price = (consume/total_minutes)*diff_1_tariff;
									double _session_2_price = (consume/total_minutes)*diff_2_tariff;
									double _session_3_price = (consume/total_minutes)*diff_2_tariff;
									System.out.println("1 Session Price "+_session_1_price);
									System.out.println("2 Session Price "+_session_2_price);
									System.out.println("3 Session Price "+_session_3_price);
									// price 
									double electricity = kwh_1*day_price;
									
									
									double estimate_profit_session_1 = _session_1_price-electricity;
									HotelRateDTO dtoReport = new HotelRateDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(kwh_1);
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit_session_1);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(_session_1_price);
									dtoReport.setTime_zone("DAY");
									domestic.add(dtoReport);
									
									double electricityn = kwh_2*peak_price;
									
									double estimate_profitn = _session_2_price-electricityn;
									HotelRateDTO dtoReport1 = new HotelRateDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_2_tariff);
									dtoReport1.setCharge_kWh(kwh_2);
									dtoReport1.setElectricity_charge(electricityn);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profitn);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_2_price);
									dtoReport1.setTime_zone("PEAK");
									domestic.add(dtoReport1);
									
									double electricity_session_3 = kwh_3*off_peak_price;
									double estimate_profit_sessin_3 = _session_3_price-electricity_session_3;
									HotelRateDTO dtoReport2 = new HotelRateDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_3_tariff);
									dtoReport1.setCharge_kWh(kwh_3);
									dtoReport1.setElectricity_charge(electricity_session_3);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profit_sessin_3);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profit_sessin_3);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_3_price);
									dtoReport1.setTime_zone("OFF PEAK");
									domestic.add(dtoReport2);
									
									
									System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 7");
									
								}else if(_charging_start_type == 3 && _charging_end_type ==  2) { //{3,1,2}
									
									long diffe = end_timestamp.getTime()-start_timestamp.getTime();
									System.out.println(diffe);
									long total_minutes = diffe/(1000*60);
									System.out.println(total_minutes);
									
									long diff_1_tariff = Duration.between(charging_start_time,off_peak_time_end).toMinutes();
									long diff_2_tariff = Duration.between(day_start_tariff,day_start_tariff).toMinutes();
									long diff_3_tariff = Duration.between(peak_time_start, charging_end_time).toMinutes();
									
									System.out.println(diff_1_tariff);
									System.out.println(diff_2_tariff);
									// Calculation Process
									System.out.println(charge_kwh);
									double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
									double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
									Double kwh_3 = (charge_kwh/total_minutes)*diff_3_tariff;
									
									
									double consume = rs.getDouble("total_consumption_amount");
									System.out.println("total_consume "+consume);
									double _session_1_price = (consume/total_minutes)*diff_1_tariff;
									double _session_2_price = (consume/total_minutes)*diff_2_tariff;
									double _session_3_price = (consume/total_minutes)*diff_2_tariff;
									System.out.println("1 Session Price "+_session_1_price);
									System.out.println("2 Session Price "+_session_2_price);
									System.out.println("3 Session Price "+_session_3_price);
									// price 
									double electricity = kwh_1*off_peak_price;
									
									
									double estimate_profit_session_1 = _session_1_price-electricity;
									HotelRateDTO dtoReport = new HotelRateDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(kwh_1);
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit_session_1);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(_session_1_price);
									dtoReport.setTime_zone("OFF PEAK");
									domestic.add(dtoReport);
									
									double electricityn = kwh_2*day_price;
									
									double estimate_profitn = _session_2_price-electricityn;
									HotelRateDTO dtoReport1 = new HotelRateDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_2_tariff);
									dtoReport1.setCharge_kWh(kwh_2);
									dtoReport1.setElectricity_charge(electricityn);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profitn);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_2_price);
									dtoReport1.setTime_zone("DAY");
									domestic.add(dtoReport1);
									
									double electricity_session_3 = kwh_3*peak_price;
									double estimate_profit_sessin_3 = _session_3_price-electricity_session_3;
									HotelRateDTO dtoReport2 = new HotelRateDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_3_tariff);
									dtoReport1.setCharge_kWh(kwh_3);
									dtoReport1.setElectricity_charge(electricity_session_3);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profit_sessin_3);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profit_sessin_3);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_3_price);
									dtoReport1.setTime_zone("PEAK");
									domestic.add(dtoReport2);
									
									
									System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 8");
									
								}else if(_charging_start_type == 2 && _charging_end_type ==  1) { //{2,3,1}
									
									long diffe = end_timestamp.getTime()-start_timestamp.getTime();
									System.out.println(diffe);
									long total_minutes = diffe/(1000*60);
									System.out.println(total_minutes);
									
									long diff_1_tariff = Duration.between(charging_start_time,peak_time_end).toMinutes();
									long diff_2_tariff = Duration.between(off_peak_time_start,off_peak_time_end).toMinutes();
									long diff_3_tariff = Duration.between(day_start_tariff, charging_end_time).toMinutes();
									
									System.out.println(diff_1_tariff);
									System.out.println(diff_2_tariff);
									// Calculation Process
									System.out.println(charge_kwh);
									double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
									double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
									Double kwh_3 = (charge_kwh/total_minutes)*diff_3_tariff;
									
									
									double consume = rs.getDouble("total_consumption_amount");
									System.out.println("total_consume "+consume);
									double _session_1_price = (consume/total_minutes)*diff_1_tariff;
									double _session_2_price = (consume/total_minutes)*diff_2_tariff;
									double _session_3_price = (consume/total_minutes)*diff_2_tariff;
									System.out.println("1 Session Price "+_session_1_price);
									System.out.println("2 Session Price "+_session_2_price);
									System.out.println("3 Session Price "+_session_3_price);
									// price 
									double electricity = kwh_1*peak_price;
									
									
									double estimate_profit_session_1 = _session_1_price-electricity;
									HotelRateDTO dtoReport = new HotelRateDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(kwh_1);
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit_session_1);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(_session_1_price);
									dtoReport.setTime_zone("PEAK");
									domestic.add(dtoReport);
									
									double electricityn = kwh_2*off_peak_price;
									
									double estimate_profitn = _session_2_price-electricityn;
									HotelRateDTO dtoReport1 = new HotelRateDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_2_tariff);
									dtoReport1.setCharge_kWh(kwh_2);
									dtoReport1.setElectricity_charge(electricityn);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profitn);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_2_price);
									dtoReport1.setTime_zone("OFF PEAK");
									domestic.add(dtoReport1);
									
									double electricity_session_3 = kwh_3*day_price;
									double estimate_profit_sessin_3 = _session_3_price-electricity_session_3;
									HotelRateDTO dtoReport2 = new HotelRateDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_3_tariff);
									dtoReport1.setCharge_kWh(kwh_3);
									dtoReport1.setElectricity_charge(electricity_session_3);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profit_sessin_3);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profit_sessin_3);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_3_price);
									dtoReport1.setTime_zone("DAY");
									domestic.add(dtoReport2);
									
									System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 9");
								}
																							
						}
							
							
							
						}else if(resultSet.getDouble("total") > 300) {
							
							String  sqlcategory = "SELECT * FROM other_consumer WHERE type = 2";
							PreparedStatement psn = con.prepareStatement(sqlcategory);
							ResultSet rsn = psn.executeQuery();
							//System.out.println(rsn.get);
							
							while(rsn.next()) {
								
								day_price = rsn.getDouble("day_price");
								peak_price = rsn.getDouble("peak_price");
								off_peak_price = rsn.getDouble("off_peak_price");
								fixed_charge =  rsn.getDouble("fixed_charge");
								demand_charge =  rsn.getDouble("demand_charge");
							}
							
							while(rs.next()) {
								//check Day
								Util util = new Util();
								charge_kwh = rs.getDouble("charge_kwh");
								//charging start time
								Timestamp start_timestamp = rs.getTimestamp("start_charging_time");
								LocalTime charging_start_time = start_timestamp.toLocalDateTime().toLocalTime();
								
								//charging end time 
								Timestamp end_timestamp = rs.getTimestamp("end_charging_time");
								LocalTime charging_end_time = end_timestamp.toLocalDateTime().toLocalTime();
								
								LocalTime day_start_tariff = LocalTime.parse(util.DAY_TIME_START);
								LocalTime day_end_tariff = LocalTime.parse(util.DAY_TIME_END);
								
								LocalTime peak_time_start = LocalTime.parse(util.PEAK_TIME_START);
								LocalTime peak_time_end = LocalTime.parse(util.PEAK_TIME_END);
								
								LocalTime off_peak_time_start = LocalTime.parse(util.OFF_PEAK_TIME_START);
								LocalTime off_peak_time_end = LocalTime.parse(util.OFF_PEAK_TIME_END);
								
								// FIRST CHECK START AND END TIME IN WHICH TARIFF
								
								int _charging_start_type = 0; // 1 = DAY, 2 = PEAK , 3 = OFF PEAK 
								int _charging_end_type = 0; // 1 = DAY, 2 = PEAK , 3 = OFF PEAK 
								
								if(charging_start_time.isAfter(day_start_tariff) && charging_start_time.isBefore(peak_time_start)) { // DAY
									
									_charging_start_type = 1;
																		
								}else if(charging_start_time.isAfter(peak_time_start) && charging_start_time.isBefore(off_peak_time_start)) { // PEAKE
									
									_charging_start_type = 2;
									
								}else if(charging_start_time.isAfter(off_peak_time_start) && charging_start_time.isBefore(day_start_tariff)) { // OFF PEAKE
									
									_charging_start_type = 3;
									
								}
								
								
								if(charging_end_time.isAfter(day_start_tariff) && charging_end_time.isBefore(peak_time_start)) { // DAY
									
									_charging_end_type = 1;
																		
								}else if(charging_end_time.isAfter(peak_time_start) && charging_end_time.isBefore(off_peak_time_start)) { // PEAKE
									
									_charging_end_type = 2;
									
								}else if(charging_end_time.isAfter(off_peak_time_start) && charging_end_time.isBefore(day_start_tariff)) { // OFF PEAKE
									
									_charging_end_type = 3;
									
								}
								
								
								//THE CYCLE PROCESS 
								
								if(_charging_start_type == 1 && _charging_end_type ==  1) { //{1}
																									
									long diff_1_tariff = Duration.between(charging_start_time,charging_end_time).toMinutes();
									double electricity = rs.getDouble("charge_kwh")*day_price;
									double consume = rs.getDouble("total_consumption_amount");
									double estimate_profit = consume-electricity;
									HotelRateDTO dtoReport = new HotelRateDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(rs.getDouble("charge_kwh"));
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(consume);
									dtoReport.setTime_zone("DAY");
									domestic.add(dtoReport);
									
									// System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 1");
									
								}else if(_charging_start_type == 2 && _charging_end_type ==  2) { //{2}
									
									long diff_1_tariff = Duration.between(charging_start_time,charging_end_time).toMinutes();
									double electricity = rs.getDouble("charge_kwh")*peak_price;
									double consume = rs.getDouble("total_consumption_amount");
									double estimate_profit = consume-electricity;
									HotelRateDTO dtoReport = new HotelRateDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(rs.getDouble("charge_kwh"));
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(consume);
									dtoReport.setTime_zone("PEAK");
									domestic.add(dtoReport);
									
									// System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 2");
									
								}else if(_charging_start_type == 3 && _charging_end_type ==  3) { //{3}
									
									long diff_1_tariff = Duration.between(charging_start_time,charging_end_time).toMinutes();
									double electricity = rs.getDouble("charge_kwh")*off_peak_price;
									double consume = rs.getDouble("total_consumption_amount");
									double estimate_profit = consume-electricity;
									HotelRateDTO dtoReport = new HotelRateDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(rs.getDouble("charge_kwh"));
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(consume);
									dtoReport.setTime_zone("OFF PEAK");
									domestic.add(dtoReport);
									
								//	System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 3");
									
								}else if(_charging_start_type == 1 && _charging_end_type ==  2) { //{1,2}
									
									long diffe = end_timestamp.getTime()-start_timestamp.getTime();
									System.out.println(diffe);
									long total_minutes = diffe/(1000*60);
									System.out.println(total_minutes);
									
									long diff_1_tariff = Duration.between(charging_start_time,day_end_tariff).toMinutes();
									long diff_2_tariff = Duration.between(peak_time_start, charging_end_time).toMinutes();
									
									System.out.println(diff_1_tariff);
									System.out.println(diff_2_tariff);
									// Calculation Process
									System.out.println(charge_kwh);
									double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
									double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
									
									System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);
									System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
									
									double consume = rs.getDouble("total_consumption_amount");
									System.out.println("total_consume "+consume);
									double _session_1_price = (consume/total_minutes)*diff_1_tariff;
									double _session_2_price = (consume/total_minutes)*diff_2_tariff;
									
									System.out.println("1 Session Price "+_session_1_price);
									System.out.println("2 Session Price "+_session_2_price);
									
									// price 
									double electricity = kwh_1*day_price;
									
									
									double estimate_profit_session_1 = _session_1_price-electricity;
									HotelRateDTO dtoReport = new HotelRateDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(kwh_1);
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit_session_1);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(_session_1_price);
									dtoReport.setTime_zone("DAY");
									domestic.add(dtoReport);
									
									double electricityn = kwh_2*peak_price;
									
									double estimate_profitn = _session_2_price-electricityn;
									HotelRateDTO dtoReport1 = new HotelRateDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_2_tariff);
									dtoReport1.setCharge_kWh(kwh_2);
									dtoReport1.setElectricity_charge(electricityn);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profitn);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_2_price);
									dtoReport1.setTime_zone("PEAK");
									domestic.add(dtoReport1);
									
									
									System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 4");
																
								}else if(_charging_start_type == 2 && _charging_end_type ==  3) { //{2,3}
									
									long diffe = end_timestamp.getTime()-start_timestamp.getTime();
									System.out.println(diffe);
									long total_minutes = diffe/(1000*60);
									System.out.println(total_minutes);
									
									long diff_1_tariff = Duration.between(charging_start_time,peak_time_end).toMinutes();
									long diff_2_tariff = Duration.between(off_peak_time_start, charging_end_time).toMinutes();
									
									System.out.println(diff_1_tariff);
									System.out.println(diff_2_tariff);
									// Calculation Process
									System.out.println(charge_kwh);
									double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
									double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
									
									System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);
									System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
									
									double consume = rs.getDouble("total_consumption_amount");
									System.out.println("total_consume "+consume);
									double _session_1_price = (consume/total_minutes)*diff_1_tariff;
									double _session_2_price = (consume/total_minutes)*diff_2_tariff;
									
									System.out.println("1 Session Price "+_session_1_price);
									System.out.println("2 Session Price "+_session_2_price);
									
									// price 
									double electricity = kwh_1*day_price;
									
									
									double estimate_profit_session_1 = _session_1_price-electricity;
									HotelRateDTO dtoReport = new HotelRateDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(kwh_1);
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit_session_1);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(_session_1_price);
									dtoReport.setTime_zone("DAY");
									domestic.add(dtoReport);
									
									double electricityn = kwh_2*peak_price;
									
									double estimate_profitn = _session_2_price-electricityn;
									HotelRateDTO dtoReport1 = new HotelRateDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_2_tariff);
									dtoReport1.setCharge_kWh(kwh_2);
									dtoReport1.setElectricity_charge(electricityn);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profitn);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_2_price);
									dtoReport1.setTime_zone("PEAK");
									domestic.add(dtoReport1);
									
									System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 5");
									
								}else if(_charging_start_type == 3 && _charging_end_type ==  1) { //{3,1}
									
									long diffe = end_timestamp.getTime()-start_timestamp.getTime();
									System.out.println(diffe);
									long total_minutes = diffe/(1000*60);
									System.out.println(total_minutes);
									
									long diff_1_tariff = Duration.between(charging_start_time,off_peak_time_end).toMinutes();
									long diff_2_tariff = Duration.between(day_start_tariff, charging_end_time).toMinutes();
									
									System.out.println(diff_1_tariff);
									System.out.println(diff_2_tariff);
									// Calculation Process
									System.out.println(charge_kwh);
									double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
									double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
									
									System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);
									System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
									
									double consume = rs.getDouble("total_consumption_amount");
									System.out.println("total_consume "+consume);
									double _session_1_price = (consume/total_minutes)*diff_1_tariff;
									double _session_2_price = (consume/total_minutes)*diff_2_tariff;
									
									System.out.println("1 Session Price "+_session_1_price);
									System.out.println("2 Session Price "+_session_2_price);
									
									// price 
									double electricity = kwh_1*day_price;
									
									
									double estimate_profit_session_1 = _session_1_price-electricity;
									HotelRateDTO dtoReport = new HotelRateDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(kwh_1);
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit_session_1);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(_session_1_price);
									dtoReport.setTime_zone("DAY");
									domestic.add(dtoReport);
									
									double electricityn = kwh_2*peak_price;
									
									double estimate_profitn = _session_2_price-electricityn;
									HotelRateDTO dtoReport1 = new HotelRateDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_2_tariff);
									dtoReport1.setCharge_kWh(kwh_2);
									dtoReport1.setElectricity_charge(electricityn);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profitn);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_2_price);
									dtoReport1.setTime_zone("PEAK");
									domestic.add(dtoReport1);
									
									
									
									System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 6");
									
								}else if(_charging_start_type == 1 && _charging_end_type ==  3) { //{1,2,3}
									
									long diffe = end_timestamp.getTime()-start_timestamp.getTime();
									System.out.println(diffe);
									long total_minutes = diffe/(1000*60);
									System.out.println(total_minutes);
									
									long diff_1_tariff = Duration.between(charging_start_time,day_end_tariff).toMinutes();
									long diff_2_tariff = Duration.between(peak_time_start,peak_time_end).toMinutes();
									long diff_3_tariff = Duration.between(off_peak_time_start, charging_end_time).toMinutes();
									
									System.out.println(diff_1_tariff);
									System.out.println(diff_2_tariff);
									// Calculation Process
									System.out.println(charge_kwh);
									double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
									double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
									Double kwh_3 = (charge_kwh/total_minutes)*diff_3_tariff;
									
									System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);									
									System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
									System.out.println("Off peak "+off_peak_price+"-"+kwh_3*off_peak_price);
									
									double consume = rs.getDouble("total_consumption_amount");
									System.out.println("total_consume "+consume);
									double _session_1_price = (consume/total_minutes)*diff_1_tariff;
									double _session_2_price = (consume/total_minutes)*diff_2_tariff;
									double _session_3_price = (consume/total_minutes)*diff_2_tariff;
									System.out.println("1 Session Price "+_session_1_price);
									System.out.println("2 Session Price "+_session_2_price);
									System.out.println("3 Session Price "+_session_3_price);
									// price 
									double electricity = kwh_1*day_price;
									
									
									double estimate_profit_session_1 = _session_1_price-electricity;
									HotelRateDTO dtoReport = new HotelRateDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(kwh_1);
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit_session_1);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(_session_1_price);
									dtoReport.setTime_zone("DAY");
									domestic.add(dtoReport);
									
									double electricityn = kwh_2*peak_price;
									
									double estimate_profitn = _session_2_price-electricityn;
									HotelRateDTO dtoReport1 = new HotelRateDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_2_tariff);
									dtoReport1.setCharge_kWh(kwh_2);
									dtoReport1.setElectricity_charge(electricityn);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profitn);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_2_price);
									dtoReport1.setTime_zone("PEAK");
									domestic.add(dtoReport1);
									
									double electricity_session_3 = kwh_3*off_peak_price;
									double estimate_profit_sessin_3 = _session_3_price-electricity_session_3;
									HotelRateDTO dtoReport2 = new HotelRateDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_3_tariff);
									dtoReport1.setCharge_kWh(kwh_3);
									dtoReport1.setElectricity_charge(electricity_session_3);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profit_sessin_3);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profit_sessin_3);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_3_price);
									dtoReport1.setTime_zone("OFF PEAK");
									domestic.add(dtoReport2);
									
									
									System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 7");
									
								}else if(_charging_start_type == 3 && _charging_end_type ==  2) { //{3,1,2}
									
									long diffe = end_timestamp.getTime()-start_timestamp.getTime();
									System.out.println(diffe);
									long total_minutes = diffe/(1000*60);
									System.out.println(total_minutes);
									
									long diff_1_tariff = Duration.between(charging_start_time,off_peak_time_end).toMinutes();
									long diff_2_tariff = Duration.between(day_start_tariff,day_start_tariff).toMinutes();
									long diff_3_tariff = Duration.between(peak_time_start, charging_end_time).toMinutes();
									
									System.out.println(diff_1_tariff);
									System.out.println(diff_2_tariff);
									// Calculation Process
									System.out.println(charge_kwh);
									double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
									double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
									Double kwh_3 = (charge_kwh/total_minutes)*diff_3_tariff;
									
									
									double consume = rs.getDouble("total_consumption_amount");
									System.out.println("total_consume "+consume);
									double _session_1_price = (consume/total_minutes)*diff_1_tariff;
									double _session_2_price = (consume/total_minutes)*diff_2_tariff;
									double _session_3_price = (consume/total_minutes)*diff_2_tariff;
									System.out.println("1 Session Price "+_session_1_price);
									System.out.println("2 Session Price "+_session_2_price);
									System.out.println("3 Session Price "+_session_3_price);
									// price 
									double electricity = kwh_1*off_peak_price;
									
									
									double estimate_profit_session_1 = _session_1_price-electricity;
									HotelRateDTO dtoReport = new HotelRateDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(kwh_1);
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit_session_1);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(_session_1_price);
									dtoReport.setTime_zone("OFF PEAK");
									domestic.add(dtoReport);
									
									double electricityn = kwh_2*day_price;
									
									double estimate_profitn = _session_2_price-electricityn;
									HotelRateDTO dtoReport1 = new HotelRateDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_2_tariff);
									dtoReport1.setCharge_kWh(kwh_2);
									dtoReport1.setElectricity_charge(electricityn);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profitn);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_2_price);
									dtoReport1.setTime_zone("DAY");
									domestic.add(dtoReport1);
									
									double electricity_session_3 = kwh_3*peak_price;
									double estimate_profit_sessin_3 = _session_3_price-electricity_session_3;
									HotelRateDTO dtoReport2 = new HotelRateDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_3_tariff);
									dtoReport1.setCharge_kWh(kwh_3);
									dtoReport1.setElectricity_charge(electricity_session_3);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profit_sessin_3);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profit_sessin_3);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_3_price);
									dtoReport1.setTime_zone("PEAK");
									domestic.add(dtoReport2);
									
									
									System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 8");
									
								}else if(_charging_start_type == 2 && _charging_end_type ==  1) { //{2,3,1}
									
									long diffe = end_timestamp.getTime()-start_timestamp.getTime();
									System.out.println(diffe);
									long total_minutes = diffe/(1000*60);
									System.out.println(total_minutes);
									
									long diff_1_tariff = Duration.between(charging_start_time,peak_time_end).toMinutes();
									long diff_2_tariff = Duration.between(off_peak_time_start,off_peak_time_end).toMinutes();
									long diff_3_tariff = Duration.between(day_start_tariff, charging_end_time).toMinutes();
									
									System.out.println(diff_1_tariff);
									System.out.println(diff_2_tariff);
									// Calculation Process
									System.out.println(charge_kwh);
									double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
									double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
									Double kwh_3 = (charge_kwh/total_minutes)*diff_3_tariff;
									
									
									double consume = rs.getDouble("total_consumption_amount");
									System.out.println("total_consume "+consume);
									double _session_1_price = (consume/total_minutes)*diff_1_tariff;
									double _session_2_price = (consume/total_minutes)*diff_2_tariff;
									double _session_3_price = (consume/total_minutes)*diff_2_tariff;
									System.out.println("1 Session Price "+_session_1_price);
									System.out.println("2 Session Price "+_session_2_price);
									System.out.println("3 Session Price "+_session_3_price);
									// price 
									double electricity = kwh_1*peak_price;
									
									
									double estimate_profit_session_1 = _session_1_price-electricity;
									HotelRateDTO dtoReport = new HotelRateDTO();
									dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport.setCharge_duration(diff_1_tariff);
									dtoReport.setCharge_kWh(kwh_1);
									dtoReport.setElectricity_charge(electricity);
									dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport.setEstimated_profit(estimate_profit_session_1);
									dtoReport.setOrder_number(rs.getString("order_number"));
									dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
									dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport.setTotal_consumption_amount(_session_1_price);
									dtoReport.setTime_zone("PEAK");
									domestic.add(dtoReport);
									
									double electricityn = kwh_2*off_peak_price;
									
									double estimate_profitn = _session_2_price-electricityn;
									HotelRateDTO dtoReport1 = new HotelRateDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_2_tariff);
									dtoReport1.setCharge_kWh(kwh_2);
									dtoReport1.setElectricity_charge(electricityn);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profitn);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_2_price);
									dtoReport1.setTime_zone("OFF PEAK");
									domestic.add(dtoReport1);
									
									double electricity_session_3 = kwh_3*day_price;
									double estimate_profit_sessin_3 = _session_3_price-electricity_session_3;
									HotelRateDTO dtoReport2 = new HotelRateDTO();
									dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
									dtoReport1.setCharge_duration(diff_3_tariff);
									dtoReport1.setCharge_kWh(kwh_3);
									dtoReport1.setElectricity_charge(electricity_session_3);
									dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
									dtoReport1.setEstimated_profit(estimate_profit_sessin_3);
									dtoReport1.setOrder_number(rs.getString("order_number"));
									dtoReport1.setRevenue_without_vat_and_ssl(estimate_profit_sessin_3);
									dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
									dtoReport1.setTotal_consumption_amount(_session_3_price);
									dtoReport1.setTime_zone("DAY");
									domestic.add(dtoReport2);
									
									System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 9");
								}
																							
						}
						
				}
			
				}
		
		}catch(Exception e) {
				e.printStackTrace();
		}
		return domestic;
	}

public List<HotelRateDTO> StartBillCalculationProcessHotelIII(String station,String start_date,String end_date,int category,int subcategory,String gun_type){
	
	String sql="SELECT * FROM charge_order_information WHERE start_charging_time >= ? AND end_charging_time <= ? AND station_name = ?";
	List<HotelRateDTO> domestic = new ArrayList<>();
	
	double price_per_unit  = 0.0;
	
	try {
		Connection con = DBConnection.getConnection();
		PreparedStatement ps = con.prepareStatement(sql);			
			ps.setString(1, start_date);
			ps.setString(2, end_date);
			ps.setString(3, station);		
			ResultSet rs = ps.executeQuery();	
			
			
			double charge_kwh = 0.0;
			double day_price = 0.0;
			double peak_price = 0.0;
			double off_peak_price = 0.0;
			double fixed_charge = 0.0;
			double demand_charge = 0.0;
			
	
	String  sqlcategory = "SELECT * FROM other_consumer WHERE type = 7";
						PreparedStatement psn = con.prepareStatement(sqlcategory);
						ResultSet rsn = psn.executeQuery();
						//System.out.println(rsn.get);
						
						while(rsn.next()) {
							
							day_price = rsn.getDouble("day_price");
							peak_price = rsn.getDouble("peak_price");
							off_peak_price = rsn.getDouble("off_peak_price");
							fixed_charge =  rsn.getDouble("fixed_charge");
							demand_charge =  rsn.getDouble("demand_charge");
						}
						
						while(rs.next()) {
							//check Day
							Util util = new Util();
							charge_kwh = rs.getDouble("charge_kwh");
							//charging start time
							Timestamp start_timestamp = rs.getTimestamp("start_charging_time");
							LocalTime charging_start_time = start_timestamp.toLocalDateTime().toLocalTime();
							
							//charging end time 
							Timestamp end_timestamp = rs.getTimestamp("end_charging_time");
							LocalTime charging_end_time = end_timestamp.toLocalDateTime().toLocalTime();
							
							LocalTime day_start_tariff = LocalTime.parse(util.DAY_TIME_START);
							LocalTime day_end_tariff = LocalTime.parse(util.DAY_TIME_END);
							
							LocalTime peak_time_start = LocalTime.parse(util.PEAK_TIME_START);
							LocalTime peak_time_end = LocalTime.parse(util.PEAK_TIME_END);
							
							LocalTime off_peak_time_start = LocalTime.parse(util.OFF_PEAK_TIME_START);
							LocalTime off_peak_time_end = LocalTime.parse(util.OFF_PEAK_TIME_END);
							
							// FIRST CHECK START AND END TIME IN WHICH TARIFF
							
							int _charging_start_type = 0; // 1 = DAY, 2 = PEAK , 3 = OFF PEAK 
							int _charging_end_type = 0; // 1 = DAY, 2 = PEAK , 3 = OFF PEAK 
							
							if(charging_start_time.isAfter(day_start_tariff) && charging_start_time.isBefore(peak_time_start)) { // DAY
								
								_charging_start_type = 1;
																	
							}else if(charging_start_time.isAfter(peak_time_start) && charging_start_time.isBefore(off_peak_time_start)) { // PEAKE
								
								_charging_start_type = 2;
								
							}else if(charging_start_time.isAfter(off_peak_time_start) && charging_start_time.isBefore(day_start_tariff)) { // OFF PEAKE
								
								_charging_start_type = 3;
								
							}
							
							
							if(charging_end_time.isAfter(day_start_tariff) && charging_end_time.isBefore(peak_time_start)) { // DAY
								
								_charging_end_type = 1;
																	
							}else if(charging_end_time.isAfter(peak_time_start) && charging_end_time.isBefore(off_peak_time_start)) { // PEAKE
								
								_charging_end_type = 2;
								
							}else if(charging_end_time.isAfter(off_peak_time_start) && charging_end_time.isBefore(day_start_tariff)) { // OFF PEAKE
								
								_charging_end_type = 3;
								
							}
							
							
							//THE CYCLE PROCESS 
							
							if(_charging_start_type == 1 && _charging_end_type ==  1) { //{1}
																								
								long diff_1_tariff = Duration.between(charging_start_time,charging_end_time).toMinutes();
								double electricity = rs.getDouble("charge_kwh")*day_price;
								double consume = rs.getDouble("total_consumption_amount");
								double estimate_profit = consume-electricity;
								HotelRateDTO dtoReport = new HotelRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(rs.getDouble("charge_kwh"));
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(consume);
								dtoReport.setTime_zone("DAY");
								domestic.add(dtoReport);
								
								// System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 1");
								
							}else if(_charging_start_type == 2 && _charging_end_type ==  2) { //{2}
								
								long diff_1_tariff = Duration.between(charging_start_time,charging_end_time).toMinutes();
								double electricity = rs.getDouble("charge_kwh")*peak_price;
								double consume = rs.getDouble("total_consumption_amount");
								double estimate_profit = consume-electricity;
								HotelRateDTO dtoReport = new HotelRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(rs.getDouble("charge_kwh"));
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(consume);
								dtoReport.setTime_zone("PEAK");
								domestic.add(dtoReport);
								
								// System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 2");
								
							}else if(_charging_start_type == 3 && _charging_end_type ==  3) { //{3}
								
								long diff_1_tariff = Duration.between(charging_start_time,charging_end_time).toMinutes();
								double electricity = rs.getDouble("charge_kwh")*off_peak_price;
								double consume = rs.getDouble("total_consumption_amount");
								double estimate_profit = consume-electricity;
								HotelRateDTO dtoReport = new HotelRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(rs.getDouble("charge_kwh"));
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(consume);
								dtoReport.setTime_zone("OFF PEAK");
								domestic.add(dtoReport);
								
							//	System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 3");
								
							}else if(_charging_start_type == 1 && _charging_end_type ==  2) { //{1,2}
								
								long diffe = end_timestamp.getTime()-start_timestamp.getTime();
								System.out.println(diffe);
								long total_minutes = diffe/(1000*60);
								System.out.println(total_minutes);
								
								long diff_1_tariff = Duration.between(charging_start_time,day_end_tariff).toMinutes();
								long diff_2_tariff = Duration.between(peak_time_start, charging_end_time).toMinutes();
								
								System.out.println(diff_1_tariff);
								System.out.println(diff_2_tariff);
								// Calculation Process
								System.out.println(charge_kwh);
								double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
								double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
								
								System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);
								System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
								
								double consume = rs.getDouble("total_consumption_amount");
								System.out.println("total_consume "+consume);
								double _session_1_price = (consume/total_minutes)*diff_1_tariff;
								double _session_2_price = (consume/total_minutes)*diff_2_tariff;
								
								System.out.println("1 Session Price "+_session_1_price);
								System.out.println("2 Session Price "+_session_2_price);
								
								// price 
								double electricity = kwh_1*day_price;
								
								
								double estimate_profit_session_1 = _session_1_price-electricity;
								HotelRateDTO dtoReport = new HotelRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(kwh_1);
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit_session_1);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(_session_1_price);
								dtoReport.setTime_zone("DAY");
								domestic.add(dtoReport);
								
								double electricityn = kwh_2*peak_price;
								
								double estimate_profitn = _session_2_price-electricityn;
								HotelRateDTO dtoReport1 = new HotelRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_2_tariff);
								dtoReport1.setCharge_kWh(kwh_2);
								dtoReport1.setElectricity_charge(electricityn);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profitn);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_2_price);
								dtoReport1.setTime_zone("PEAK");
								domestic.add(dtoReport1);
								
								
								System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 4");
															
							}else if(_charging_start_type == 2 && _charging_end_type ==  3) { //{2,3}
								
								long diffe = end_timestamp.getTime()-start_timestamp.getTime();
								System.out.println(diffe);
								long total_minutes = diffe/(1000*60);
								System.out.println(total_minutes);
								
								long diff_1_tariff = Duration.between(charging_start_time,peak_time_end).toMinutes();
								long diff_2_tariff = Duration.between(off_peak_time_start, charging_end_time).toMinutes();
								
								System.out.println(diff_1_tariff);
								System.out.println(diff_2_tariff);
								// Calculation Process
								System.out.println(charge_kwh);
								double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
								double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
								
								System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);
								System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
								
								double consume = rs.getDouble("total_consumption_amount");
								System.out.println("total_consume "+consume);
								double _session_1_price = (consume/total_minutes)*diff_1_tariff;
								double _session_2_price = (consume/total_minutes)*diff_2_tariff;
								
								System.out.println("1 Session Price "+_session_1_price);
								System.out.println("2 Session Price "+_session_2_price);
								
								// price 
								double electricity = kwh_1*day_price;
								
								
								double estimate_profit_session_1 = _session_1_price-electricity;
								HotelRateDTO dtoReport = new HotelRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(kwh_1);
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit_session_1);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(_session_1_price);
								dtoReport.setTime_zone("DAY");
								domestic.add(dtoReport);
								
								double electricityn = kwh_2*peak_price;
								
								double estimate_profitn = _session_2_price-electricityn;
								HotelRateDTO dtoReport1 = new HotelRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_2_tariff);
								dtoReport1.setCharge_kWh(kwh_2);
								dtoReport1.setElectricity_charge(electricityn);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profitn);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_2_price);
								dtoReport1.setTime_zone("PEAK");
								domestic.add(dtoReport1);
								
								System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 5");
								
							}else if(_charging_start_type == 3 && _charging_end_type ==  1) { //{3,1}
								
								long diffe = end_timestamp.getTime()-start_timestamp.getTime();
								System.out.println(diffe);
								long total_minutes = diffe/(1000*60);
								System.out.println(total_minutes);
								
								long diff_1_tariff = Duration.between(charging_start_time,off_peak_time_end).toMinutes();
								long diff_2_tariff = Duration.between(day_start_tariff, charging_end_time).toMinutes();
								
								System.out.println(diff_1_tariff);
								System.out.println(diff_2_tariff);
								// Calculation Process
								System.out.println(charge_kwh);
								double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
								double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
								
								System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);
								System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
								
								double consume = rs.getDouble("total_consumption_amount");
								System.out.println("total_consume "+consume);
								double _session_1_price = (consume/total_minutes)*diff_1_tariff;
								double _session_2_price = (consume/total_minutes)*diff_2_tariff;
								
								System.out.println("1 Session Price "+_session_1_price);
								System.out.println("2 Session Price "+_session_2_price);
								
								// price 
								double electricity = kwh_1*day_price;
								
								
								double estimate_profit_session_1 = _session_1_price-electricity;
								HotelRateDTO dtoReport = new HotelRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(kwh_1);
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit_session_1);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(_session_1_price);
								dtoReport.setTime_zone("DAY");
								domestic.add(dtoReport);
								
								double electricityn = kwh_2*peak_price;
								
								double estimate_profitn = _session_2_price-electricityn;
								HotelRateDTO dtoReport1 = new HotelRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_2_tariff);
								dtoReport1.setCharge_kWh(kwh_2);
								dtoReport1.setElectricity_charge(electricityn);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profitn);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_2_price);
								dtoReport1.setTime_zone("PEAK");
								domestic.add(dtoReport1);
								
								
								
								System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 6");
								
							}else if(_charging_start_type == 1 && _charging_end_type ==  3) { //{1,2,3}
								
								long diffe = end_timestamp.getTime()-start_timestamp.getTime();
								System.out.println(diffe);
								long total_minutes = diffe/(1000*60);
								System.out.println(total_minutes);
								
								long diff_1_tariff = Duration.between(charging_start_time,day_end_tariff).toMinutes();
								long diff_2_tariff = Duration.between(peak_time_start,peak_time_end).toMinutes();
								long diff_3_tariff = Duration.between(off_peak_time_start, charging_end_time).toMinutes();
								
								System.out.println(diff_1_tariff);
								System.out.println(diff_2_tariff);
								// Calculation Process
								System.out.println(charge_kwh);
								double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
								double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
								Double kwh_3 = (charge_kwh/total_minutes)*diff_3_tariff;
								
								System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);									
								System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
								System.out.println("Off peak "+off_peak_price+"-"+kwh_3*off_peak_price);
								
								double consume = rs.getDouble("total_consumption_amount");
								System.out.println("total_consume "+consume);
								double _session_1_price = (consume/total_minutes)*diff_1_tariff;
								double _session_2_price = (consume/total_minutes)*diff_2_tariff;
								double _session_3_price = (consume/total_minutes)*diff_2_tariff;
								System.out.println("1 Session Price "+_session_1_price);
								System.out.println("2 Session Price "+_session_2_price);
								System.out.println("3 Session Price "+_session_3_price);
								// price 
								double electricity = kwh_1*day_price;
								
								
								double estimate_profit_session_1 = _session_1_price-electricity;
								HotelRateDTO dtoReport = new HotelRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(kwh_1);
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit_session_1);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(_session_1_price);
								dtoReport.setTime_zone("DAY");
								domestic.add(dtoReport);
								
								double electricityn = kwh_2*peak_price;
								
								double estimate_profitn = _session_2_price-electricityn;
								HotelRateDTO dtoReport1 = new HotelRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_2_tariff);
								dtoReport1.setCharge_kWh(kwh_2);
								dtoReport1.setElectricity_charge(electricityn);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profitn);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_2_price);
								dtoReport1.setTime_zone("PEAK");
								domestic.add(dtoReport1);
								
								double electricity_session_3 = kwh_3*off_peak_price;
								double estimate_profit_sessin_3 = _session_3_price-electricity_session_3;
								HotelRateDTO dtoReport2 = new HotelRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_3_tariff);
								dtoReport1.setCharge_kWh(kwh_3);
								dtoReport1.setElectricity_charge(electricity_session_3);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profit_sessin_3);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profit_sessin_3);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_3_price);
								dtoReport1.setTime_zone("OFF PEAK");
								domestic.add(dtoReport2);
								
								
								System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 7");
								
							}else if(_charging_start_type == 3 && _charging_end_type ==  2) { //{3,1,2}
								
								long diffe = end_timestamp.getTime()-start_timestamp.getTime();
								System.out.println(diffe);
								long total_minutes = diffe/(1000*60);
								System.out.println(total_minutes);
								
								long diff_1_tariff = Duration.between(charging_start_time,off_peak_time_end).toMinutes();
								long diff_2_tariff = Duration.between(day_start_tariff,day_start_tariff).toMinutes();
								long diff_3_tariff = Duration.between(peak_time_start, charging_end_time).toMinutes();
								
								System.out.println(diff_1_tariff);
								System.out.println(diff_2_tariff);
								// Calculation Process
								System.out.println(charge_kwh);
								double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
								double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
								Double kwh_3 = (charge_kwh/total_minutes)*diff_3_tariff;
								
								
								double consume = rs.getDouble("total_consumption_amount");
								System.out.println("total_consume "+consume);
								double _session_1_price = (consume/total_minutes)*diff_1_tariff;
								double _session_2_price = (consume/total_minutes)*diff_2_tariff;
								double _session_3_price = (consume/total_minutes)*diff_2_tariff;
								System.out.println("1 Session Price "+_session_1_price);
								System.out.println("2 Session Price "+_session_2_price);
								System.out.println("3 Session Price "+_session_3_price);
								// price 
								double electricity = kwh_1*off_peak_price;
								
								
								double estimate_profit_session_1 = _session_1_price-electricity;
								HotelRateDTO dtoReport = new HotelRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(kwh_1);
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit_session_1);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(_session_1_price);
								dtoReport.setTime_zone("OFF PEAK");
								domestic.add(dtoReport);
								
								double electricityn = kwh_2*day_price;
								
								double estimate_profitn = _session_2_price-electricityn;
								HotelRateDTO dtoReport1 = new HotelRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_2_tariff);
								dtoReport1.setCharge_kWh(kwh_2);
								dtoReport1.setElectricity_charge(electricityn);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profitn);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_2_price);
								dtoReport1.setTime_zone("DAY");
								domestic.add(dtoReport1);
								
								double electricity_session_3 = kwh_3*peak_price;
								double estimate_profit_sessin_3 = _session_3_price-electricity_session_3;
								HotelRateDTO dtoReport2 = new HotelRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_3_tariff);
								dtoReport1.setCharge_kWh(kwh_3);
								dtoReport1.setElectricity_charge(electricity_session_3);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profit_sessin_3);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profit_sessin_3);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_3_price);
								dtoReport1.setTime_zone("PEAK");
								domestic.add(dtoReport2);
								
								
								System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 8");
								
							}else if(_charging_start_type == 2 && _charging_end_type ==  1) { //{2,3,1}
								
								long diffe = end_timestamp.getTime()-start_timestamp.getTime();
								System.out.println(diffe);
								long total_minutes = diffe/(1000*60);
								System.out.println(total_minutes);
								
								long diff_1_tariff = Duration.between(charging_start_time,peak_time_end).toMinutes();
								long diff_2_tariff = Duration.between(off_peak_time_start,off_peak_time_end).toMinutes();
								long diff_3_tariff = Duration.between(day_start_tariff, charging_end_time).toMinutes();
								
								System.out.println(diff_1_tariff);
								System.out.println(diff_2_tariff);
								// Calculation Process
								System.out.println(charge_kwh);
								double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
								double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
								Double kwh_3 = (charge_kwh/total_minutes)*diff_3_tariff;
								
								
								double consume = rs.getDouble("total_consumption_amount");
								System.out.println("total_consume "+consume);
								double _session_1_price = (consume/total_minutes)*diff_1_tariff;
								double _session_2_price = (consume/total_minutes)*diff_2_tariff;
								double _session_3_price = (consume/total_minutes)*diff_2_tariff;
								System.out.println("1 Session Price "+_session_1_price);
								System.out.println("2 Session Price "+_session_2_price);
								System.out.println("3 Session Price "+_session_3_price);
								// price 
								double electricity = kwh_1*peak_price;
								
								
								double estimate_profit_session_1 = _session_1_price-electricity;
								HotelRateDTO dtoReport = new HotelRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(kwh_1);
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit_session_1);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(_session_1_price);
								dtoReport.setTime_zone("PEAK");
								domestic.add(dtoReport);
								
								double electricityn = kwh_2*off_peak_price;
								
								double estimate_profitn = _session_2_price-electricityn;
								HotelRateDTO dtoReport1 = new HotelRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_2_tariff);
								dtoReport1.setCharge_kWh(kwh_2);
								dtoReport1.setElectricity_charge(electricityn);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profitn);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_2_price);
								dtoReport1.setTime_zone("OFF PEAK");
								domestic.add(dtoReport1);
								
								double electricity_session_3 = kwh_3*day_price;
								double estimate_profit_sessin_3 = _session_3_price-electricity_session_3;
								HotelRateDTO dtoReport2 = new HotelRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_3_tariff);
								dtoReport1.setCharge_kWh(kwh_3);
								dtoReport1.setElectricity_charge(electricity_session_3);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profit_sessin_3);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profit_sessin_3);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_3_price);
								dtoReport1.setTime_zone("DAY");
								domestic.add(dtoReport2);
								
								System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 9");
							}
																						
					}
					
			
		
			
	
	}catch(Exception e) {
			e.printStackTrace();
	}
	return domestic;
}

public List<GeneralPurposeRateDTO> getChargerGeneralPurposeRate(String station,String start_date,String end_date,int category,int subcategory,String gun_type){
	
	String sql="SELECT * FROM charge_order_information WHERE start_charging_time >= ? AND end_charging_time <= ? AND station_name = ?";
	List<GeneralPurposeRateDTO> domestic = new ArrayList<>();
	
	double price_per_unit  = 0.0;
	
	try {
		Connection con = DBConnection.getConnection();
		PreparedStatement ps = con.prepareStatement(sql);			
			ps.setString(1, start_date);
			ps.setString(2, end_date);
			ps.setString(3, station);		
			ResultSet rs = ps.executeQuery();	
			
			String total_kwh_query = "SELECT SUM(charge_kwh) as total FROM charge_order_information WHERE station_name = ? ";
			PreparedStatement ps_tkq = con.prepareStatement(total_kwh_query);
			ps_tkq.setString(1, station);
			ResultSet resultSet = ps_tkq.executeQuery();
			
			double charge_kwh = 0.0;
			double day_price = 0.0;
			double peak_price = 0.0;
			double off_peak_price = 0.0;
			double fixed_charge = 0.0;
			double demand_charge = 0.0;
			
			if(resultSet.next()) {
				
				
		
					if(resultSet.getDouble("total") <= 180) {
						
						String  sqlcategory = "SELECT * FROM other_consumer WHERE type = 8";
						PreparedStatement psn = con.prepareStatement(sqlcategory);
						ResultSet rsn = psn.executeQuery();
						//System.out.println(rsn.get);
						
	while(rsn.next()) {
							
							day_price = rsn.getDouble("day_price");
							peak_price = rsn.getDouble("peak_price");
							off_peak_price = rsn.getDouble("off_peak_price");
							fixed_charge =  rsn.getDouble("fixed_charge");
							demand_charge =  rsn.getDouble("demand_charge");
						}
						
						while(rs.next()) {
							//check Day
							Util util = new Util();
							charge_kwh = rs.getDouble("charge_kwh");
							//charging start time
							Timestamp start_timestamp = rs.getTimestamp("start_charging_time");
							LocalTime charging_start_time = start_timestamp.toLocalDateTime().toLocalTime();
							
							//charging end time 
							Timestamp end_timestamp = rs.getTimestamp("end_charging_time");
							LocalTime charging_end_time = end_timestamp.toLocalDateTime().toLocalTime();
							
							LocalTime day_start_tariff = LocalTime.parse(util.DAY_TIME_START);
							LocalTime day_end_tariff = LocalTime.parse(util.DAY_TIME_END);
							
							LocalTime peak_time_start = LocalTime.parse(util.PEAK_TIME_START);
							LocalTime peak_time_end = LocalTime.parse(util.PEAK_TIME_END);
							
							LocalTime off_peak_time_start = LocalTime.parse(util.OFF_PEAK_TIME_START);
							LocalTime off_peak_time_end = LocalTime.parse(util.OFF_PEAK_TIME_END);
							
							// FIRST CHECK START AND END TIME IN WHICH TARIFF
							
							int _charging_start_type = 0; // 1 = DAY, 2 = PEAK , 3 = OFF PEAK 
							int _charging_end_type = 0; // 1 = DAY, 2 = PEAK , 3 = OFF PEAK 
							
							if(charging_start_time.isAfter(day_start_tariff) && charging_start_time.isBefore(peak_time_start)) { // DAY
								
								_charging_start_type = 1;
																	
							}else if(charging_start_time.isAfter(peak_time_start) && charging_start_time.isBefore(off_peak_time_start)) { // PEAKE
								
								_charging_start_type = 2;
								
							}else if(charging_start_time.isAfter(off_peak_time_start) && charging_start_time.isBefore(day_start_tariff)) { // OFF PEAKE
								
								_charging_start_type = 3;
								
							}
							
							
							if(charging_end_time.isAfter(day_start_tariff) && charging_end_time.isBefore(peak_time_start)) { // DAY
								
								_charging_end_type = 1;
																	
							}else if(charging_end_time.isAfter(peak_time_start) && charging_end_time.isBefore(off_peak_time_start)) { // PEAKE
								
								_charging_end_type = 2;
								
							}else if(charging_end_time.isAfter(off_peak_time_start) && charging_end_time.isBefore(day_start_tariff)) { // OFF PEAKE
								
								_charging_end_type = 3;
								
							}
							
							
							//THE CYCLE PROCESS 
							
							if(_charging_start_type == 1 && _charging_end_type ==  1) { //{1}
																								
								long diff_1_tariff = Duration.between(charging_start_time,charging_end_time).toMinutes();
								double electricity = rs.getDouble("charge_kwh")*day_price;
								double consume = rs.getDouble("total_consumption_amount");
								double estimate_profit = consume-electricity;
								GeneralPurposeRateDTO dtoReport = new GeneralPurposeRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(rs.getDouble("charge_kwh"));
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(consume);
								dtoReport.setTime_zone("DAY");
								domestic.add(dtoReport);
								
								// System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 1");
								
							}else if(_charging_start_type == 2 && _charging_end_type ==  2) { //{2}
								
								long diff_1_tariff = Duration.between(charging_start_time,charging_end_time).toMinutes();
								double electricity = rs.getDouble("charge_kwh")*peak_price;
								double consume = rs.getDouble("total_consumption_amount");
								double estimate_profit = consume-electricity;
								GeneralPurposeRateDTO dtoReport = new GeneralPurposeRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(rs.getDouble("charge_kwh"));
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(consume);
								dtoReport.setTime_zone("PEAK");
								domestic.add(dtoReport);
								
								// System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 2");
								
							}else if(_charging_start_type == 3 && _charging_end_type ==  3) { //{3}
								
								long diff_1_tariff = Duration.between(charging_start_time,charging_end_time).toMinutes();
								double electricity = rs.getDouble("charge_kwh")*off_peak_price;
								double consume = rs.getDouble("total_consumption_amount");
								double estimate_profit = consume-electricity;
								GeneralPurposeRateDTO dtoReport = new GeneralPurposeRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(rs.getDouble("charge_kwh"));
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(consume);
								dtoReport.setTime_zone("OFF PEAK");
								domestic.add(dtoReport);
								
							//	System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 3");
								
							}else if(_charging_start_type == 1 && _charging_end_type ==  2) { //{1,2}
								
								long diffe = end_timestamp.getTime()-start_timestamp.getTime();
								System.out.println(diffe);
								long total_minutes = diffe/(1000*60);
								System.out.println(total_minutes);
								
								long diff_1_tariff = Duration.between(charging_start_time,day_end_tariff).toMinutes();
								long diff_2_tariff = Duration.between(peak_time_start, charging_end_time).toMinutes();
								
								System.out.println(diff_1_tariff);
								System.out.println(diff_2_tariff);
								// Calculation Process
								System.out.println(charge_kwh);
								double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
								double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
								
								System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);
								System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
								
								double consume = rs.getDouble("total_consumption_amount");
								System.out.println("total_consume "+consume);
								double _session_1_price = (consume/total_minutes)*diff_1_tariff;
								double _session_2_price = (consume/total_minutes)*diff_2_tariff;
								
								System.out.println("1 Session Price "+_session_1_price);
								System.out.println("2 Session Price "+_session_2_price);
								
								// price 
								double electricity = kwh_1*day_price;
								
								
								double estimate_profit_session_1 = _session_1_price-electricity;
								GeneralPurposeRateDTO dtoReport = new GeneralPurposeRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(kwh_1);
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit_session_1);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(_session_1_price);
								dtoReport.setTime_zone("DAY");
								domestic.add(dtoReport);
								
								double electricityn = kwh_2*peak_price;
								
								double estimate_profitn = _session_2_price-electricityn;
								GeneralPurposeRateDTO dtoReport1 = new GeneralPurposeRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_2_tariff);
								dtoReport1.setCharge_kWh(kwh_2);
								dtoReport1.setElectricity_charge(electricityn);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profitn);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_2_price);
								dtoReport1.setTime_zone("PEAK");
								domestic.add(dtoReport1);
								
								
								System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 4");
															
							}else if(_charging_start_type == 2 && _charging_end_type ==  3) { //{2,3}
								
								long diffe = end_timestamp.getTime()-start_timestamp.getTime();
								System.out.println(diffe);
								long total_minutes = diffe/(1000*60);
								System.out.println(total_minutes);
								
								long diff_1_tariff = Duration.between(charging_start_time,peak_time_end).toMinutes();
								long diff_2_tariff = Duration.between(off_peak_time_start, charging_end_time).toMinutes();
								
								System.out.println(diff_1_tariff);
								System.out.println(diff_2_tariff);
								// Calculation Process
								System.out.println(charge_kwh);
								double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
								double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
								
								System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);
								System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
								
								double consume = rs.getDouble("total_consumption_amount");
								System.out.println("total_consume "+consume);
								double _session_1_price = (consume/total_minutes)*diff_1_tariff;
								double _session_2_price = (consume/total_minutes)*diff_2_tariff;
								
								System.out.println("1 Session Price "+_session_1_price);
								System.out.println("2 Session Price "+_session_2_price);
								
								// price 
								double electricity = kwh_1*day_price;
								
								
								double estimate_profit_session_1 = _session_1_price-electricity;
								GeneralPurposeRateDTO dtoReport = new GeneralPurposeRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(kwh_1);
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit_session_1);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(_session_1_price);
								dtoReport.setTime_zone("DAY");
								domestic.add(dtoReport);
								
								double electricityn = kwh_2*peak_price;
								
								double estimate_profitn = _session_2_price-electricityn;
								GeneralPurposeRateDTO dtoReport1 = new GeneralPurposeRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_2_tariff);
								dtoReport1.setCharge_kWh(kwh_2);
								dtoReport1.setElectricity_charge(electricityn);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profitn);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_2_price);
								dtoReport1.setTime_zone("PEAK");
								domestic.add(dtoReport1);
								
								System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 5");
								
							}else if(_charging_start_type == 3 && _charging_end_type ==  1) { //{3,1}
								
								long diffe = end_timestamp.getTime()-start_timestamp.getTime();
								System.out.println(diffe);
								long total_minutes = diffe/(1000*60);
								System.out.println(total_minutes);
								
								long diff_1_tariff = Duration.between(charging_start_time,off_peak_time_end).toMinutes();
								long diff_2_tariff = Duration.between(day_start_tariff, charging_end_time).toMinutes();
								
								System.out.println(diff_1_tariff);
								System.out.println(diff_2_tariff);
								// Calculation Process
								System.out.println(charge_kwh);
								double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
								double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
								
								System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);
								System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
								
								double consume = rs.getDouble("total_consumption_amount");
								System.out.println("total_consume "+consume);
								double _session_1_price = (consume/total_minutes)*diff_1_tariff;
								double _session_2_price = (consume/total_minutes)*diff_2_tariff;
								
								System.out.println("1 Session Price "+_session_1_price);
								System.out.println("2 Session Price "+_session_2_price);
								
								// price 
								double electricity = kwh_1*day_price;
								
								
								double estimate_profit_session_1 = _session_1_price-electricity;
								GeneralPurposeRateDTO dtoReport = new GeneralPurposeRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(kwh_1);
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit_session_1);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(_session_1_price);
								dtoReport.setTime_zone("DAY");
								domestic.add(dtoReport);
								
								double electricityn = kwh_2*peak_price;
								
								double estimate_profitn = _session_2_price-electricityn;
								GeneralPurposeRateDTO dtoReport1 = new GeneralPurposeRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_2_tariff);
								dtoReport1.setCharge_kWh(kwh_2);
								dtoReport1.setElectricity_charge(electricityn);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profitn);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_2_price);
								dtoReport1.setTime_zone("PEAK");
								domestic.add(dtoReport1);
								
								
								
								System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 6");
								
							}else if(_charging_start_type == 1 && _charging_end_type ==  3) { //{1,2,3}
								
								long diffe = end_timestamp.getTime()-start_timestamp.getTime();
								System.out.println(diffe);
								long total_minutes = diffe/(1000*60);
								System.out.println(total_minutes);
								
								long diff_1_tariff = Duration.between(charging_start_time,day_end_tariff).toMinutes();
								long diff_2_tariff = Duration.between(peak_time_start,peak_time_end).toMinutes();
								long diff_3_tariff = Duration.between(off_peak_time_start, charging_end_time).toMinutes();
								
								System.out.println(diff_1_tariff);
								System.out.println(diff_2_tariff);
								// Calculation Process
								System.out.println(charge_kwh);
								double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
								double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
								Double kwh_3 = (charge_kwh/total_minutes)*diff_3_tariff;
								
								System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);									
								System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
								System.out.println("Off peak "+off_peak_price+"-"+kwh_3*off_peak_price);
								
								double consume = rs.getDouble("total_consumption_amount");
								System.out.println("total_consume "+consume);
								double _session_1_price = (consume/total_minutes)*diff_1_tariff;
								double _session_2_price = (consume/total_minutes)*diff_2_tariff;
								double _session_3_price = (consume/total_minutes)*diff_2_tariff;
								System.out.println("1 Session Price "+_session_1_price);
								System.out.println("2 Session Price "+_session_2_price);
								System.out.println("3 Session Price "+_session_3_price);
								// price 
								double electricity = kwh_1*day_price;
								
								
								double estimate_profit_session_1 = _session_1_price-electricity;
								GeneralPurposeRateDTO dtoReport = new GeneralPurposeRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(kwh_1);
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit_session_1);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(_session_1_price);
								dtoReport.setTime_zone("DAY");
								domestic.add(dtoReport);
								
								double electricityn = kwh_2*peak_price;
								
								double estimate_profitn = _session_2_price-electricityn;
								GeneralPurposeRateDTO dtoReport1 = new GeneralPurposeRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_2_tariff);
								dtoReport1.setCharge_kWh(kwh_2);
								dtoReport1.setElectricity_charge(electricityn);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profitn);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_2_price);
								dtoReport1.setTime_zone("PEAK");
								domestic.add(dtoReport1);
								
								double electricity_session_3 = kwh_3*off_peak_price;
								double estimate_profit_sessin_3 = _session_3_price-electricity_session_3;
								GeneralPurposeRateDTO dtoReport2 = new GeneralPurposeRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_3_tariff);
								dtoReport1.setCharge_kWh(kwh_3);
								dtoReport1.setElectricity_charge(electricity_session_3);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profit_sessin_3);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profit_sessin_3);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_3_price);
								dtoReport1.setTime_zone("OFF PEAK");
								domestic.add(dtoReport2);
								
								
								System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 7");
								
							}else if(_charging_start_type == 3 && _charging_end_type ==  2) { //{3,1,2}
								
								long diffe = end_timestamp.getTime()-start_timestamp.getTime();
								System.out.println(diffe);
								long total_minutes = diffe/(1000*60);
								System.out.println(total_minutes);
								
								long diff_1_tariff = Duration.between(charging_start_time,off_peak_time_end).toMinutes();
								long diff_2_tariff = Duration.between(day_start_tariff,day_start_tariff).toMinutes();
								long diff_3_tariff = Duration.between(peak_time_start, charging_end_time).toMinutes();
								
								System.out.println(diff_1_tariff);
								System.out.println(diff_2_tariff);
								// Calculation Process
								System.out.println(charge_kwh);
								double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
								double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
								Double kwh_3 = (charge_kwh/total_minutes)*diff_3_tariff;
								
								
								double consume = rs.getDouble("total_consumption_amount");
								System.out.println("total_consume "+consume);
								double _session_1_price = (consume/total_minutes)*diff_1_tariff;
								double _session_2_price = (consume/total_minutes)*diff_2_tariff;
								double _session_3_price = (consume/total_minutes)*diff_2_tariff;
								System.out.println("1 Session Price "+_session_1_price);
								System.out.println("2 Session Price "+_session_2_price);
								System.out.println("3 Session Price "+_session_3_price);
								// price 
								double electricity = kwh_1*off_peak_price;
								
								
								double estimate_profit_session_1 = _session_1_price-electricity;
								GeneralPurposeRateDTO dtoReport = new GeneralPurposeRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(kwh_1);
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit_session_1);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(_session_1_price);
								dtoReport.setTime_zone("OFF PEAK");
								domestic.add(dtoReport);
								
								double electricityn = kwh_2*day_price;
								
								double estimate_profitn = _session_2_price-electricityn;
								GeneralPurposeRateDTO dtoReport1 = new GeneralPurposeRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_2_tariff);
								dtoReport1.setCharge_kWh(kwh_2);
								dtoReport1.setElectricity_charge(electricityn);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profitn);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_2_price);
								dtoReport1.setTime_zone("DAY");
								domestic.add(dtoReport1);
								
								double electricity_session_3 = kwh_3*peak_price;
								double estimate_profit_sessin_3 = _session_3_price-electricity_session_3;
								GeneralPurposeRateDTO dtoReport2 = new GeneralPurposeRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_3_tariff);
								dtoReport1.setCharge_kWh(kwh_3);
								dtoReport1.setElectricity_charge(electricity_session_3);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profit_sessin_3);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profit_sessin_3);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_3_price);
								dtoReport1.setTime_zone("PEAK");
								domestic.add(dtoReport2);
								
								
								System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 8");
								
							}else if(_charging_start_type == 2 && _charging_end_type ==  1) { //{2,3,1}
								
								long diffe = end_timestamp.getTime()-start_timestamp.getTime();
								System.out.println(diffe);
								long total_minutes = diffe/(1000*60);
								System.out.println(total_minutes);
								
								long diff_1_tariff = Duration.between(charging_start_time,peak_time_end).toMinutes();
								long diff_2_tariff = Duration.between(off_peak_time_start,off_peak_time_end).toMinutes();
								long diff_3_tariff = Duration.between(day_start_tariff, charging_end_time).toMinutes();
								
								System.out.println(diff_1_tariff);
								System.out.println(diff_2_tariff);
								// Calculation Process
								System.out.println(charge_kwh);
								double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
								double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
								Double kwh_3 = (charge_kwh/total_minutes)*diff_3_tariff;
								
								
								double consume = rs.getDouble("total_consumption_amount");
								System.out.println("total_consume "+consume);
								double _session_1_price = (consume/total_minutes)*diff_1_tariff;
								double _session_2_price = (consume/total_minutes)*diff_2_tariff;
								double _session_3_price = (consume/total_minutes)*diff_2_tariff;
								System.out.println("1 Session Price "+_session_1_price);
								System.out.println("2 Session Price "+_session_2_price);
								System.out.println("3 Session Price "+_session_3_price);
								// price 
								double electricity = kwh_1*peak_price;
								
								
								double estimate_profit_session_1 = _session_1_price-electricity;
								GeneralPurposeRateDTO dtoReport = new GeneralPurposeRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(kwh_1);
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit_session_1);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(_session_1_price);
								dtoReport.setTime_zone("PEAK");
								domestic.add(dtoReport);
								
								double electricityn = kwh_2*off_peak_price;
								
								double estimate_profitn = _session_2_price-electricityn;
								GeneralPurposeRateDTO dtoReport1 = new GeneralPurposeRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_2_tariff);
								dtoReport1.setCharge_kWh(kwh_2);
								dtoReport1.setElectricity_charge(electricityn);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profitn);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_2_price);
								dtoReport1.setTime_zone("OFF PEAK");
								domestic.add(dtoReport1);
								
								double electricity_session_3 = kwh_3*day_price;
								double estimate_profit_sessin_3 = _session_3_price-electricity_session_3;
								GeneralPurposeRateDTO dtoReport2 = new GeneralPurposeRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_3_tariff);
								dtoReport1.setCharge_kWh(kwh_3);
								dtoReport1.setElectricity_charge(electricity_session_3);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profit_sessin_3);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profit_sessin_3);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_3_price);
								dtoReport1.setTime_zone("DAY");
								domestic.add(dtoReport2);
								
								System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 9");
							}
																						
					}
						
						
						
					}else if(resultSet.getDouble("total") > 180) {
						
						String  sqlcategory = "SELECT * FROM other_consumer WHERE type = 2";
						PreparedStatement psn = con.prepareStatement(sqlcategory);
						ResultSet rsn = psn.executeQuery();
						//System.out.println(rsn.get);
						
						while(rsn.next()) {
							
							day_price = rsn.getDouble("day_price");
							peak_price = rsn.getDouble("peak_price");
							off_peak_price = rsn.getDouble("off_peak_price");
							fixed_charge =  rsn.getDouble("fixed_charge");
							demand_charge =  rsn.getDouble("demand_charge");
						}
						
						while(rs.next()) {
							//check Day
							Util util = new Util();
							charge_kwh = rs.getDouble("charge_kwh");
							//charging start time
							Timestamp start_timestamp = rs.getTimestamp("start_charging_time");
							LocalTime charging_start_time = start_timestamp.toLocalDateTime().toLocalTime();
							
							//charging end time 
							Timestamp end_timestamp = rs.getTimestamp("end_charging_time");
							LocalTime charging_end_time = end_timestamp.toLocalDateTime().toLocalTime();
							
							LocalTime day_start_tariff = LocalTime.parse(util.DAY_TIME_START);
							LocalTime day_end_tariff = LocalTime.parse(util.DAY_TIME_END);
							
							LocalTime peak_time_start = LocalTime.parse(util.PEAK_TIME_START);
							LocalTime peak_time_end = LocalTime.parse(util.PEAK_TIME_END);
							
							LocalTime off_peak_time_start = LocalTime.parse(util.OFF_PEAK_TIME_START);
							LocalTime off_peak_time_end = LocalTime.parse(util.OFF_PEAK_TIME_END);
							
							// FIRST CHECK START AND END TIME IN WHICH TARIFF
							
							int _charging_start_type = 0; // 1 = DAY, 2 = PEAK , 3 = OFF PEAK 
							int _charging_end_type = 0; // 1 = DAY, 2 = PEAK , 3 = OFF PEAK 
							
							if(charging_start_time.isAfter(day_start_tariff) && charging_start_time.isBefore(peak_time_start)) { // DAY
								
								_charging_start_type = 1;
																	
							}else if(charging_start_time.isAfter(peak_time_start) && charging_start_time.isBefore(off_peak_time_start)) { // PEAKE
								
								_charging_start_type = 2;
								
							}else if(charging_start_time.isAfter(off_peak_time_start) && charging_start_time.isBefore(day_start_tariff)) { // OFF PEAKE
								
								_charging_start_type = 3;
								
							}
							
							
							if(charging_end_time.isAfter(day_start_tariff) && charging_end_time.isBefore(peak_time_start)) { // DAY
								
								_charging_end_type = 1;
																	
							}else if(charging_end_time.isAfter(peak_time_start) && charging_end_time.isBefore(off_peak_time_start)) { // PEAKE
								
								_charging_end_type = 2;
								
							}else if(charging_end_time.isAfter(off_peak_time_start) && charging_end_time.isBefore(day_start_tariff)) { // OFF PEAKE
								
								_charging_end_type = 3;
								
							}
							
							
							//THE CYCLE PROCESS 
							
							if(_charging_start_type == 1 && _charging_end_type ==  1) { //{1}
																								
								long diff_1_tariff = Duration.between(charging_start_time,charging_end_time).toMinutes();
								double electricity = rs.getDouble("charge_kwh")*day_price;
								double consume = rs.getDouble("total_consumption_amount");
								double estimate_profit = consume-electricity;
								GeneralPurposeRateDTO dtoReport = new GeneralPurposeRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(rs.getDouble("charge_kwh"));
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(consume);
								dtoReport.setTime_zone("DAY");
								domestic.add(dtoReport);
								
								// System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 1");
								
							}else if(_charging_start_type == 2 && _charging_end_type ==  2) { //{2}
								
								long diff_1_tariff = Duration.between(charging_start_time,charging_end_time).toMinutes();
								double electricity = rs.getDouble("charge_kwh")*peak_price;
								double consume = rs.getDouble("total_consumption_amount");
								double estimate_profit = consume-electricity;
								GeneralPurposeRateDTO dtoReport = new GeneralPurposeRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(rs.getDouble("charge_kwh"));
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(consume);
								dtoReport.setTime_zone("PEAK");
								domestic.add(dtoReport);
								
								// System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 2");
								
							}else if(_charging_start_type == 3 && _charging_end_type ==  3) { //{3}
								
								long diff_1_tariff = Duration.between(charging_start_time,charging_end_time).toMinutes();
								double electricity = rs.getDouble("charge_kwh")*off_peak_price;
								double consume = rs.getDouble("total_consumption_amount");
								double estimate_profit = consume-electricity;
								GeneralPurposeRateDTO dtoReport = new GeneralPurposeRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(rs.getDouble("charge_kwh"));
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(consume);
								dtoReport.setTime_zone("OFF PEAK");
								domestic.add(dtoReport);
								
							//	System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 3");
								
							}else if(_charging_start_type == 1 && _charging_end_type ==  2) { //{1,2}
								
								long diffe = end_timestamp.getTime()-start_timestamp.getTime();
								System.out.println(diffe);
								long total_minutes = diffe/(1000*60);
								System.out.println(total_minutes);
								
								long diff_1_tariff = Duration.between(charging_start_time,day_end_tariff).toMinutes();
								long diff_2_tariff = Duration.between(peak_time_start, charging_end_time).toMinutes();
								
								System.out.println(diff_1_tariff);
								System.out.println(diff_2_tariff);
								// Calculation Process
								System.out.println(charge_kwh);
								double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
								double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
								
								System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);
								System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
								
								double consume = rs.getDouble("total_consumption_amount");
								System.out.println("total_consume "+consume);
								double _session_1_price = (consume/total_minutes)*diff_1_tariff;
								double _session_2_price = (consume/total_minutes)*diff_2_tariff;
								
								System.out.println("1 Session Price "+_session_1_price);
								System.out.println("2 Session Price "+_session_2_price);
								
								// price 
								double electricity = kwh_1*day_price;
								
								
								double estimate_profit_session_1 = _session_1_price-electricity;
								GeneralPurposeRateDTO dtoReport = new GeneralPurposeRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(kwh_1);
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit_session_1);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(_session_1_price);
								dtoReport.setTime_zone("DAY");
								domestic.add(dtoReport);
								
								double electricityn = kwh_2*peak_price;
								
								double estimate_profitn = _session_2_price-electricityn;
								GeneralPurposeRateDTO dtoReport1 = new GeneralPurposeRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_2_tariff);
								dtoReport1.setCharge_kWh(kwh_2);
								dtoReport1.setElectricity_charge(electricityn);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profitn);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_2_price);
								dtoReport1.setTime_zone("PEAK");
								domestic.add(dtoReport1);
								
								
								System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 4");
															
							}else if(_charging_start_type == 2 && _charging_end_type ==  3) { //{2,3}
								
								long diffe = end_timestamp.getTime()-start_timestamp.getTime();
								System.out.println(diffe);
								long total_minutes = diffe/(1000*60);
								System.out.println(total_minutes);
								
								long diff_1_tariff = Duration.between(charging_start_time,peak_time_end).toMinutes();
								long diff_2_tariff = Duration.between(off_peak_time_start, charging_end_time).toMinutes();
								
								System.out.println(diff_1_tariff);
								System.out.println(diff_2_tariff);
								// Calculation Process
								System.out.println(charge_kwh);
								double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
								double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
								
								System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);
								System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
								
								double consume = rs.getDouble("total_consumption_amount");
								System.out.println("total_consume "+consume);
								double _session_1_price = (consume/total_minutes)*diff_1_tariff;
								double _session_2_price = (consume/total_minutes)*diff_2_tariff;
								
								System.out.println("1 Session Price "+_session_1_price);
								System.out.println("2 Session Price "+_session_2_price);
								
								// price 
								double electricity = kwh_1*day_price;
								
								
								double estimate_profit_session_1 = _session_1_price-electricity;
								GeneralPurposeRateDTO dtoReport = new GeneralPurposeRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(kwh_1);
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit_session_1);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(_session_1_price);
								dtoReport.setTime_zone("DAY");
								domestic.add(dtoReport);
								
								double electricityn = kwh_2*peak_price;
								
								double estimate_profitn = _session_2_price-electricityn;
								GeneralPurposeRateDTO dtoReport1 = new GeneralPurposeRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_2_tariff);
								dtoReport1.setCharge_kWh(kwh_2);
								dtoReport1.setElectricity_charge(electricityn);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profitn);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_2_price);
								dtoReport1.setTime_zone("PEAK");
								domestic.add(dtoReport1);
								
								System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 5");
								
							}else if(_charging_start_type == 3 && _charging_end_type ==  1) { //{3,1}
								
								long diffe = end_timestamp.getTime()-start_timestamp.getTime();
								System.out.println(diffe);
								long total_minutes = diffe/(1000*60);
								System.out.println(total_minutes);
								
								long diff_1_tariff = Duration.between(charging_start_time,off_peak_time_end).toMinutes();
								long diff_2_tariff = Duration.between(day_start_tariff, charging_end_time).toMinutes();
								
								System.out.println(diff_1_tariff);
								System.out.println(diff_2_tariff);
								// Calculation Process
								System.out.println(charge_kwh);
								double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
								double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
								
								System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);
								System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
								
								double consume = rs.getDouble("total_consumption_amount");
								System.out.println("total_consume "+consume);
								double _session_1_price = (consume/total_minutes)*diff_1_tariff;
								double _session_2_price = (consume/total_minutes)*diff_2_tariff;
								
								System.out.println("1 Session Price "+_session_1_price);
								System.out.println("2 Session Price "+_session_2_price);
								
								// price 
								double electricity = kwh_1*day_price;
								
								
								double estimate_profit_session_1 = _session_1_price-electricity;
								GeneralPurposeRateDTO dtoReport = new GeneralPurposeRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(kwh_1);
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit_session_1);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(_session_1_price);
								dtoReport.setTime_zone("DAY");
								domestic.add(dtoReport);
								
								double electricityn = kwh_2*peak_price;
								
								double estimate_profitn = _session_2_price-electricityn;
								GeneralPurposeRateDTO dtoReport1 = new GeneralPurposeRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_2_tariff);
								dtoReport1.setCharge_kWh(kwh_2);
								dtoReport1.setElectricity_charge(electricityn);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profitn);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_2_price);
								dtoReport1.setTime_zone("PEAK");
								domestic.add(dtoReport1);
								
								
								
								System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 6");
								
							}else if(_charging_start_type == 1 && _charging_end_type ==  3) { //{1,2,3}
								
								long diffe = end_timestamp.getTime()-start_timestamp.getTime();
								System.out.println(diffe);
								long total_minutes = diffe/(1000*60);
								System.out.println(total_minutes);
								
								long diff_1_tariff = Duration.between(charging_start_time,day_end_tariff).toMinutes();
								long diff_2_tariff = Duration.between(peak_time_start,peak_time_end).toMinutes();
								long diff_3_tariff = Duration.between(off_peak_time_start, charging_end_time).toMinutes();
								
								System.out.println(diff_1_tariff);
								System.out.println(diff_2_tariff);
								// Calculation Process
								System.out.println(charge_kwh);
								double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
								double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
								Double kwh_3 = (charge_kwh/total_minutes)*diff_3_tariff;
								
								System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);									
								System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
								System.out.println("Off peak "+off_peak_price+"-"+kwh_3*off_peak_price);
								
								double consume = rs.getDouble("total_consumption_amount");
								System.out.println("total_consume "+consume);
								double _session_1_price = (consume/total_minutes)*diff_1_tariff;
								double _session_2_price = (consume/total_minutes)*diff_2_tariff;
								double _session_3_price = (consume/total_minutes)*diff_2_tariff;
								System.out.println("1 Session Price "+_session_1_price);
								System.out.println("2 Session Price "+_session_2_price);
								System.out.println("3 Session Price "+_session_3_price);
								// price 
								double electricity = kwh_1*day_price;
								
								
								double estimate_profit_session_1 = _session_1_price-electricity;
								GeneralPurposeRateDTO dtoReport = new GeneralPurposeRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(kwh_1);
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit_session_1);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(_session_1_price);
								dtoReport.setTime_zone("DAY");
								domestic.add(dtoReport);
								
								double electricityn = kwh_2*peak_price;
								
								double estimate_profitn = _session_2_price-electricityn;
								GeneralPurposeRateDTO dtoReport1 = new GeneralPurposeRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_2_tariff);
								dtoReport1.setCharge_kWh(kwh_2);
								dtoReport1.setElectricity_charge(electricityn);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profitn);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_2_price);
								dtoReport1.setTime_zone("PEAK");
								domestic.add(dtoReport1);
								
								double electricity_session_3 = kwh_3*off_peak_price;
								double estimate_profit_sessin_3 = _session_3_price-electricity_session_3;
								GeneralPurposeRateDTO dtoReport2 = new GeneralPurposeRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_3_tariff);
								dtoReport1.setCharge_kWh(kwh_3);
								dtoReport1.setElectricity_charge(electricity_session_3);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profit_sessin_3);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profit_sessin_3);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_3_price);
								dtoReport1.setTime_zone("OFF PEAK");
								domestic.add(dtoReport2);
								
								
								System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 7");
								
							}else if(_charging_start_type == 3 && _charging_end_type ==  2) { //{3,1,2}
								
								long diffe = end_timestamp.getTime()-start_timestamp.getTime();
								System.out.println(diffe);
								long total_minutes = diffe/(1000*60);
								System.out.println(total_minutes);
								
								long diff_1_tariff = Duration.between(charging_start_time,off_peak_time_end).toMinutes();
								long diff_2_tariff = Duration.between(day_start_tariff,day_start_tariff).toMinutes();
								long diff_3_tariff = Duration.between(peak_time_start, charging_end_time).toMinutes();
								
								System.out.println(diff_1_tariff);
								System.out.println(diff_2_tariff);
								// Calculation Process
								System.out.println(charge_kwh);
								double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
								double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
								Double kwh_3 = (charge_kwh/total_minutes)*diff_3_tariff;
								
								
								double consume = rs.getDouble("total_consumption_amount");
								System.out.println("total_consume "+consume);
								double _session_1_price = (consume/total_minutes)*diff_1_tariff;
								double _session_2_price = (consume/total_minutes)*diff_2_tariff;
								double _session_3_price = (consume/total_minutes)*diff_2_tariff;
								System.out.println("1 Session Price "+_session_1_price);
								System.out.println("2 Session Price "+_session_2_price);
								System.out.println("3 Session Price "+_session_3_price);
								// price 
								double electricity = kwh_1*off_peak_price;
								
								
								double estimate_profit_session_1 = _session_1_price-electricity;
								GeneralPurposeRateDTO dtoReport = new GeneralPurposeRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(kwh_1);
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit_session_1);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(_session_1_price);
								dtoReport.setTime_zone("OFF PEAK");
								domestic.add(dtoReport);
								
								double electricityn = kwh_2*day_price;
								
								double estimate_profitn = _session_2_price-electricityn;
								GeneralPurposeRateDTO dtoReport1 = new GeneralPurposeRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_2_tariff);
								dtoReport1.setCharge_kWh(kwh_2);
								dtoReport1.setElectricity_charge(electricityn);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profitn);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_2_price);
								dtoReport1.setTime_zone("DAY");
								domestic.add(dtoReport1);
								
								double electricity_session_3 = kwh_3*peak_price;
								double estimate_profit_sessin_3 = _session_3_price-electricity_session_3;
								GeneralPurposeRateDTO dtoReport2 = new GeneralPurposeRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_3_tariff);
								dtoReport1.setCharge_kWh(kwh_3);
								dtoReport1.setElectricity_charge(electricity_session_3);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profit_sessin_3);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profit_sessin_3);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_3_price);
								dtoReport1.setTime_zone("PEAK");
								domestic.add(dtoReport2);
								
								
								System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 8");
								
							}else if(_charging_start_type == 2 && _charging_end_type ==  1) { //{2,3,1}
								
								long diffe = end_timestamp.getTime()-start_timestamp.getTime();
								System.out.println(diffe);
								long total_minutes = diffe/(1000*60);
								System.out.println(total_minutes);
								
								long diff_1_tariff = Duration.between(charging_start_time,peak_time_end).toMinutes();
								long diff_2_tariff = Duration.between(off_peak_time_start,off_peak_time_end).toMinutes();
								long diff_3_tariff = Duration.between(day_start_tariff, charging_end_time).toMinutes();
								
								System.out.println(diff_1_tariff);
								System.out.println(diff_2_tariff);
								// Calculation Process
								System.out.println(charge_kwh);
								double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
								double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
								Double kwh_3 = (charge_kwh/total_minutes)*diff_3_tariff;
								
								
								double consume = rs.getDouble("total_consumption_amount");
								System.out.println("total_consume "+consume);
								double _session_1_price = (consume/total_minutes)*diff_1_tariff;
								double _session_2_price = (consume/total_minutes)*diff_2_tariff;
								double _session_3_price = (consume/total_minutes)*diff_2_tariff;
								System.out.println("1 Session Price "+_session_1_price);
								System.out.println("2 Session Price "+_session_2_price);
								System.out.println("3 Session Price "+_session_3_price);
								// price 
								double electricity = kwh_1*peak_price;
								
								
								double estimate_profit_session_1 = _session_1_price-electricity;
								GeneralPurposeRateDTO dtoReport = new GeneralPurposeRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(kwh_1);
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit_session_1);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(_session_1_price);
								dtoReport.setTime_zone("PEAK");
								domestic.add(dtoReport);
								
								double electricityn = kwh_2*off_peak_price;
								
								double estimate_profitn = _session_2_price-electricityn;
								GeneralPurposeRateDTO dtoReport1 = new GeneralPurposeRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_2_tariff);
								dtoReport1.setCharge_kWh(kwh_2);
								dtoReport1.setElectricity_charge(electricityn);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profitn);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_2_price);
								dtoReport1.setTime_zone("OFF PEAK");
								domestic.add(dtoReport1);
								
								double electricity_session_3 = kwh_3*day_price;
								double estimate_profit_sessin_3 = _session_3_price-electricity_session_3;
								GeneralPurposeRateDTO dtoReport2 = new GeneralPurposeRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_3_tariff);
								dtoReport1.setCharge_kWh(kwh_3);
								dtoReport1.setElectricity_charge(electricity_session_3);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profit_sessin_3);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profit_sessin_3);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_3_price);
								dtoReport1.setTime_zone("DAY");
								domestic.add(dtoReport2);
								
								System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 9");
							}
																						
					}
					
			}
		
			}
	
	}catch(Exception e) {
			e.printStackTrace();
	}
	return domestic;
}

public List<GeneralPurposeRateDTO> getChargerGeneralPurposeRateII(String station,String start_date,String end_date,int category,int subcategory,String gun_type){
	
	String sql="SELECT * FROM charge_order_information WHERE start_charging_time >= ? AND end_charging_time <= ? AND station_name = ?";
	List<GeneralPurposeRateDTO> domestic = new ArrayList<>();
	
	double price_per_unit  = 0.0;
	
	try {
		Connection con = DBConnection.getConnection();
		PreparedStatement ps = con.prepareStatement(sql);			
			ps.setString(1, start_date);
			ps.setString(2, end_date);
			ps.setString(3, station);		
			ResultSet rs = ps.executeQuery();	
			
			
			double charge_kwh = 0.0;
			double day_price = 0.0;
			double peak_price = 0.0;
			double off_peak_price = 0.0;
			double fixed_charge = 0.0;
			double demand_charge = 0.0;
			
	
	String  sqlcategory = "SELECT * FROM other_consumer WHERE type = 10";
						PreparedStatement psn = con.prepareStatement(sqlcategory);
						ResultSet rsn = psn.executeQuery();
						//System.out.println(rsn.get);
						
						while(rsn.next()) {
							
							day_price = rsn.getDouble("day_price");
							peak_price = rsn.getDouble("peak_price");
							off_peak_price = rsn.getDouble("off_peak_price");
							fixed_charge =  rsn.getDouble("fixed_charge");
							demand_charge =  rsn.getDouble("demand_charge");
						}
						
						while(rs.next()) {
							//check Day
							Util util = new Util();
							charge_kwh = rs.getDouble("charge_kwh");
							//charging start time
							Timestamp start_timestamp = rs.getTimestamp("start_charging_time");
							LocalTime charging_start_time = start_timestamp.toLocalDateTime().toLocalTime();
							
							//charging end time 
							Timestamp end_timestamp = rs.getTimestamp("end_charging_time");
							LocalTime charging_end_time = end_timestamp.toLocalDateTime().toLocalTime();
							
							LocalTime day_start_tariff = LocalTime.parse(util.DAY_TIME_START);
							LocalTime day_end_tariff = LocalTime.parse(util.DAY_TIME_END);
							
							LocalTime peak_time_start = LocalTime.parse(util.PEAK_TIME_START);
							LocalTime peak_time_end = LocalTime.parse(util.PEAK_TIME_END);
							
							LocalTime off_peak_time_start = LocalTime.parse(util.OFF_PEAK_TIME_START);
							LocalTime off_peak_time_end = LocalTime.parse(util.OFF_PEAK_TIME_END);
							
							// FIRST CHECK START AND END TIME IN WHICH TARIFF
							
							int _charging_start_type = 0; // 1 = DAY, 2 = PEAK , 3 = OFF PEAK 
							int _charging_end_type = 0; // 1 = DAY, 2 = PEAK , 3 = OFF PEAK 
							
							if(charging_start_time.isAfter(day_start_tariff) && charging_start_time.isBefore(peak_time_start)) { // DAY
								
								_charging_start_type = 1;
																	
							}else if(charging_start_time.isAfter(peak_time_start) && charging_start_time.isBefore(off_peak_time_start)) { // PEAKE
								
								_charging_start_type = 2;
								
							}else if(charging_start_time.isAfter(off_peak_time_start) && charging_start_time.isBefore(day_start_tariff)) { // OFF PEAKE
								
								_charging_start_type = 3;
								
							}
							
							
							if(charging_end_time.isAfter(day_start_tariff) && charging_end_time.isBefore(peak_time_start)) { // DAY
								
								_charging_end_type = 1;
																	
							}else if(charging_end_time.isAfter(peak_time_start) && charging_end_time.isBefore(off_peak_time_start)) { // PEAKE
								
								_charging_end_type = 2;
								
							}else if(charging_end_time.isAfter(off_peak_time_start) && charging_end_time.isBefore(day_start_tariff)) { // OFF PEAKE
								
								_charging_end_type = 3;
								
							}
							
							
							//THE CYCLE PROCESS 
							
							if(_charging_start_type == 1 && _charging_end_type ==  1) { //{1}
																								
								long diff_1_tariff = Duration.between(charging_start_time,charging_end_time).toMinutes();
								double electricity = rs.getDouble("charge_kwh")*day_price;
								double consume = rs.getDouble("total_consumption_amount");
								double estimate_profit = consume-electricity;
								GeneralPurposeRateDTO dtoReport = new GeneralPurposeRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(rs.getDouble("charge_kwh"));
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(consume);
								dtoReport.setTime_zone("DAY");
								domestic.add(dtoReport);
								
								// System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 1");
								
							}else if(_charging_start_type == 2 && _charging_end_type ==  2) { //{2}
								
								long diff_1_tariff = Duration.between(charging_start_time,charging_end_time).toMinutes();
								double electricity = rs.getDouble("charge_kwh")*peak_price;
								double consume = rs.getDouble("total_consumption_amount");
								double estimate_profit = consume-electricity;
								GeneralPurposeRateDTO dtoReport = new GeneralPurposeRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(rs.getDouble("charge_kwh"));
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(consume);
								dtoReport.setTime_zone("PEAK");
								domestic.add(dtoReport);
								
								// System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 2");
								
							}else if(_charging_start_type == 3 && _charging_end_type ==  3) { //{3}
								
								long diff_1_tariff = Duration.between(charging_start_time,charging_end_time).toMinutes();
								double electricity = rs.getDouble("charge_kwh")*off_peak_price;
								double consume = rs.getDouble("total_consumption_amount");
								double estimate_profit = consume-electricity;
								GeneralPurposeRateDTO dtoReport = new GeneralPurposeRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(rs.getDouble("charge_kwh"));
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(consume);
								dtoReport.setTime_zone("OFF PEAK");
								domestic.add(dtoReport);
								
							//	System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 3");
								
							}else if(_charging_start_type == 1 && _charging_end_type ==  2) { //{1,2}
								
								long diffe = end_timestamp.getTime()-start_timestamp.getTime();
								System.out.println(diffe);
								long total_minutes = diffe/(1000*60);
								System.out.println(total_minutes);
								
								long diff_1_tariff = Duration.between(charging_start_time,day_end_tariff).toMinutes();
								long diff_2_tariff = Duration.between(peak_time_start, charging_end_time).toMinutes();
								
								System.out.println(diff_1_tariff);
								System.out.println(diff_2_tariff);
								// Calculation Process
								System.out.println(charge_kwh);
								double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
								double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
								
								System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);
								System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
								
								double consume = rs.getDouble("total_consumption_amount");
								System.out.println("total_consume "+consume);
								double _session_1_price = (consume/total_minutes)*diff_1_tariff;
								double _session_2_price = (consume/total_minutes)*diff_2_tariff;
								
								System.out.println("1 Session Price "+_session_1_price);
								System.out.println("2 Session Price "+_session_2_price);
								
								// price 
								double electricity = kwh_1*day_price;
								
								
								double estimate_profit_session_1 = _session_1_price-electricity;
								GeneralPurposeRateDTO dtoReport = new GeneralPurposeRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(kwh_1);
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit_session_1);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(_session_1_price);
								dtoReport.setTime_zone("DAY");
								domestic.add(dtoReport);
								
								double electricityn = kwh_2*peak_price;
								
								double estimate_profitn = _session_2_price-electricityn;
								GeneralPurposeRateDTO dtoReport1 = new GeneralPurposeRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_2_tariff);
								dtoReport1.setCharge_kWh(kwh_2);
								dtoReport1.setElectricity_charge(electricityn);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profitn);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_2_price);
								dtoReport1.setTime_zone("PEAK");
								domestic.add(dtoReport1);
								
								
								System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 4");
															
							}else if(_charging_start_type == 2 && _charging_end_type ==  3) { //{2,3}
								
								long diffe = end_timestamp.getTime()-start_timestamp.getTime();
								System.out.println(diffe);
								long total_minutes = diffe/(1000*60);
								System.out.println(total_minutes);
								
								long diff_1_tariff = Duration.between(charging_start_time,peak_time_end).toMinutes();
								long diff_2_tariff = Duration.between(off_peak_time_start, charging_end_time).toMinutes();
								
								System.out.println(diff_1_tariff);
								System.out.println(diff_2_tariff);
								// Calculation Process
								System.out.println(charge_kwh);
								double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
								double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
								
								System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);
								System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
								
								double consume = rs.getDouble("total_consumption_amount");
								System.out.println("total_consume "+consume);
								double _session_1_price = (consume/total_minutes)*diff_1_tariff;
								double _session_2_price = (consume/total_minutes)*diff_2_tariff;
								
								System.out.println("1 Session Price "+_session_1_price);
								System.out.println("2 Session Price "+_session_2_price);
								
								// price 
								double electricity = kwh_1*day_price;
								
								
								double estimate_profit_session_1 = _session_1_price-electricity;
								GeneralPurposeRateDTO dtoReport = new GeneralPurposeRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(kwh_1);
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit_session_1);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(_session_1_price);
								dtoReport.setTime_zone("DAY");
								domestic.add(dtoReport);
								
								double electricityn = kwh_2*peak_price;
								
								double estimate_profitn = _session_2_price-electricityn;
								GeneralPurposeRateDTO dtoReport1 = new GeneralPurposeRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_2_tariff);
								dtoReport1.setCharge_kWh(kwh_2);
								dtoReport1.setElectricity_charge(electricityn);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profitn);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_2_price);
								dtoReport1.setTime_zone("PEAK");
								domestic.add(dtoReport1);
								
								System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 5");
								
							}else if(_charging_start_type == 3 && _charging_end_type ==  1) { //{3,1}
								
								long diffe = end_timestamp.getTime()-start_timestamp.getTime();
								System.out.println(diffe);
								long total_minutes = diffe/(1000*60);
								System.out.println(total_minutes);
								
								long diff_1_tariff = Duration.between(charging_start_time,off_peak_time_end).toMinutes();
								long diff_2_tariff = Duration.between(day_start_tariff, charging_end_time).toMinutes();
								
								System.out.println(diff_1_tariff);
								System.out.println(diff_2_tariff);
								// Calculation Process
								System.out.println(charge_kwh);
								double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
								double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
								
								System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);
								System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
								
								double consume = rs.getDouble("total_consumption_amount");
								System.out.println("total_consume "+consume);
								double _session_1_price = (consume/total_minutes)*diff_1_tariff;
								double _session_2_price = (consume/total_minutes)*diff_2_tariff;
								
								System.out.println("1 Session Price "+_session_1_price);
								System.out.println("2 Session Price "+_session_2_price);
								
								// price 
								double electricity = kwh_1*day_price;
								
								
								double estimate_profit_session_1 = _session_1_price-electricity;
								GeneralPurposeRateDTO dtoReport = new GeneralPurposeRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(kwh_1);
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit_session_1);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(_session_1_price);
								dtoReport.setTime_zone("DAY");
								domestic.add(dtoReport);
								
								double electricityn = kwh_2*peak_price;
								
								double estimate_profitn = _session_2_price-electricityn;
								GeneralPurposeRateDTO dtoReport1 = new GeneralPurposeRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_2_tariff);
								dtoReport1.setCharge_kWh(kwh_2);
								dtoReport1.setElectricity_charge(electricityn);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profitn);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_2_price);
								dtoReport1.setTime_zone("PEAK");
								domestic.add(dtoReport1);
								
								
								
								System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 6");
								
							}else if(_charging_start_type == 1 && _charging_end_type ==  3) { //{1,2,3}
								
								long diffe = end_timestamp.getTime()-start_timestamp.getTime();
								System.out.println(diffe);
								long total_minutes = diffe/(1000*60);
								System.out.println(total_minutes);
								
								long diff_1_tariff = Duration.between(charging_start_time,day_end_tariff).toMinutes();
								long diff_2_tariff = Duration.between(peak_time_start,peak_time_end).toMinutes();
								long diff_3_tariff = Duration.between(off_peak_time_start, charging_end_time).toMinutes();
								
								System.out.println(diff_1_tariff);
								System.out.println(diff_2_tariff);
								// Calculation Process
								System.out.println(charge_kwh);
								double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
								double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
								Double kwh_3 = (charge_kwh/total_minutes)*diff_3_tariff;
								
								System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);									
								System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
								System.out.println("Off peak "+off_peak_price+"-"+kwh_3*off_peak_price);
								
								double consume = rs.getDouble("total_consumption_amount");
								System.out.println("total_consume "+consume);
								double _session_1_price = (consume/total_minutes)*diff_1_tariff;
								double _session_2_price = (consume/total_minutes)*diff_2_tariff;
								double _session_3_price = (consume/total_minutes)*diff_2_tariff;
								System.out.println("1 Session Price "+_session_1_price);
								System.out.println("2 Session Price "+_session_2_price);
								System.out.println("3 Session Price "+_session_3_price);
								// price 
								double electricity = kwh_1*day_price;
								
								
								double estimate_profit_session_1 = _session_1_price-electricity;
								GeneralPurposeRateDTO dtoReport = new GeneralPurposeRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(kwh_1);
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit_session_1);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(_session_1_price);
								dtoReport.setTime_zone("DAY");
								domestic.add(dtoReport);
								
								double electricityn = kwh_2*peak_price;
								
								double estimate_profitn = _session_2_price-electricityn;
								GeneralPurposeRateDTO dtoReport1 = new GeneralPurposeRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_2_tariff);
								dtoReport1.setCharge_kWh(kwh_2);
								dtoReport1.setElectricity_charge(electricityn);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profitn);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_2_price);
								dtoReport1.setTime_zone("PEAK");
								domestic.add(dtoReport1);
								
								double electricity_session_3 = kwh_3*off_peak_price;
								double estimate_profit_sessin_3 = _session_3_price-electricity_session_3;
								GeneralPurposeRateDTO dtoReport2 = new GeneralPurposeRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_3_tariff);
								dtoReport1.setCharge_kWh(kwh_3);
								dtoReport1.setElectricity_charge(electricity_session_3);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profit_sessin_3);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profit_sessin_3);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_3_price);
								dtoReport1.setTime_zone("OFF PEAK");
								domestic.add(dtoReport2);
								
								
								System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 7");
								
							}else if(_charging_start_type == 3 && _charging_end_type ==  2) { //{3,1,2}
								
								long diffe = end_timestamp.getTime()-start_timestamp.getTime();
								System.out.println(diffe);
								long total_minutes = diffe/(1000*60);
								System.out.println(total_minutes);
								
								long diff_1_tariff = Duration.between(charging_start_time,off_peak_time_end).toMinutes();
								long diff_2_tariff = Duration.between(day_start_tariff,day_start_tariff).toMinutes();
								long diff_3_tariff = Duration.between(peak_time_start, charging_end_time).toMinutes();
								
								System.out.println(diff_1_tariff);
								System.out.println(diff_2_tariff);
								// Calculation Process
								System.out.println(charge_kwh);
								double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
								double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
								Double kwh_3 = (charge_kwh/total_minutes)*diff_3_tariff;
								
								
								double consume = rs.getDouble("total_consumption_amount");
								System.out.println("total_consume "+consume);
								double _session_1_price = (consume/total_minutes)*diff_1_tariff;
								double _session_2_price = (consume/total_minutes)*diff_2_tariff;
								double _session_3_price = (consume/total_minutes)*diff_2_tariff;
								System.out.println("1 Session Price "+_session_1_price);
								System.out.println("2 Session Price "+_session_2_price);
								System.out.println("3 Session Price "+_session_3_price);
								// price 
								double electricity = kwh_1*off_peak_price;
								
								
								double estimate_profit_session_1 = _session_1_price-electricity;
								GeneralPurposeRateDTO dtoReport = new GeneralPurposeRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(kwh_1);
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit_session_1);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(_session_1_price);
								dtoReport.setTime_zone("OFF PEAK");
								domestic.add(dtoReport);
								
								double electricityn = kwh_2*day_price;
								
								double estimate_profitn = _session_2_price-electricityn;
								GeneralPurposeRateDTO dtoReport1 = new GeneralPurposeRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_2_tariff);
								dtoReport1.setCharge_kWh(kwh_2);
								dtoReport1.setElectricity_charge(electricityn);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profitn);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_2_price);
								dtoReport1.setTime_zone("DAY");
								domestic.add(dtoReport1);
								
								double electricity_session_3 = kwh_3*peak_price;
								double estimate_profit_sessin_3 = _session_3_price-electricity_session_3;
								GeneralPurposeRateDTO dtoReport2 = new GeneralPurposeRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_3_tariff);
								dtoReport1.setCharge_kWh(kwh_3);
								dtoReport1.setElectricity_charge(electricity_session_3);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profit_sessin_3);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profit_sessin_3);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_3_price);
								dtoReport1.setTime_zone("PEAK");
								domestic.add(dtoReport2);
								
								
								System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 8");
								
							}else if(_charging_start_type == 2 && _charging_end_type ==  1) { //{2,3,1}
								
								long diffe = end_timestamp.getTime()-start_timestamp.getTime();
								System.out.println(diffe);
								long total_minutes = diffe/(1000*60);
								System.out.println(total_minutes);
								
								long diff_1_tariff = Duration.between(charging_start_time,peak_time_end).toMinutes();
								long diff_2_tariff = Duration.between(off_peak_time_start,off_peak_time_end).toMinutes();
								long diff_3_tariff = Duration.between(day_start_tariff, charging_end_time).toMinutes();
								
								System.out.println(diff_1_tariff);
								System.out.println(diff_2_tariff);
								// Calculation Process
								System.out.println(charge_kwh);
								double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
								double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
								Double kwh_3 = (charge_kwh/total_minutes)*diff_3_tariff;
								
								
								double consume = rs.getDouble("total_consumption_amount");
								System.out.println("total_consume "+consume);
								double _session_1_price = (consume/total_minutes)*diff_1_tariff;
								double _session_2_price = (consume/total_minutes)*diff_2_tariff;
								double _session_3_price = (consume/total_minutes)*diff_2_tariff;
								System.out.println("1 Session Price "+_session_1_price);
								System.out.println("2 Session Price "+_session_2_price);
								System.out.println("3 Session Price "+_session_3_price);
								// price 
								double electricity = kwh_1*peak_price;
								
								
								double estimate_profit_session_1 = _session_1_price-electricity;
								GeneralPurposeRateDTO dtoReport = new GeneralPurposeRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(kwh_1);
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit_session_1);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(_session_1_price);
								dtoReport.setTime_zone("PEAK");
								domestic.add(dtoReport);
								
								double electricityn = kwh_2*off_peak_price;
								
								double estimate_profitn = _session_2_price-electricityn;
								GeneralPurposeRateDTO dtoReport1 = new GeneralPurposeRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_2_tariff);
								dtoReport1.setCharge_kWh(kwh_2);
								dtoReport1.setElectricity_charge(electricityn);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profitn);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_2_price);
								dtoReport1.setTime_zone("OFF PEAK");
								domestic.add(dtoReport1);
								
								double electricity_session_3 = kwh_3*day_price;
								double estimate_profit_sessin_3 = _session_3_price-electricity_session_3;
								GeneralPurposeRateDTO dtoReport2 = new GeneralPurposeRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_3_tariff);
								dtoReport1.setCharge_kWh(kwh_3);
								dtoReport1.setElectricity_charge(electricity_session_3);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profit_sessin_3);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profit_sessin_3);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_3_price);
								dtoReport1.setTime_zone("DAY");
								domestic.add(dtoReport2);
								
								System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 9");
							}
																						
					}
					
			
		
			
	
	}catch(Exception e) {
			e.printStackTrace();
	}
	return domestic;
}

public List<GeneralPurposeRateDTO> getChargerGeneralPurposeRateIII(String station,String start_date,String end_date,int category,int subcategory,String gun_type){
	
	String sql="SELECT * FROM charge_order_information WHERE start_charging_time >= ? AND end_charging_time <= ? AND station_name = ?";
	List<GeneralPurposeRateDTO> domestic = new ArrayList<>();
	
	double price_per_unit  = 0.0;
	
	try {
		Connection con = DBConnection.getConnection();
		PreparedStatement ps = con.prepareStatement(sql);			
			ps.setString(1, start_date);
			ps.setString(2, end_date);
			ps.setString(3, station);		
			ResultSet rs = ps.executeQuery();	
			
			
			double charge_kwh = 0.0;
			double day_price = 0.0;
			double peak_price = 0.0;
			double off_peak_price = 0.0;
			double fixed_charge = 0.0;
			double demand_charge = 0.0;
			
	
	String  sqlcategory = "SELECT * FROM other_consumer WHERE type = 11";
						PreparedStatement psn = con.prepareStatement(sqlcategory);
						ResultSet rsn = psn.executeQuery();
						//System.out.println(rsn.get);
						
						while(rsn.next()) {
							
							day_price = rsn.getDouble("day_price");
							peak_price = rsn.getDouble("peak_price");
							off_peak_price = rsn.getDouble("off_peak_price");
							fixed_charge =  rsn.getDouble("fixed_charge");
							demand_charge =  rsn.getDouble("demand_charge");
						}
						
						while(rs.next()) {
							//check Day
							Util util = new Util();
							charge_kwh = rs.getDouble("charge_kwh");
							//charging start time
							Timestamp start_timestamp = rs.getTimestamp("start_charging_time");
							LocalTime charging_start_time = start_timestamp.toLocalDateTime().toLocalTime();
							
							//charging end time 
							Timestamp end_timestamp = rs.getTimestamp("end_charging_time");
							LocalTime charging_end_time = end_timestamp.toLocalDateTime().toLocalTime();
							
							LocalTime day_start_tariff = LocalTime.parse(util.DAY_TIME_START);
							LocalTime day_end_tariff = LocalTime.parse(util.DAY_TIME_END);
							
							LocalTime peak_time_start = LocalTime.parse(util.PEAK_TIME_START);
							LocalTime peak_time_end = LocalTime.parse(util.PEAK_TIME_END);
							
							LocalTime off_peak_time_start = LocalTime.parse(util.OFF_PEAK_TIME_START);
							LocalTime off_peak_time_end = LocalTime.parse(util.OFF_PEAK_TIME_END);
							
							// FIRST CHECK START AND END TIME IN WHICH TARIFF
							
							int _charging_start_type = 0; // 1 = DAY, 2 = PEAK , 3 = OFF PEAK 
							int _charging_end_type = 0; // 1 = DAY, 2 = PEAK , 3 = OFF PEAK 
							
							if(charging_start_time.isAfter(day_start_tariff) && charging_start_time.isBefore(peak_time_start)) { // DAY
								
								_charging_start_type = 1;
																	
							}else if(charging_start_time.isAfter(peak_time_start) && charging_start_time.isBefore(off_peak_time_start)) { // PEAKE
								
								_charging_start_type = 2;
								
							}else if(charging_start_time.isAfter(off_peak_time_start) && charging_start_time.isBefore(day_start_tariff)) { // OFF PEAKE
								
								_charging_start_type = 3;
								
							}
							
							
							if(charging_end_time.isAfter(day_start_tariff) && charging_end_time.isBefore(peak_time_start)) { // DAY
								
								_charging_end_type = 1;
																	
							}else if(charging_end_time.isAfter(peak_time_start) && charging_end_time.isBefore(off_peak_time_start)) { // PEAKE
								
								_charging_end_type = 2;
								
							}else if(charging_end_time.isAfter(off_peak_time_start) && charging_end_time.isBefore(day_start_tariff)) { // OFF PEAKE
								
								_charging_end_type = 3;
								
							}
							
							
							//THE CYCLE PROCESS 
							
							if(_charging_start_type == 1 && _charging_end_type ==  1) { //{1}
																								
								long diff_1_tariff = Duration.between(charging_start_time,charging_end_time).toMinutes();
								double electricity = rs.getDouble("charge_kwh")*day_price;
								double consume = rs.getDouble("total_consumption_amount");
								double estimate_profit = consume-electricity;
								GeneralPurposeRateDTO dtoReport = new GeneralPurposeRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(rs.getDouble("charge_kwh"));
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(consume);
								dtoReport.setTime_zone("DAY");
								domestic.add(dtoReport);
								
								// System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 1");
								
							}else if(_charging_start_type == 2 && _charging_end_type ==  2) { //{2}
								
								long diff_1_tariff = Duration.between(charging_start_time,charging_end_time).toMinutes();
								double electricity = rs.getDouble("charge_kwh")*peak_price;
								double consume = rs.getDouble("total_consumption_amount");
								double estimate_profit = consume-electricity;
								GeneralPurposeRateDTO dtoReport = new GeneralPurposeRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(rs.getDouble("charge_kwh"));
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(consume);
								dtoReport.setTime_zone("PEAK");
								domestic.add(dtoReport);
								
								// System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 2");
								
							}else if(_charging_start_type == 3 && _charging_end_type ==  3) { //{3}
								
								long diff_1_tariff = Duration.between(charging_start_time,charging_end_time).toMinutes();
								double electricity = rs.getDouble("charge_kwh")*off_peak_price;
								double consume = rs.getDouble("total_consumption_amount");
								double estimate_profit = consume-electricity;
								GeneralPurposeRateDTO dtoReport = new GeneralPurposeRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(rs.getDouble("charge_kwh"));
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(consume);
								dtoReport.setTime_zone("OFF PEAK");
								domestic.add(dtoReport);
								
							//	System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 3");
								
							}else if(_charging_start_type == 1 && _charging_end_type ==  2) { //{1,2}
								
								long diffe = end_timestamp.getTime()-start_timestamp.getTime();
								System.out.println(diffe);
								long total_minutes = diffe/(1000*60);
								System.out.println(total_minutes);
								
								long diff_1_tariff = Duration.between(charging_start_time,day_end_tariff).toMinutes();
								long diff_2_tariff = Duration.between(peak_time_start, charging_end_time).toMinutes();
								
								System.out.println(diff_1_tariff);
								System.out.println(diff_2_tariff);
								// Calculation Process
								System.out.println(charge_kwh);
								double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
								double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
								
								System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);
								System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
								
								double consume = rs.getDouble("total_consumption_amount");
								System.out.println("total_consume "+consume);
								double _session_1_price = (consume/total_minutes)*diff_1_tariff;
								double _session_2_price = (consume/total_minutes)*diff_2_tariff;
								
								System.out.println("1 Session Price "+_session_1_price);
								System.out.println("2 Session Price "+_session_2_price);
								
								// price 
								double electricity = kwh_1*day_price;
								
								
								double estimate_profit_session_1 = _session_1_price-electricity;
								GeneralPurposeRateDTO dtoReport = new GeneralPurposeRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(kwh_1);
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit_session_1);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(_session_1_price);
								dtoReport.setTime_zone("DAY");
								domestic.add(dtoReport);
								
								double electricityn = kwh_2*peak_price;
								
								double estimate_profitn = _session_2_price-electricityn;
								GeneralPurposeRateDTO dtoReport1 = new GeneralPurposeRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_2_tariff);
								dtoReport1.setCharge_kWh(kwh_2);
								dtoReport1.setElectricity_charge(electricityn);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profitn);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_2_price);
								dtoReport1.setTime_zone("PEAK");
								domestic.add(dtoReport1);
								
								
								System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 4");
															
							}else if(_charging_start_type == 2 && _charging_end_type ==  3) { //{2,3}
								
								long diffe = end_timestamp.getTime()-start_timestamp.getTime();
								System.out.println(diffe);
								long total_minutes = diffe/(1000*60);
								System.out.println(total_minutes);
								
								long diff_1_tariff = Duration.between(charging_start_time,peak_time_end).toMinutes();
								long diff_2_tariff = Duration.between(off_peak_time_start, charging_end_time).toMinutes();
								
								System.out.println(diff_1_tariff);
								System.out.println(diff_2_tariff);
								// Calculation Process
								System.out.println(charge_kwh);
								double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
								double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
								
								System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);
								System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
								
								double consume = rs.getDouble("total_consumption_amount");
								System.out.println("total_consume "+consume);
								double _session_1_price = (consume/total_minutes)*diff_1_tariff;
								double _session_2_price = (consume/total_minutes)*diff_2_tariff;
								
								System.out.println("1 Session Price "+_session_1_price);
								System.out.println("2 Session Price "+_session_2_price);
								
								// price 
								double electricity = kwh_1*day_price;
								
								
								double estimate_profit_session_1 = _session_1_price-electricity;
								GeneralPurposeRateDTO dtoReport = new GeneralPurposeRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(kwh_1);
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit_session_1);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(_session_1_price);
								dtoReport.setTime_zone("DAY");
								domestic.add(dtoReport);
								
								double electricityn = kwh_2*peak_price;
								
								double estimate_profitn = _session_2_price-electricityn;
								GeneralPurposeRateDTO dtoReport1 = new GeneralPurposeRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_2_tariff);
								dtoReport1.setCharge_kWh(kwh_2);
								dtoReport1.setElectricity_charge(electricityn);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profitn);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_2_price);
								dtoReport1.setTime_zone("PEAK");
								domestic.add(dtoReport1);
								
								System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 5");
								
							}else if(_charging_start_type == 3 && _charging_end_type ==  1) { //{3,1}
								
								long diffe = end_timestamp.getTime()-start_timestamp.getTime();
								System.out.println(diffe);
								long total_minutes = diffe/(1000*60);
								System.out.println(total_minutes);
								
								long diff_1_tariff = Duration.between(charging_start_time,off_peak_time_end).toMinutes();
								long diff_2_tariff = Duration.between(day_start_tariff, charging_end_time).toMinutes();
								
								System.out.println(diff_1_tariff);
								System.out.println(diff_2_tariff);
								// Calculation Process
								System.out.println(charge_kwh);
								double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
								double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
								
								System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);
								System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
								
								double consume = rs.getDouble("total_consumption_amount");
								System.out.println("total_consume "+consume);
								double _session_1_price = (consume/total_minutes)*diff_1_tariff;
								double _session_2_price = (consume/total_minutes)*diff_2_tariff;
								
								System.out.println("1 Session Price "+_session_1_price);
								System.out.println("2 Session Price "+_session_2_price);
								
								// price 
								double electricity = kwh_1*day_price;
								
								
								double estimate_profit_session_1 = _session_1_price-electricity;
								GeneralPurposeRateDTO dtoReport = new GeneralPurposeRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(kwh_1);
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit_session_1);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(_session_1_price);
								dtoReport.setTime_zone("DAY");
								domestic.add(dtoReport);
								
								double electricityn = kwh_2*peak_price;
								
								double estimate_profitn = _session_2_price-electricityn;
								GeneralPurposeRateDTO dtoReport1 = new GeneralPurposeRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_2_tariff);
								dtoReport1.setCharge_kWh(kwh_2);
								dtoReport1.setElectricity_charge(electricityn);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profitn);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_2_price);
								dtoReport1.setTime_zone("PEAK");
								domestic.add(dtoReport1);
								
								
								
								System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 6");
								
							}else if(_charging_start_type == 1 && _charging_end_type ==  3) { //{1,2,3}
								
								long diffe = end_timestamp.getTime()-start_timestamp.getTime();
								System.out.println(diffe);
								long total_minutes = diffe/(1000*60);
								System.out.println(total_minutes);
								
								long diff_1_tariff = Duration.between(charging_start_time,day_end_tariff).toMinutes();
								long diff_2_tariff = Duration.between(peak_time_start,peak_time_end).toMinutes();
								long diff_3_tariff = Duration.between(off_peak_time_start, charging_end_time).toMinutes();
								
								System.out.println(diff_1_tariff);
								System.out.println(diff_2_tariff);
								// Calculation Process
								System.out.println(charge_kwh);
								double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
								double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
								Double kwh_3 = (charge_kwh/total_minutes)*diff_3_tariff;
								
								System.out.println("Day Price"+ day_price+"-"+kwh_1*day_price);									
								System.out.println("Peak Price"+peak_price+"-"+kwh_2*peak_price);
								System.out.println("Off peak "+off_peak_price+"-"+kwh_3*off_peak_price);
								
								double consume = rs.getDouble("total_consumption_amount");
								System.out.println("total_consume "+consume);
								double _session_1_price = (consume/total_minutes)*diff_1_tariff;
								double _session_2_price = (consume/total_minutes)*diff_2_tariff;
								double _session_3_price = (consume/total_minutes)*diff_2_tariff;
								System.out.println("1 Session Price "+_session_1_price);
								System.out.println("2 Session Price "+_session_2_price);
								System.out.println("3 Session Price "+_session_3_price);
								// price 
								double electricity = kwh_1*day_price;
								
								
								double estimate_profit_session_1 = _session_1_price-electricity;
								GeneralPurposeRateDTO dtoReport = new GeneralPurposeRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(kwh_1);
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit_session_1);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(_session_1_price);
								dtoReport.setTime_zone("DAY");
								domestic.add(dtoReport);
								
								double electricityn = kwh_2*peak_price;
								
								double estimate_profitn = _session_2_price-electricityn;
								GeneralPurposeRateDTO dtoReport1 = new GeneralPurposeRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_2_tariff);
								dtoReport1.setCharge_kWh(kwh_2);
								dtoReport1.setElectricity_charge(electricityn);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profitn);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_2_price);
								dtoReport1.setTime_zone("PEAK");
								domestic.add(dtoReport1);
								
								double electricity_session_3 = kwh_3*off_peak_price;
								double estimate_profit_sessin_3 = _session_3_price-electricity_session_3;
								GeneralPurposeRateDTO dtoReport2 = new GeneralPurposeRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_3_tariff);
								dtoReport1.setCharge_kWh(kwh_3);
								dtoReport1.setElectricity_charge(electricity_session_3);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profit_sessin_3);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profit_sessin_3);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_3_price);
								dtoReport1.setTime_zone("OFF PEAK");
								domestic.add(dtoReport2);
								
								
								System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 7");
								
							}else if(_charging_start_type == 3 && _charging_end_type ==  2) { //{3,1,2}
								
								long diffe = end_timestamp.getTime()-start_timestamp.getTime();
								System.out.println(diffe);
								long total_minutes = diffe/(1000*60);
								System.out.println(total_minutes);
								
								long diff_1_tariff = Duration.between(charging_start_time,off_peak_time_end).toMinutes();
								long diff_2_tariff = Duration.between(day_start_tariff,day_start_tariff).toMinutes();
								long diff_3_tariff = Duration.between(peak_time_start, charging_end_time).toMinutes();
								
								System.out.println(diff_1_tariff);
								System.out.println(diff_2_tariff);
								// Calculation Process
								System.out.println(charge_kwh);
								double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
								double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
								Double kwh_3 = (charge_kwh/total_minutes)*diff_3_tariff;
								
								
								double consume = rs.getDouble("total_consumption_amount");
								System.out.println("total_consume "+consume);
								double _session_1_price = (consume/total_minutes)*diff_1_tariff;
								double _session_2_price = (consume/total_minutes)*diff_2_tariff;
								double _session_3_price = (consume/total_minutes)*diff_2_tariff;
								System.out.println("1 Session Price "+_session_1_price);
								System.out.println("2 Session Price "+_session_2_price);
								System.out.println("3 Session Price "+_session_3_price);
								// price 
								double electricity = kwh_1*off_peak_price;
								
								
								double estimate_profit_session_1 = _session_1_price-electricity;
								GeneralPurposeRateDTO dtoReport = new GeneralPurposeRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(kwh_1);
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit_session_1);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(_session_1_price);
								dtoReport.setTime_zone("OFF PEAK");
								domestic.add(dtoReport);
								
								double electricityn = kwh_2*day_price;
								
								double estimate_profitn = _session_2_price-electricityn;
								GeneralPurposeRateDTO dtoReport1 = new GeneralPurposeRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_2_tariff);
								dtoReport1.setCharge_kWh(kwh_2);
								dtoReport1.setElectricity_charge(electricityn);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profitn);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_2_price);
								dtoReport1.setTime_zone("DAY");
								domestic.add(dtoReport1);
								
								double electricity_session_3 = kwh_3*peak_price;
								double estimate_profit_sessin_3 = _session_3_price-electricity_session_3;
								GeneralPurposeRateDTO dtoReport2 = new GeneralPurposeRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_3_tariff);
								dtoReport1.setCharge_kWh(kwh_3);
								dtoReport1.setElectricity_charge(electricity_session_3);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profit_sessin_3);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profit_sessin_3);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_3_price);
								dtoReport1.setTime_zone("PEAK");
								domestic.add(dtoReport2);
								
								
								System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 8");
								
							}else if(_charging_start_type == 2 && _charging_end_type ==  1) { //{2,3,1}
								
								long diffe = end_timestamp.getTime()-start_timestamp.getTime();
								System.out.println(diffe);
								long total_minutes = diffe/(1000*60);
								System.out.println(total_minutes);
								
								long diff_1_tariff = Duration.between(charging_start_time,peak_time_end).toMinutes();
								long diff_2_tariff = Duration.between(off_peak_time_start,off_peak_time_end).toMinutes();
								long diff_3_tariff = Duration.between(day_start_tariff, charging_end_time).toMinutes();
								
								System.out.println(diff_1_tariff);
								System.out.println(diff_2_tariff);
								// Calculation Process
								System.out.println(charge_kwh);
								double kwh_1 = (charge_kwh/total_minutes)*diff_1_tariff;
								double kwh_2 = (charge_kwh/total_minutes)*diff_2_tariff;
								Double kwh_3 = (charge_kwh/total_minutes)*diff_3_tariff;
								
								
								double consume = rs.getDouble("total_consumption_amount");
								System.out.println("total_consume "+consume);
								double _session_1_price = (consume/total_minutes)*diff_1_tariff;
								double _session_2_price = (consume/total_minutes)*diff_2_tariff;
								double _session_3_price = (consume/total_minutes)*diff_2_tariff;
								System.out.println("1 Session Price "+_session_1_price);
								System.out.println("2 Session Price "+_session_2_price);
								System.out.println("3 Session Price "+_session_3_price);
								// price 
								double electricity = kwh_1*peak_price;
								
								
								double estimate_profit_session_1 = _session_1_price-electricity;
								GeneralPurposeRateDTO dtoReport = new GeneralPurposeRateDTO();
								dtoReport.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport.setCharge_duration(diff_1_tariff);
								dtoReport.setCharge_kWh(kwh_1);
								dtoReport.setElectricity_charge(electricity);
								dtoReport.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport.setEstimated_profit(estimate_profit_session_1);
								dtoReport.setOrder_number(rs.getString("order_number"));
								dtoReport.setRevenue_without_vat_and_ssl(estimate_profit_session_1);
								dtoReport.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport.setTotal_consumption_amount(_session_1_price);
								dtoReport.setTime_zone("PEAK");
								domestic.add(dtoReport);
								
								double electricityn = kwh_2*off_peak_price;
								
								double estimate_profitn = _session_2_price-electricityn;
								GeneralPurposeRateDTO dtoReport1 = new GeneralPurposeRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_2_tariff);
								dtoReport1.setCharge_kWh(kwh_2);
								dtoReport1.setElectricity_charge(electricityn);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profitn);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profitn);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_2_price);
								dtoReport1.setTime_zone("OFF PEAK");
								domestic.add(dtoReport1);
								
								double electricity_session_3 = kwh_3*day_price;
								double estimate_profit_sessin_3 = _session_3_price-electricity_session_3;
								GeneralPurposeRateDTO dtoReport2 = new GeneralPurposeRateDTO();
								dtoReport1.setCar_plate_number(rs.getString("car_plate_number"));
								dtoReport1.setCharge_duration(diff_3_tariff);
								dtoReport1.setCharge_kWh(kwh_3);
								dtoReport1.setElectricity_charge(electricity_session_3);
								dtoReport1.setEnd_charging_time(rs.getString("end_charging_time"));
								dtoReport1.setEstimated_profit(estimate_profit_sessin_3);
								dtoReport1.setOrder_number(rs.getString("order_number"));
								dtoReport1.setRevenue_without_vat_and_ssl(estimate_profit_sessin_3);
								dtoReport1.setStart_charging_time(rs.getString("start_charging_time"));
								dtoReport1.setTotal_consumption_amount(_session_3_price);
								dtoReport1.setTime_zone("DAY");
								domestic.add(dtoReport2);
								
								System.out.println(rs.getTimestamp("start_charging_time")+" "+rs.getTimestamp("end_charging_time")+"=> 9");
							}
																						
					}
					
			
		
			
	
	}catch(Exception e) {
			e.printStackTrace();
	}
	return domestic;
}

		
}