import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

// Declaring a WebServlet called ItemServlet, which maps to url "/items"
public class ItemsServlet extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Get instance of current session on request
        HttpSession session = request.getSession();

        // Retrieve data named "previousItems" from session
        ArrayList<String> previousItems = (ArrayList<String>) session.getAttribute("previousItems");

        // If "previousItems" is not found on session, this is new user, thus create new previousItems ArrayList for user
        if (previousItems == null) {
            // Add newly created ArrayList to session, so that ic can be retrieved next time
            previousItems = new ArrayList<String>();
            session.setAttribute("previousItems", previousItems);
        }

        // Log to localhost log
        request.getServletContext().log("getting " + previousItems.size() + " items");

        // Get parameter that sent by GET request url
        String newItem = request.getParameter("newItem");

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String title = "Items Purchased";

        out.println(String.format("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">\n" +
                "<html>\n" +
                "   <head>" +
                "   <title>%s</title>" +
                "   </head>\n" +
                "   <body bgcolor=\"#FDF5E6\">\n" +
                "       <h1>%s</h1>", title, title));

        // To prevent multiple clients, requests from altering previousItems ArrayList at same time, lock ArrayList while updating
        synchronized (previousItems) {
            if (newItem != null) {
                previousItems.add(newItem);
            }

            // Display current previousItems ArrayList
            if (previousItems.size() == 0) {
                out.println("<i>No items</i>");
            } else {
                out.println("<ul>");
                for (String previousItem : previousItems) {
                    out.println("<li>" + previousItem + "</li>");
                }
                out.println("</ul>");
            }
        }
        out.println("</body></html>");
    }
}
