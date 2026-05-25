package service;

import java.util.ArrayList;
import java.util.List;

import dao.ChargeOrderInformationDAO;
import dto.GeneralPurposeRateDTO;
import dto.HotelRateDTO;
import dto.IndustrialChargerCalculationDTO;
import dto.ReportDomasticNormalDTO;

public class ProcessController implements ProcessInterface {
	
	public List<ReportDomasticNormalDTO> StartBillCalculationProcess(
			String charging_station_name, 
			String charging_lifecycle_start_date, 
			String charging_lifecycle_end_date,
			int tariff_catgory) {
			List<ReportDomasticNormalDTO> reportList = new ArrayList<>();
			
		// get Charger billing cycle
		if(tariff_catgory == 1) {
			// Domestic Category
			// Calculate Total kWh for Domestic
			
			ChargeOrderInformationDAO  chgerOrderObj = new ChargeOrderInformationDAO();
			double total_hours = chgerOrderObj.getTotalCharginHours(charging_station_name, charging_lifecycle_start_date, charging_lifecycle_end_date);
			
			if(total_hours <= 30) {
					
					ChargeOrderInformationDAO order = new ChargeOrderInformationDAO();
					List<ReportDomasticNormalDTO> reportObj = order.getDomesticNormalReport(charging_station_name, charging_lifecycle_start_date, charging_lifecycle_end_date,2);
					reportList= reportObj;				
				
			}else if((total_hours <= 60) && (total_hours >= 31)) {
					
					ChargeOrderInformationDAO order = new ChargeOrderInformationDAO();
					List<ReportDomasticNormalDTO> reportObj = order.getDomesticNormalReport(charging_station_name, charging_lifecycle_start_date, charging_lifecycle_end_date,3);
					reportList= reportObj;
					
				}else if((total_hours <= 90) && (total_hours >= 60) ) {
					
					ChargeOrderInformationDAO order = new ChargeOrderInformationDAO();
					List<ReportDomasticNormalDTO> reportObj = order.getDomesticNormalReport(charging_station_name, charging_lifecycle_start_date, charging_lifecycle_end_date,4);
					reportList= reportObj;
					
					
				}else if((total_hours <= 120) && (total_hours >= 90)) {
					
					ChargeOrderInformationDAO order = new ChargeOrderInformationDAO();
					List<ReportDomasticNormalDTO> reportObj = order.getDomesticNormalReport(charging_station_name, charging_lifecycle_start_date, charging_lifecycle_end_date,5);
					reportList= reportObj;
					
				}else if((total_hours >= 121) && (total_hours >= 180)) {
						
					ChargeOrderInformationDAO order = new ChargeOrderInformationDAO();
					List<ReportDomasticNormalDTO> reportObj = order.getDomesticNormalReport(charging_station_name, charging_lifecycle_start_date, charging_lifecycle_end_date,6);
					reportList= reportObj;
					
				}else if((total_hours >= 181)) {
					
					ChargeOrderInformationDAO order = new ChargeOrderInformationDAO();
					List<ReportDomasticNormalDTO> reportObj = order.getDomesticNormalReport(charging_station_name, charging_lifecycle_start_date, charging_lifecycle_end_date,7);
					reportList= reportObj;
					
				}
			
		}else if(tariff_catgory == 2) {
			// Domastic Optional 
			
			
			
		}else if(tariff_catgory == 3) {
			//  Domastic Religious
			
		}else if(tariff_catgory == 4) {
			// other_consumer_category
			
			
			
		}
		
		return reportList;
	}
		
	
	
	public List<IndustrialChargerCalculationDTO> StartBillCalculationProcessIndustrial1(
			String station,
			String start_date,
			String end_date,
			int category,
			int subcategory,
			String gun_type
			) {
		
		List<IndustrialChargerCalculationDTO> industrial = new ArrayList<>();
		ChargeOrderInformationDAO cotiDAO = new ChargeOrderInformationDAO();
		industrial = cotiDAO.getChargerIndustrialData(station,start_date,end_date,category,subcategory,gun_type);
			
		return industrial;
		
	}
	
