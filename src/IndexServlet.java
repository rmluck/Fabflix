import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.Gson;

import jakarta.servlet.ServletConfig;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This IndexServlet is declared in the web annotation below,
 * which is mapped to the URL pattern /api/index.
 */
@WebServlet(name = "IndexServlet", urlPatterns = "/api/index")
public class IndexServlet extends HttpServlet {
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
     * Handles GET requests to store session information
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = request.getParameter("action");
        if (action == null) {
            handleSessionData(request, response);
            return;
        }

        switch (action) {
            case "getGenres":
                getGenres(response);
                break;
            case "getMoviesByGenre":
                String genreId = request.getParameter("genreId");
                getMoviesByGenre(genreId, response);
                break;
            case "getMoviesByTitle":
                String letter = request.getParameter("letter");
                getMoviesByTitle(letter, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
        }
    }

    /**
     * Handle session data and store it in JSON format
     */
    private void handleSessionData(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String sessionId = session.getId();
        long lastAccessTime = session.getLastAccessedTime();

        JsonObject responseJsonObject = new JsonObject();
        responseJsonObject.addProperty("sessionId", sessionId);
        responseJsonObject.addProperty("lastAccessTime", new Date(lastAccessTime).toString());

        ArrayList<String> previousItems = (ArrayList<String>) session.getAttribute("previousItems");
        if (previousItems == null) {
            previousItems = new ArrayList<>();
        }

        // Log to localhost log
        request.getServletContext().log("getting " + previousItems.size() + " items");
        JsonArray previousItemsJsonArray = new JsonArray();
        previousItems.forEach(previousItemsJsonArray::add);
        responseJsonObject.add("previousItems", previousItemsJsonArray);

        // Write all data into jsonObject
        response.getWriter().write(responseJsonObject.toString());
    }

    private void getGenres(HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Fetch genres from database
        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT id, name FROM genres ORDER BY name";
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            List<Genre> genres = new ArrayList<>();
            while (rs.next()) {
                genres.add(new Genre(rs.getInt("id"), rs.getString("name")));
            }

            rs.close();
            statement.close();

            // Send genres as JSON response
            out.write(new Gson().toJson(genres));
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

    private void getMoviesByGenre(String genreId, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Fetch movies by genre from database
        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT m.id, m.title, m.year, m.director FROM movies m " +
                    "JOIN genres_in_movies gim ON m.id = gim.movieId " +
                    "WHERE gim.genreId = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, genreId);
            ResultSet rs = statement.executeQuery();

            List<Movie> movies = new ArrayList<>();
            while (rs.next()) {
                movies.add(new Movie(rs.getString("id"), rs.getString("title"), rs.getInt("year"), rs.getString("director")));
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
                movies.add(new Movie(rs.getString("id"), rs.getString("title"), rs.getInt("year"), rs.getString("director")));
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

    /**
     * Handles POST requests to add and show item list information
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String item = request.getParameter("item");
        System.out.println(item);
        HttpSession session = request.getSession();

        // get previous items in ArrayList
        ArrayList<String> previousItems = (ArrayList<String>) session.getAttribute("previousItems");
        if (previousItems == null) {
            previousItems = new ArrayList<>();
            previousItems.add(item);
            session.setAttribute("previousItems", previousItems);
        } else {
            // prevent corrupted states through sharing under multi-threads
            // will only be executed by one thread at a time
            synchronized (previousItems) {
                previousItems.add(item);
            }
        }

        JsonObject responseJsonObject = new JsonObject();

        JsonArray previousItemsJsonArray = new JsonArray();
        previousItems.forEach(previousItemsJsonArray::add);
        responseJsonObject.add("previousItems", previousItemsJsonArray);

        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        out.write(responseJsonObject.toString());
        out.close();
    }
}