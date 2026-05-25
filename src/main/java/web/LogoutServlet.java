package web;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;



@WebServlet("/logout")
public class LogoutServlet extends HttpServlet{
	

	@Override
	protected void doGet(HttpServletRequest req,HttpServletResponse res) throws IOException,ServletException {
			HttpSession sess = req.getSession(false);
			
			if(sess != null) {
				
				sess.invalidate();
				
			}
			
			
			res.sendRedirect("index.jsp?success=logout");
	}
	
	
	

}
