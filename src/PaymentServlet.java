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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.IOException;
import java.util.ArrayList;

@WebServlet(name = "PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which is registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbexample");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

//    @Override
//    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        HttpSession session = request.getSession();
//        ArrayList<Movie> cartItems = (ArrayList<Movie>) session.getAttribute("cartItems");
//        double totalPrice = 0.0;
//
////        // Calculate total price based on cart items
////        if (cartItems != null) {
////            for (Movie movie : cartItems) {
////                totalPrice += movie.getPrice();
////            }
////        }
//
//        request.setAttribute("totalPrice", totalPrice);
//
//        request.getRequestDispatcher("payment.jsp").forward(request, response);
//    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String creditCardNumber = request.getParameter("creditCardNumber");
        String expirationDate = request.getParameter("expirationDate");

        // Validate the payment information
        boolean isValid = validatePayment(firstName, lastName, creditCardNumber, expirationDate);

        if (isValid) {
            HttpSession session = request.getSession();
            int customerId = (Integer) session.getAttribute("customerId");

            ArrayList<Movie> cartItems = (ArrayList<Movie>) session.getAttribute("cartItems");

            if (cartItems != null) {
                for (Movie movie : cartItems) {
                    String movieId = movie.getId();

                    recordSale(customerId, movieId);
                }
            }

            session.removeAttribute("cartItems");

            response.sendRedirect("/fabflix.com/confirmation.html");
        } else {
            response.sendRedirect("payment.html?error=Invalid payment information");
        }
    }


    private boolean validatePayment(String firstName, String lastName, String creditCardNumber, String expirationDate) {
        expirationDate = expirationDate.replace("/", "-");

        String query = "SELECT * FROM creditcards WHERE firstName = ? AND lastName = ? AND id = ? AND expiration = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {

            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setString(3, creditCardNumber);
            statement.setString(4, expirationDate);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    private void recordSale(int customerId, String movieId) {
        String query = "INSERT INTO sales (customerId, movieId, saleDate) VALUES (?, ?, ?, CURDATE())";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {

            statement.setInt(1, customerId);
            statement.setString(2, movieId);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Sale recorded successfully.");
            } else {
                System.out.println("Failed to record sale.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
