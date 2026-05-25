package web;

import java.io.IOException;


import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
	
	@Override
	protected void doGet(HttpServletRequest req,HttpServletResponse res) throws IOException,ServletException {
		res.sendRedirect("login.jsp");
	}
	
	@Override
	protected void doPost(HttpServletRequest req,HttpServletResponse res) throws IOException,ServletException{
		
		String username = req.getParameter("username");
		String password = req.getParameter("password");
		
		if(username != null && password != null) {
			
			UserDAO user = new UserDAO();
			User usern = user.loginUser(username, password);
			if(usern != null) {
				HttpSession session = req.getSession();
				session.setAttribute("loggedInUser", user);
				res.sendRedirect("dashboard.jsp");
			}else {
				res.sendRedirect("index.jsp?error=invalid");
			}
			
			
		}
		
	}

}
