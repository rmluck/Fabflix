import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
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
import jakarta.servlet.http.HttpSession;

// Declaring a WebServlet called LogoutServlet, which maps to url "/api/logout"
@WebServlet(name = "LogoutServlet", urlPatterns = "/api/logout")
public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

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
        JsonObject responseJsonObject = new JsonObject();

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
            responseJsonObject.addProperty("status", "success");
            responseJsonObject.addProperty("message", "Successfully logged out");
            System.out.println("User logged out successfully.");
        } else {
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "No active session found");
            System.out.println("No active session to log out.");
        }

        out.write(responseJsonObject.toString());
        out.flush();
        out.close();
    }
}