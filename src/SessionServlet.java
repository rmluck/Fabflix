import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

// Declaring a WebServlet called SessionServlet, which maps to url "/session"
@WebServlet(name = "SessionServlet", urlPatterns="/session")
public class SessionServlet extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String title = "Session Tracking";

        // Get an instance of current session on request
        HttpSession session = request.getSession(true);
        String heading;

        // Retrieve data named "accessCount" from session, which count how many times user requested before
        Integer accessCount = (Integer) session.getAttribute("accessCount");

        if (accessCount == null) {
            // User is never seen before
            accessCount = 0;
            heading = "Welcome, New User";
        } else {
            // User has requested before, thus user information can be found in session
            heading = "Welcome Back";
            accessCount++;
        }

        // Update new accessCount to session, replacing old value if existed
        session.setAttribute("accessCount", accessCount);

        out.println("<html><head><title>" + title + "</title></head>\n" +
                "<body bgcolor=\"#FDF5E6\">\n" +
                // Set greeting header generated before
                "<h1 ALIGN=\"center\">" + heading + "</h1>\n" +
                "<h2>Information on Your Session:</H2>\n" +
                // Create <table>
                "<table border=1 align=\"center\">\n" +
                // Create <tr> (table row)
                "  <tr bgcolor=\"#FFAD00\">\n" +
                // Create two <th>s (table header)
                "    <th>Info Type<th>Value\n" +
                // Create <tr> (table row)
                "  <tr>\n" +
                // Create first <td> (table data) in <tr>, which corresponds to first column
                "    <td>ID\n" +
                // Create second <td> (table data) in <tr>, which corresponds to second column
                "    <td>" + session.getId() + "\n" +
                // Repeat for more table rows and data
                "  <tr>\n" +
                "    <td>Creation Time\n" +
                "    <td>" +
                new Date(session.getCreationTime()) + "\n" +
                "  <tr>\n" +
                "    <td>Time of Last Access\n" +
                "    <td>" +
                new Date(session.getLastAccessedTime()) + "\n" +
                "  <tr>\n" +
                "    <td>Number of Previous Accesses\n" +
                "    <td>" + accessCount + "\n" +
                "  </tr>" +
                "</table>\n");

        // Following two statements show how to retrieve parameters in request
        String myName = request.getParameter("myname");
        if (myName != null) {
            out.println("Hi " + myName + "<br><br>");
        }

        out.println("</body></html");
    }
}
