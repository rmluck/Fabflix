import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    private static final long serialVersionUID = 1L;
    private DataSource dataSource;

    public void init(ServletConfig config ) {
        try{
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbexample");
        } catch (NamingException e) {
            e.printStackTrace();
        }

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        JsonObject responseJsonObject = new JsonObject();

        if (email.equals("test@uci.edu") && password.equals("123456")) {
            //test login successfull
            request.getSession().setAttribute("user", new User(email));
            responseJsonObject.addProperty("status", "success");
            responseJsonObject.addProperty("message", "Success");
            System.out.println("Test user login success: " + email);
        } else {
            request.getServletContext().log("Test user login fail: " + email);
            try (Connection con = dataSource.getConnection()) {
                String query = "SELECT * FROM customers WHERE email = ? AND password = ?";
                PreparedStatement ps = con.prepareStatement(query);
                ps.setString(1, email);
                ps.setString(2, password);

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    request.getSession().setAttribute("user", new User(email));
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");
                } else {
                    // Login fail
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "Invalid email or password");
                    System.out.println("Login Failed: " + email);
                }
            } catch (Exception e) {
                responseJsonObject.addProperty("status", "error");
                responseJsonObject.addProperty("message", "Internal Server Error");
                System.out.println("Internal Server Error");
                //System.out.println(e.printStackTrace().toString());
            }
        }

        response.getWriter().write(responseJsonObject.toString());
    }
}