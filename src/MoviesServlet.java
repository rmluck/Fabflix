import com.google.gson.Gson;
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
import java.util.ArrayList;
import java.util.List;

// Declaring a WebServlet called MoviesServlet, which maps to url "/api/movies"
@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

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
        response.setContentType("application/json");
        System.out.println("CONTENT TYPE");
        String title = request.getParameter("title");
        System.out.println("TITLE: " + title);
        String year = request.getParameter("year");
        System.out.println("YEAR: " + year);
        String director = request.getParameter("director");
        System.out.println("DIRECTOR: " + director);
        String star = request.getParameter("star");
        System.out.println("STAR: " + star);

        if (title != null && year != null && director != null && star != null) {
            searchMovies(request, response);
        } else {
            String action = request.getParameter("action");
            System.out.println("Action: " + action);

            if (action == null) {
                handleTop20Movies(request, response);
            } else {
                switch (action) {
                    case "getMoviesByGenre":
                        String genreId = request.getParameter("genreId");
                        System.out.println("genreId: " + genreId);
                        getMoviesByGenre(genreId, response);
                        break;
                    case "getMoviesByTitle":
                        String movieId = request.getParameter("movieId");
                        getMoviesByTitle(movieId, response);
                        break;
                    default:
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
                }
            }
        }

    }

    private void handleTop20Movies(HttpServletRequest request, HttpServletResponse response) throws IOException { //old doGet for top20 proj1
        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage
        try (Connection conn = dataSource.getConnection()) {
            // Construct query
            String query = "SELECT m.id, m.title, m.year, m.director, r.rating, " +
                    "(SELECT GROUP_CONCAT(DISTINCT g.name ORDER BY g.name) FROM genres_in_movies AS gimov " +
                    "JOIN genres AS g ON gimov.genreId = g.id WHERE gimov.movieId = m.id LIMIT 3) AS genres, " +
                    "(SELECT GROUP_CONCAT(CONCAT(three_stars.id, ':', three_stars.name) ORDER BY three_stars.name) " +
                    " FROM (SELECT s.id, s.name FROM stars_in_movies AS simov JOIN stars AS s ON simov.starId = s.id  " +
                    " WHERE simov.movieId = m.id ORDER BY s.name LIMIT 3) AS three_stars) AS stars " +
                    "FROM movies AS m " +
                    "JOIN ratings AS r ON m.id = r.movieId " +
                    "GROUP BY m.id, r.rating " +
                    "ORDER BY r.rating DESC " +
                    "LIMIT 20;";

            // Declare statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Perform query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String movie_id = rs.getString("id");
                String title = rs.getString("title");
                String year = rs.getString("year");
                String director = rs.getString("director");
                String rating = rs.getString("rating");
                String genres = rs.getString("genres");
                String stars = rs.getString("stars");

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject object = new JsonObject();
                object.addProperty("movie_id", movie_id);
                object.addProperty("title", title);
                object.addProperty("year", year);
                object.addProperty("director", director);
                object.addProperty("rating", rating);
                object.addProperty("genres", genres);
                object.addProperty("stars", stars);

                jsonArray.add(object);
            }

            rs.close();
            statement.close();

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to object
            out.write(jsonArray.toString());
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

    private void searchMovies(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String title = request.getParameter("title");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String star = request.getParameter("star");

        List<Movie> movies = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            StringBuilder queryBuilder = new StringBuilder("SELECT DISTINCT m.id, m.title, m.year, m.director " +
                    "FROM movies m " +
                    "JOIN stars_in_movies sim ON m.id = sim.movieId " +
                    "JOIN stars s ON sim.starId = s.id WHERE 1=1");

            if (title != null && !title.isEmpty()) {
                queryBuilder.append(" AND LOWER(m.title) LIKE LOWER(?)");
            }
            if (year != null && !year.isEmpty()) {
                queryBuilder.append(" AND LOWER(m.year) = LOWER(?)");
            }
            if (director != null && !director.isEmpty()) {
                queryBuilder.append(" AND LOWER(m.director) LIKE LOWER(?)");
            }
            if (star != null && !star.isEmpty()) {
                queryBuilder.append(" AND LOWER(s.name) LIKE LOWER(?)");
            }

            PreparedStatement ps = conn.prepareStatement(queryBuilder.toString());

            int index = 1;
            if (title != null && !title.isEmpty()) {
                ps.setString(index++, "%" + title + "%");
            }
            if (year != null && !year.isEmpty()) {
                ps.setString(index++, year);
            }
            if (director != null && !director.isEmpty()) {
                ps.setString(index++, "%" + director + "%");
            }
            if (star != null && !star.isEmpty()) {
                ps.setString(index++, "%" + star + "%");
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                movies.add(new Movie(rs.getString("id"), rs.getString("title"),
                        rs.getInt("year"), rs.getString("director"), rs.getString("genres"), rs.getString("stars"), rs.getFloat("rating")));
            }

            rs.close();
            ps.close();

            response.getWriter().write(new Gson().toJson(movies));
            response.setStatus(200);
        } catch (Exception e) {
            response.setStatus(500);
            response.getWriter().write("{\"errorMessage\": \"" + e.getMessage() + "\"}");
        }
    }

    private void getMoviesByGenre(String genreId, HttpServletResponse response) throws IOException {
        System.out.println("AT GET MOVIES BY GENRE");
        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Fetch movies by genre from database
        try (Connection conn = dataSource.getConnection()) {
            System.out.println("MADE CONNECTION WITH DATABASE");
//            String query = "SELECT m.id, m.title, m.year, m.director FROM movies m " +
//                    "JOIN genres_in_movies gim ON m.id = gim.movieId " +
//                    "WHERE gim.genreId = ?";
            String query = "SELECT m.id, m.title, m.year, m.director, r.rating, " +
                    "(SELECT GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ', ') " +
                    " FROM genres_in_movies AS gimov " +
                    " JOIN genres as g on gimov.genreId = g.id " +
                    " WHERE gimov.movieId = m.id LIMIT 3) AS genres, " +
                    "(SELECT GROUP_CONCAT(CONCAT(three_stars.id, ':', three_stars.name) SEPARATOR ', ') " +
                    " FROM (SELECT s.id, s.name " +
                    " FROM stars_in_movies AS simov " +
                    " JOIN stars AS s ON simov.starId = s.id " +
                    " WHERE simov.movieId = m.id ORDER BY s.name LIMIT 3) AS three_stars) AS stars " +
                    " FROM movies AS m " +
                    " JOIN ratings as r on m.id = r.movieId " +
                    " JOIN genres_in_movies AS gim ON m.id = gim.movieId " +
                    " WHERE gim.genreId = ? " +
                    " GROUP BY m.id, r.rating " +
                    " ORDER BY r.rating DESC " +
                    " LIMIT 20;";
            System.out.println("MADE QUERY");
            PreparedStatement statement = conn.prepareStatement(query);
            System.out.println("PREPARED QUERY");
            statement.setString(1, genreId);
            System.out.println("SET QUERY");
            ResultSet rs = statement.executeQuery();
            System.out.println("EXECUTED QUERY");

            List<Movie> movies = new ArrayList<>();
            while (rs.next()) {
                movies.add(new Movie(rs.getString("id"), rs.getString("title"), rs.getInt("year"), rs.getString("director"), rs.getString("genres"), rs.getString("stars"), rs.getFloat("rating")));
            }

            rs.close();
            statement.close();

            // Send genres as JSON response
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

    private void getMoviesByTitle(String letter, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Fetch movies by title from database
        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT id, title, year, director FROM movies " +
                    "WHERE title LIKE ? ORDER BY title";
            PreparedStatement statement;

            // Handle "*" character for non-alphanumeric titles
            if (letter.equals("*")) {
                query = "SELECT id, title, year, director FROM movies " +
                        "WHERE title REGEXP '^[^a-zA-Z0-9]' ORDER BY title";
                statement = conn.prepareStatement(query);
            } else {
                statement = conn.prepareStatement(query);
                statement.setString(1, letter + "%");
            }

            ResultSet rs = statement.executeQuery();
            List<Movie> movies = new ArrayList<>();
            while (rs.next()) {
                movies.add(new Movie(rs.getString("id"), rs.getString("title"), rs.getInt("year"), rs.getString("director"), rs.getString("genres"), rs.getString("stars"), rs.getFloat("rating")));
            }

            rs.close();
            statement.close();

            // Send genres as JSON response
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
