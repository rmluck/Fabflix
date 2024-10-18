import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// Declaring a WebServlet called FormServlet, which maps to url "/form"
@WebServlet(name = "FormServlet", urlPatterns = "/form")
public class FormServlet extends HttpServlet {
    //Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbexample");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    // Use http POST
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Building page head with title
        out.println("<html><head><title>MovieDBExample: Found Records</title></head>");

        // Building page body
        out.println("<body><h1>MovieDBExample: Found Records</h1>");

        try {
            // Create new connection to database
            Connection dbCon = dataSource.getConnection();

            // Declare new statement
            Statement statement = dbCon.createStatement();

            // Retrieve parameter "email" from http request, which refers to value of <input name="email"> in index.html
            String name = request.getParameter("email");

            // Generate a SQL query
            String query = String.format("SELECT * from stars where name like '%s'", name);

            // Log to localhost log
            request.getServletContext().log("query: " + query);

            // Perform query
            ResultSet rs = statement.executeQuery(query);

            // Create html <table>
            out.println("<table border>");

            // Iterate through each row of rs and create table row <tr>
            out.println("<tr><td>ID</td><td>Name</td></tr>");
            while (rs.next()) {
                String m_ID = rs.getString("ID");
                String m_name = rs.getString("name");
                out.println(String.format("<tr><td>%s</td><td>%s</td></tr>", m_ID, m_name));
            }

            out.println("</table>");

            // Close all structures
            rs.close();
            statement.close();
            dbCon.close();
        } catch (Exception e) {
            request.getServletContext().log("Error: ", e);

            // Output error message to html
            out.println(String.format("<html><head><title>MovieDBExample: Error</title></head>\\n<body><p>SQL error in doGet: %s</p></body></html>", e.getMessage()));

            return;
        }
        out.close();
    }
}
