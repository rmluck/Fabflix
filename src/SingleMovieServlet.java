import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

// Declaring a WebServlet called SingleMovieServlet, which maps to url "/api/single-movie"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DatabaseConnectionManager dbManager;

    @Override
    public void init(ServletConfig config) {
        ServletContext context = config.getServletContext();
        dbManager = (DatabaseConnectionManager) context.getAttribute("DatabaseConnectionManager");

        if (dbManager == null) {
            throw new IllegalStateException("DatabaseConnectionManager is not initialized in the context.");
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request
        String movieId = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + movieId);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage
        try (Connection conn = dbManager.getConnection("READ")) {
            // Construct query with parameter represented by "?"
            String query = "SELECT m.id, m.title, m.year, m.director, r.rating, " +
                    "(SELECT GROUP_CONCAT(CONCAT(all_genres.id, ':', all_genres.name) SEPARATOR ',') " +
                    " FROM (SELECT g.id, g.name " +
                    " FROM genres_in_movies AS gimov " +
                    " JOIN genres AS g ON gimov.genreId = g.id " +
                    " WHERE gimov.movieId = m.id " +
                    " ORDER BY g.name) AS all_genres) AS genres, " +
                    "(SELECT GROUP_CONCAT(CONCAT(all_stars.id, ':', all_stars.name) SEPARATOR ',') " +
                    " FROM (SELECT s.id, s.name " +
                    " FROM stars_in_movies AS simov " +
                    " JOIN stars AS s ON simov.starId = s.id " +
                    " JOIN (SELECT starId, COUNT(movieId) AS movie_count " +
                    " FROM stars_in_movies " +
                    " GROUP BY starId) AS star_counts ON s.id = star_counts.starId " +
                    " WHERE simov.movieId = m.id ORDER BY star_counts.movie_count DESC, s.name ASC) AS all_stars) AS stars " +
                    " FROM movies AS m " +
                    " JOIN ratings AS r ON m.id = r.movieId " +
                    " JOIN genres_in_movies AS gim ON m.id = gim.movieId " +
                    " WHERE m.id = ? " +
                    " GROUP BY m.id;";

            // Declare statement
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, movieId);
            ResultSet rs = statement.executeQuery();

            List<Movie> movies = new ArrayList<>();
            while (rs.next()) {
                movies.add(new Movie(rs.getString("id"), rs.getString("title"), rs.getInt("year"), rs.getString("director"), rs.getString("genres"), rs.getString("stars"), rs.getFloat("rating")));
            }

            rs.close();
            statement.close();

            // Write JSON string to output
            out.write(new Gson().toJson(movies));
            // Set response status to 200 (OK)
            response.setStatus(200);
        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}