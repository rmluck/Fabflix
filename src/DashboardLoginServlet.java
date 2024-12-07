import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletContext;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import com.google.gson.JsonObject;

// Declaring a WebServlet called LoginServlet, which maps to url "/api/_dashboard_login"
@WebServlet(name = "DashboardLoginServlet", urlPatterns = "/api/_dashboard_login")
public class DashboardLoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.xml
    private DatabaseConnectionManager dbManager;

    @Override
    public void init(ServletConfig config) {
        ServletContext context = config.getServletContext();
        dbManager = (DatabaseConnectionManager) context.getAttribute("DatabaseConnectionManager");

        if (dbManager == null) {
            throw new IllegalStateException("DatabaseConnectionManager is not initialized in the context.");
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
            request.getSession().setAttribute("admin", "admin");
            responseJsonObject.addProperty("status", "success");
            responseJsonObject.addProperty("message", "Successfully logged in");
            System.out.println("Test admin login success: " + email);
        } else {
            request.getServletContext().log("Test admin login fail: " + email);

            // Create new connection to database
            try (Connection conn = dbManager.getConnection("READ")) {
                String query = "SELECT * FROM employees WHERE email = ?"; // AND password = ?";
                PreparedStatement statement = conn.prepareStatement(query);
                statement.setString(1, email);
                //statement.setString(2, password);
                ResultSet resultSet = statement.executeQuery();

                // If user is found
                if (resultSet.next()) {
                    String storedPassword = resultSet.getString("password");

                    if(password.equals(storedPassword)) {
                        request.getSession().setAttribute("admin", "admin");
                        responseJsonObject.addProperty("status", "success");
                        responseJsonObject.addProperty("message", "Successfully logged in");
                        System.out.println("Admin login success: " + email);
                    } else {
                        // Login fail
                        responseJsonObject.addProperty("status", "fail");
                        responseJsonObject.addProperty("message", "Invalid email or password");
                        System.out.println("Login Failed: " + email);
                    }
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