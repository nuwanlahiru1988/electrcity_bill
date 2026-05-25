package web;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import dao.StationDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Station;

@WebServlet("/reportform")
public class ReportFormServlet extends HttpServlet {
	
	
	@Override
	protected void doGet(HttpServletRequest req,HttpServletResponse res)throws IOException, ServletException {
		
		StationDAO station = new StationDAO();
		List<Station> stations = station.getStationList();
		req.setAttribute("stationsLists", stations);
		RequestDispatcher rd = req.getRequestDispatcher("reportform.jsp");
		// res.sendRedirect("adminreport.jsp");
		rd.forward(req, res);
		
	}
	
	
	protected void doPost(HttpServletRequest req,HttpServletResponse res)throws IOException, ServletException {
		
	}
	
	

}
