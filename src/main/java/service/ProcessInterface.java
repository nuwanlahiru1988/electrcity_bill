package service;

import java.util.List;

import dto.ReportDomasticNormalDTO;

public interface ProcessInterface {
	
	List<ReportDomasticNormalDTO> StartBillCalculationProcess(String charging_station_name, 
			String charging_lifecycle_start_date, 
			String charging_lifecycle_end_date,
			int tariff_catgory);

}