	public List<IndustrialChargerCalculationDTO> StartBillCalculationProcessIndustrial2(
			String station,
			String start_date,
			String end_date,
			int category,
			int subcategory,
			String gun_type
			) {
		
		List<IndustrialChargerCalculationDTO> industrial = new ArrayList<>();
		ChargeOrderInformationDAO cotiDAO = new ChargeOrderInformationDAO();
		industrial = cotiDAO.getChargerIndustrialDataII(station,start_date,end_date,category,subcategory,gun_type);
			
		return industrial;
		
	}
	
	public List<IndustrialChargerCalculationDTO> StartBillCalculationProcessIndustrial3(
			String station,
			String start_date,
			String end_date,
			int category,
			int subcategory,
			String gun_type
			) {
		
		List<IndustrialChargerCalculationDTO> industrial = new ArrayList<>();
		ChargeOrderInformationDAO cotiDAO = new ChargeOrderInformationDAO();
		industrial = cotiDAO.getChargerIndustrialDataIII(station,start_date,end_date,category,subcategory,gun_type);
			
		return industrial;
		
	}
	
	public List<HotelRateDTO> StartBillCalculationProcessHotel(String station,
			String start_date,
			String end_date,
			int category,
			int subcategory,
			String gun_type){
		 
		List<HotelRateDTO> hotelRateDTOs = new ArrayList<>();
		ChargeOrderInformationDAO cotiDAO = new ChargeOrderInformationDAO();
		hotelRateDTOs = cotiDAO.StartBillCalculationProcessHotel(station,start_date,end_date,category,subcategory,gun_type);
		return hotelRateDTOs;
	}
	
	public List<HotelRateDTO> StartBillCalculationProcessHotelIII(String station,
			String start_date,
			String end_date,
			int category,
			int subcategory,
			String gun_type){
		 
		List<HotelRateDTO> hotelRateDTOs = new ArrayList<>();
		ChargeOrderInformationDAO cotiDAO = new ChargeOrderInformationDAO();
		hotelRateDTOs = cotiDAO.StartBillCalculationProcessHotelIII(station,start_date,end_date,category,subcategory,gun_type);
		return hotelRateDTOs;
	}
	
	public List<GeneralPurposeRateDTO> StartBillCalculationProcessGPR(String station,
			String start_date,
			String end_date,
			int category,
			int subcategory,
			String gun_type){
		 
		List<GeneralPurposeRateDTO> hotelRateDTOs = new ArrayList<>();
		ChargeOrderInformationDAO cotiDAO = new ChargeOrderInformationDAO();
		hotelRateDTOs = cotiDAO.getChargerGeneralPurposeRate(station,start_date,end_date,category,subcategory,gun_type);
		return hotelRateDTOs;
	}
	
	public List<GeneralPurposeRateDTO> StartBillCalculationProcessGPR2(
			String station,
			String start_date,
			String end_date,
			int category,
			int subcategory,
			String gun_type
			) {
		
		List<GeneralPurposeRateDTO> industrial = new ArrayList<>();
		ChargeOrderInformationDAO cotiDAO = new ChargeOrderInformationDAO();
		industrial = cotiDAO.getChargerGeneralPurposeRateII(station,start_date,end_date,category,subcategory,gun_type);
			
		return industrial;
		
	}
	
	public List<GeneralPurposeRateDTO> StartBillCalculationProcessGPR3(
			String station,
			String start_date,
			String end_date,
			int category,
			int subcategory,
			String gun_type
			) {
		
		List<GeneralPurposeRateDTO> industrial = new ArrayList<>();
		ChargeOrderInformationDAO cotiDAO = new ChargeOrderInformationDAO();
		industrial = cotiDAO.getChargerGeneralPurposeRateIII(station,start_date,end_date,category,subcategory,gun_type);
			
		return industrial;
		
	}

}
