package web;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.util.List;

import com.google.gson.Gson;

import dao.ConsumerCategoryDAO;
import dao.OtherTariffListDAO;
import dao.StationDAO;
import dao.TariffDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.ConsumerCategory;
import model.OtherConsumer;
import model.Station;

@WebServlet("/getTariffList")
public class JsonRequestHandlerServlet extends HttpServlet{
	
	@Override
	protected void doGet(HttpServletRequest req,HttpServletResponse res) throws IOException,ServletException{
			
		 	ConsumerCategoryDAO dao = new ConsumerCategoryDAO();
 			List<ConsumerCategory> lists = dao.getCategoryList();
			Gson json = new Gson();
			String json_data = json.toJson(lists);
			
			res.setContentType("application/json");
			res.setCharacterEncoding("UTF-8");
			
			res.getWriter().write(json_data);
			
	
	}
	
	
	
}
