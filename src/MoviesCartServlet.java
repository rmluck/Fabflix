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

@WebServlet("/api/cart")
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

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        ArrayList<Movie> cartItems = (ArrayList<Movie>) session.getAttribute("cartItems");

        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }

        String action = request.getParameter("action");
        String movieId = request.getParameter("movieId");
        String title = request.getParameter("title");
        int year = Integer.parseInt(request.getParameter("year"));

        switch (action) {
            case "add":
                Movie existingMovieInCart = cartItems.stream()
                        .filter(movie -> movie.getId().equals(movieId))
                        .findFirst()
                        .orElse(null);

                if (existingMovieInCart != null) {
                    existingMovieInCart.increaseQuantity();
                } else {
                    Movie newMovie = new Movie(movieId, title, year, "", "", "", 0.0f); //default vals, not sure if all needed.
                    cartItems.add(newMovie);
                }
                break;

            case "remove":
                cartItems.removeIf(movie -> movie.getId().equals(movieId));
                break;
        }

        session.setAttribute("cartItems", cartItems);

        response.setStatus(HttpServletResponse.SC_OK);
    }
}
