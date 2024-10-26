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
        HttpSession session = request.getSession(false);
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
        HttpSession session = request.getSession(false);
        ArrayList<Movie> cartItems = (ArrayList<Movie>) session.getAttribute("cartItems");

        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }

        String action = request.getParameter("action");
        String movieId = request.getParameter("movieId");
        String title = request.getParameter("title");
        //int year = Integer.parseInt(request.getParameter("year"));

        switch (action) {
            case "add":
                Movie existingMovieInCart = cartItems.stream()
                        .filter(movie -> movie.getId().equals(movieId))
                        .findFirst()
                        .orElse(null);

                if (existingMovieInCart != null) {
                    existingMovieInCart.increaseQuantity();
                } else {
                    Movie newMovie = new Movie(movieId, title, 0, "", "", "", 0.0f); //default vals, not sure if all needed.
                    cartItems.add(newMovie);
                }
                break;

            case "remove":
                System.out.println("Attempting to remove movie with ID: " + movieId); // Log the movie ID being removed
                boolean removed = cartItems.removeIf(movie -> movie.getId().equals(movieId));

                if (removed) {
                    System.out.println("Successfully removed movie with ID: " + movieId); // Log success
                } else {
                    System.out.println("Movie with ID: " + movieId + " not found in cart."); // Log if not found
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Movie not found in cart");
                    return;
                }
                break;
//                cartItems.stream()
//                        .filter(movie -> movie.getId().equals(movieId))
//                        .findFirst()
//                        .ifPresent(movie -> movie.setQuantity(0)); // Assuming you have a setQuantity method
//                break;
        }

        session.setAttribute("cartItems", cartItems);

        response.setStatus(HttpServletResponse.SC_OK);
    }
}
