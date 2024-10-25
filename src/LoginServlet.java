import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import com.google.gson.JsonObject;

// Declaring a WebServlet called LoginServlet, which maps to url "/api/login"
@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbexample");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    // Use http POST
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        JsonObject responseJsonObject = new JsonObject();

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        if (email.equals("test@uci.edu") && password.equals("123456")) {
            // Test login successful
            request.getSession().setAttribute("user", new User(email));
            responseJsonObject.addProperty("status", "success");
            responseJsonObject.addProperty("message", "Success");
            System.out.println("Test user login success: " + email);
        } else {
            request.getServletContext().log("Test user login fail: " + email);

            // Create new connection to database
            try (Connection conn = dataSource.getConnection()) {
                String query = "SELECT * FROM customers WHERE email = ? AND password = ?";
                PreparedStatement statement = conn.prepareStatement(query);
                statement.setString(1, email);
                statement.setString(2, password);
                ResultSet resultSet = statement.executeQuery();

                // If user is found
                if (resultSet.next()) {
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
            }
        }

        out.write(responseJsonObject.toString());
        out.flush();
        out.close();
//        response.getWriter().write(responseJsonObject.toString());
    }
}