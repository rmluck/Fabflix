import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

@WebServlet("/api/cart/total")
public class TotalPriceServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbexample");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        ArrayList<Movie> cartItems = (ArrayList<Movie>) session.getAttribute("cartItems");
        double totalPrice = 0.0;

        // Calculate total price based on cart items
        if (cartItems != null) {
            for (Movie movie : cartItems) {
                totalPrice += movie.getPrice() * movie.getQuantity();
            }
        }

        // Create a JSON response
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.write("{\"totalPrice\": " + totalPrice + "}");
        out.flush();
        out.close();
    }
}
