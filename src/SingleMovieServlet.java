import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
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

// Declaring a WebServlet called SingleMovieServlet, which maps to url "/api/single-movie"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbexample");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage
        try (Connection conn = dataSource.getConnection()) {
            System.out.println("AT QUERY");
            // Construct query with parameter represented by "?"
            String query = "SELECT m.id AS movieId, m.title, m.year, m.director, " +
                    "GROUP_CONCAT(DISTINCT g.name ORDER BY g.name) AS genres, " +
                    "GROUP_CONCAT(DISTINCT CONCAT(s.id, ':', s.name) ORDER BY s.name) AS stars, " +
                    "r.rating AS rating " +
                    "FROM movies AS m " +
                    "LEFT JOIN stars_in_movies AS sim ON m.id = sim.movieId " +
                    "LEFT JOIN stars AS s ON sim.starId = s.id " +
                    "LEFT JOIN genres_in_movies AS gim ON m.id = gim.movieId " +
                    "LEFT JOIN genres AS g ON gim.genreId = g.id " +
                    "LEFT JOIN ratings AS r ON m.id = r.movieId " +
                    "WHERE m.id = ? " +
                    "GROUP BY m.id";

            // Declare statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform query
            ResultSet rs = statement.executeQuery();

            JsonObject jsonObject = new JsonObject();

            // Iterate through each row of rs
            while (rs.next()) {
                String movieId = rs.getString("movieId");
                String movieTitle = rs.getString("title");
                String movieYear = rs.getString("year");
                String movieDirector = rs.getString("director");
                String stars = rs.getString("stars");
                String genres = rs.getString("genres");
                String rating = rs.getString("rating");

                // Create a JsonObject based on the data we retrieve from rs
                jsonObject.addProperty("movie_id", movieId);
                jsonObject.addProperty("movie_title", movieTitle);
                jsonObject.addProperty("movie_year", movieYear);
                jsonObject.addProperty("movie_director", movieDirector);
                jsonObject.addProperty("stars", stars);
                jsonObject.addProperty("genres", genres);
                jsonObject.addProperty("rating", rating);

            }

            rs.close();
            statement.close();

            // Write JSON string to output
            out.write(jsonObject.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);
        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}
