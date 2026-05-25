package web;

import java.io.IOException;
import java.rmi.ServerException;
import java.util.List;
import dao.StationDAO;
import dto.ChargeOrderInformationDTO;
import dto.GeneralPurposeRateDTO;
import dto.HotelRateDTO;
import dto.IndustrialChargerCalculationDTO;
import dto.ReportDomasticNormalDTO;
import dao.ChargeOrderInformationDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Station;
import service.ProcessController;

@WebServlet("/adminreport")
public class AdminReportServlet extends HttpServlet{
	
	@Override
	protected void doGet(HttpServletRequest req,HttpServletResponse res) throws IOException,ServletException{
				
		StationDAO station = new StationDAO();
		List<Station> stations = station.getStationList();
		req.setAttribute("stationsLists", stations);
		RequestDispatcher rd = req.getRequestDispatcher("adminreport.jsp");
		// res.sendRedirect("adminreport.jsp");
		rd.forward(req, res);
	
	}
	
	@Override
	protected void doPost( HttpServletRequest req,HttpServletResponse res)throws IOException, ServletException {
		
		String station = req.getParameter("station");
		String start_date = req.getParameter("start_date");
		String end_date = req.getParameter("end_date");
		String category = req.getParameter("category");
		String gun_type = "";
		
		int catid =Integer.parseInt(category);
		
		
		
		if(catid == 4) {
			
			String tariff_actegory = req.getParameter("tariff_category");
			int tariff_category = Integer.parseInt(tariff_actegory);
		
			
			
			if(tariff_category == 1){
				
				ChargeOrderInformationDAO chOrderDAOObj = new ChargeOrderInformationDAO();
				//List<ChargeOrderInformationDTO> chObj= chOrderDAOObj.getChargerOrderInformationListByDateRangeANDStationName(station, start_date, end_date);
				
				// Process All Report
				ProcessController processObj = new ProcessController();
				List<IndustrialChargerCalculationDTO>  Obj= processObj.StartBillCalculationProcessIndustrial1(station, start_date, end_date,4,1,gun_type);
				
				
				req.setAttribute("reports", Obj); 
				RequestDispatcher red = req.getRequestDispatcher("adminreport.jsp");
				red.forward(req,res);
				
			}else if(tariff_category == 2) {
				
				ChargeOrderInformationDAO chOrderDAOObj = new ChargeOrderInformationDAO();
				//List<ChargeOrderInformationDTO> chObj= chOrderDAOObj.getChargerOrderInformationListByDateRangeANDStationName(station, start_date, end_date);
				
				// Process All Report
				ProcessController processObj = new ProcessController();
				List<IndustrialChargerCalculationDTO>  Obj= processObj.StartBillCalculationProcessIndustrial2(station, start_date, end_date,4,1,gun_type);
				
				
				req.setAttribute("reports", Obj); 
				RequestDispatcher red = req.getRequestDispatcher("adminreport.jsp");
				red.forward(req,res);
				
			}else if(tariff_category == 3) {
				
				ChargeOrderInformationDAO chOrderDAOObj = new ChargeOrderInformationDAO();
				//List<ChargeOrderInformationDTO> chObj= chOrderDAOObj.getChargerOrderInformationListByDateRangeANDStationName(station, start_date, end_date);
				
				// Process All Report
				ProcessController processObj = new ProcessController();
				List<IndustrialChargerCalculationDTO>  Obj= processObj.StartBillCalculationProcessIndustrial3(station, start_date, end_date,4,1,gun_type);
				
				
				req.setAttribute("reports", Obj); 
				RequestDispatcher red = req.getRequestDispatcher("adminreport.jsp");
				red.forward(req,res);
				
			}else if(tariff_category == 4) {
				
				ChargeOrderInformationDAO chOrderDAOObj = new ChargeOrderInformationDAO();
				//List<ChargeOrderInformationDTO> chObj= chOrderDAOObj.getChargerOrderInformationListByDateRangeANDStationName(station, start_date, end_date);
				
				// Process All Report
				ProcessController processObj = new ProcessController();
				List<HotelRateDTO>  Obj= processObj.StartBillCalculationProcessHotel(station, start_date, end_date,4,1,gun_type);
				
				
				req.setAttribute("reports", Obj); 
				RequestDispatcher red = req.getRequestDispatcher("adminreport.jsp");
				red.forward(req,res);
				
			}else if(tariff_category == 5) {
				
				ProcessController processObj = new ProcessController();
				List<HotelRateDTO>  Obj= processObj.StartBillCalculationProcessHotelIII(station, start_date, end_date,4,1,gun_type);	
				req.setAttribute("reports", Obj); 
				RequestDispatcher red = req.getRequestDispatcher("adminreport.jsp");
				red.forward(req,res);
				
			}else if(tariff_category == 6) {
				
				
				ProcessController processObj = new ProcessController();
				List<GeneralPurposeRateDTO>  Obj= processObj.StartBillCalculationProcessGPR(station, start_date, end_date,4,1,gun_type);	
				req.setAttribute("reports", Obj); 
				RequestDispatcher red = req.getRequestDispatcher("adminreport.jsp");
				red.forward(req,res);
				
			}else if(tariff_category == 7) {
				
				
				ProcessController processObj = new ProcessController();
				List<GeneralPurposeRateDTO>  Obj= processObj.StartBillCalculationProcessGPR2(station, start_date, end_date,4,1,gun_type);	
				req.setAttribute("reports", Obj); 
				RequestDispatcher red = req.getRequestDispatcher("adminreport.jsp");
				red.forward(req,res);
				
			}else if(tariff_category == 8) {
				
				
				ProcessController processObj = new ProcessController();
				List<GeneralPurposeRateDTO>  Obj= processObj.StartBillCalculationProcessGPR3(station, start_date, end_date,4,1,gun_type);	
				req.setAttribute("reports", Obj); 
				RequestDispatcher red = req.getRequestDispatcher("adminreport.jsp");
				red.forward(req,res);
			}
			
		}else {
		
		ChargeOrderInformationDAO chOrderDAOObj = new ChargeOrderInformationDAO();
		//List<ChargeOrderInformationDTO> chObj= chOrderDAOObj.getChargerOrderInformationListByDateRangeANDStationName(station, start_date, end_date);
		
		// Process All Report
		ProcessController processObj = new ProcessController();
		List<ReportDomasticNormalDTO>  Obj= processObj.StartBillCalculationProcess(station, start_date, end_date, catid);
		
		
		req.setAttribute("reports", Obj); 
		RequestDispatcher red = req.getRequestDispatcher("domastic_normal_report.jsp");
		red.forward(req,res);
		
		
		}
		
	}
	
	
	
}