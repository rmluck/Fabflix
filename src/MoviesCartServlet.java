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
                System.out.println("Attempting to remove movie with ID: " + movieId);
                boolean removed = cartItems.removeIf(movie -> movie.getId().equals(movieId));

                if (removed) {
                    System.out.println("Successfully removed movie with ID: " + movieId);
                } else {
                    System.out.println("Movie with ID: " + movieId + " not found in cart.");
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Movie not found in cart");
                    return;
                }
                break;

            case "update":
                String quantityString = request.getParameter("quantity");
                int quantity = Integer.parseInt(quantityString);

                Movie movieToUpdate = cartItems.stream()
                        .filter(movie -> movie.getId().equals(movieId))
                        .findFirst()
                        .orElse(null);

                if (movieToUpdate != null) {
                    if (quantity >= 0) {
                        if (quantity == 0) {
                            cartItems.remove(movieToUpdate);
                            System.out.println("Removed movie ID: " + movieId + " from cart.");
                        } else {
                            movieToUpdate.setQuantity(quantity); // Update quantity
                            System.out.println("Updated quantity of movie ID: " + movieId + " to " + quantity);
                        }
                    } else {
                        System.out.println("Invalid quantity for movie ID: " + movieId);
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid quantity.");
                        return;
                    }
                } else {
                    System.out.println("Movie with ID: " + movieId + " not found in cart.");
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Movie not found in cart");
                    return;
                }
                break;
        }

        session.setAttribute("cartItems", cartItems);

        response.setStatus(HttpServletResponse.SC_OK);
    }
}
