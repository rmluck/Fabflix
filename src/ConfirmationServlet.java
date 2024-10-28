import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
@WebServlet(name = "ConfirmationServlet", urlPatterns = "/api/confirmation")
public class ConfirmationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        // Retrieve cart items from the session
        ArrayList<Movie> cartItems = (ArrayList<Movie>) session.getAttribute("cartItems");

        if (cartItems == null || cartItems.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No items found in the cart.");
            return;
        }

        // Calculate the total price of the order
        double totalPrice = 0.0;
        for (Movie movie : cartItems) {
            totalPrice += movie.getPrice() * movie.getQuantity(); // Assuming you have a price and quantity in the Movie class
        }

        // Create a response with the order details
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        out.write("{");
        out.write("\"totalPrice\": " + totalPrice + ",");
        out.write("\"orderId\": \"" + generateOrderId(session) + "\""); // Generate a mock order ID for now
        out.write("}");
        out.flush();
        out.close();
    }

    private String generateOrderId(HttpSession session) {
        return "ORD-" + session.getId().substring(0, 8); // Mock order ID based on session ID
    }
}
