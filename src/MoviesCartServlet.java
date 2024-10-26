import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

@WebServlet("/cart")
public class MoviesCartServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        ArrayList<Movie> cartItems = (ArrayList<Movie>) session.getAttribute("cartItems");

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // Initialize cart if it doesn't exist
        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }

        // Convert cart items to JSON
        Gson gson = new Gson();
        String json = gson.toJson(cartItems);
        out.print(json);
        out.flush();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        ArrayList<Movie> cartItems = (ArrayList<Movie>) session.getAttribute("cartItems");

        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }

        String action = request.getParameter("action");
        String movieId = request.getParameter("movieId");

        switch (action) {
            case "remove":
                // Logic for removing a movie from the cart
                cartItems.removeIf(movie -> movie.getId().equals(movieId));
                break;
        }

        // Update session attribute
        session.setAttribute("cartItems", cartItems);

        // Respond with a success message or redirect
        response.setStatus(HttpServletResponse.SC_OK); // Set the response status to OK
    }
}
