import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.Gson;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
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
     * Handles GET requests to store session information
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String action = request.getParameter("action");
        if (action == null) {
            handleSessionData(request, response);
            return;
        }

        switch (action) {
            case "searchMovies":
//                searchMovies(request, response);
//                break;
                forwardToMoviesServlet(request, response, "searchMovies", null);
                break;
            case "getGenres":
                getGenres(response);
                break;
            case "getMoviesByGenre":
                String genreId = request.getParameter("genreId");
//                getMoviesByGenre(genreId, response);
//                break;
                forwardToMoviesServlet(request, response, "getMoviesByGenre", genreId);
                break;
            case "getMoviesByTitle":
                String letter = request.getParameter("letter");
//                getMoviesByTitle(letter, response);
//                break;
                forwardToMoviesServlet(request, response, "getMoviesByTitle", letter);
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

    private void forwardToMoviesServlet(HttpServletRequest request, HttpServletResponse response, String action, String param) throws IOException, ServletException {
        request.setAttribute("action", action);

        if ("searchMovies".equals(action)) {
            String title = request.getParameter("title");
            String year = request.getParameter("year");
            String director = request.getParameter("director");
            String star = request.getParameter("star");

            request.setAttribute("title", title);
            request.setAttribute("year", year);
            request.setAttribute("director", director);
            request.setAttribute("star", star);
        } else {
            request.setAttribute("param", param);
        }

        request.getRequestDispatcher("/api/movies").forward(request, response);
    }

//    private void searchMovies(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        String title = request.getParameter("title");
//        String year = request.getParameter("year");
//        String director = request.getParameter("director");
//        String star = request.getParameter("star");
//
//        List<Movie> movies = new ArrayList<>();
//        try (Connection conn = dataSource.getConnection()) {
//            StringBuilder queryBuilder = new StringBuilder("SELECT DISTINCT m.id, m.title, m.year, m.director " +
//                    "FROM movies m " +
//                    "JOIN stars_in_movies sim ON m.id = sim.movieId " +
//                    "JOIN stars s ON sim.starId = s.id WHERE 1=1");
//
//            if (title != null && !title.isEmpty()) {
//                queryBuilder.append(" AND LOWER(m.title) LIKE LOWER(?)");
//            }
//            if (year != null && !year.isEmpty()) {
//                queryBuilder.append(" AND LOWER(m.year) = LOWER(?)");
//            }
//            if (director != null && !director.isEmpty()) {
//                queryBuilder.append(" AND LOWER(m.director) LIKE LOWER(?)");
//            }
//            if (star != null && !star.isEmpty()) {
//                queryBuilder.append(" AND LOWER(s.name) LIKE LOWER(?)");
//            }
//
//            PreparedStatement ps = conn.prepareStatement(queryBuilder.toString());
//
//            int index = 1;
//            if (title != null && !title.isEmpty()) {
//                ps.setString(index++, "%" + title + "%");
//            }
//            if (year != null && !year.isEmpty()) {
//                ps.setString(index++, year);
//            }
//            if (director != null && !director.isEmpty()) {
//                ps.setString(index++, "%" + director + "%");
//            }
//            if (star != null && !star.isEmpty()) {
//                ps.setString(index++, "%" + star + "%");
//            }
//
//            ResultSet rs = ps.executeQuery();
//            while (rs.next()) {
//                movies.add(new Movie(rs.getString("id"), rs.getString("title"),
//                        rs.getInt("year"), rs.getString("director")));
//            }
//
//            rs.close();
//            ps.close();
//
//            response.getWriter().write(new Gson().toJson(movies));
//            response.setStatus(200);
//        } catch (Exception e) {
//            response.setStatus(500);
//            response.getWriter().write("{\"errorMessage\": \"" + e.getMessage() + "\"}");
//        }
//    }

    private void getGenres(HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Fetch genres from database
        try (Connection conn = dbManager.getConnection("READ")) {
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

//    private void getMoviesByGenre(String genreId, HttpServletResponse response) throws IOException {
//        response.setContentType("application/json"); // Response mime type
//
//        // Output stream to STDOUT
//        PrintWriter out = response.getWriter();
//
//        // Fetch movies by genre from database
//        try (Connection conn = dataSource.getConnection()) {
//            String query = "SELECT m.id, m.title, m.year, m.director FROM movies m " +
//                    "JOIN genres_in_movies gim ON m.id = gim.movieId " +
//                    "WHERE gim.genreId = ?";
//            PreparedStatement statement = conn.prepareStatement(query);
//            statement.setString(1, genreId);
//            ResultSet rs = statement.executeQuery();
//
//            List<Movie> movies = new ArrayList<>();
//            while (rs.next()) {
//                movies.add(new Movie(rs.getString("id"), rs.getString("title"), rs.getInt("year"), rs.getString("director")));
//            }
//
//            rs.close();
//            statement.close();
//
//            // Send genres as JSON response
//            out.write(new Gson().toJson(movies));
//            // Set response status to 200 (OK)
//            response.setStatus(200);
//        } catch (Exception e) {
//            // Write error message JSON object to output
//            JsonObject jsonObject = new JsonObject();
//            jsonObject.addProperty("errorMessage", e.getMessage());
//            out.write(jsonObject.toString());
//
//            // Set response status to 500 (Internal Server Error)
//            response.setStatus(500);
//        } finally {
//            out.close();
//        }
//    }
//
//    private void getMoviesByTitle(String letter, HttpServletResponse response) throws IOException {
//        response.setContentType("application/json"); // Response mime type
//
//        // Output stream to STDOUT
//        PrintWriter out = response.getWriter();
//
//        // Fetch movies by title from database
//        try (Connection conn = dataSource.getConnection()) {
//            String query = "SELECT id, title, year, director FROM movies " +
//                    "WHERE title LIKE ? ORDER BY title";
//            PreparedStatement statement;
//
//            // Handle "*" character for non-alphanumeric titles
//            if (letter.equals("*")) {
//                query = "SELECT id, title, year, director FROM movies " +
//                        "WHERE title REGEXP '^[^a-zA-Z0-9]' ORDER BY title";
//                statement = conn.prepareStatement(query);
//            } else {
//                statement = conn.prepareStatement(query);
//                statement.setString(1, letter + "%");
//            }
//
//            ResultSet rs = statement.executeQuery();
//            List<Movie> movies = new ArrayList<>();
//            while (rs.next()) {
//                movies.add(new Movie(rs.getString("id"), rs.getString("title"), rs.getInt("year"), rs.getString("director")));
//            }
//
//            rs.close();
//            statement.close();
//
//            // Send genres as JSON response
//            out.write(new Gson().toJson(movies));
//            // Set response status to 200 (OK)
//            response.setStatus(200);
//        } catch (Exception e) {
//            // Write error message JSON object to output
//            JsonObject jsonObject = new JsonObject();
//            jsonObject.addProperty("errorMessage", e.getMessage());
//            out.write(jsonObject.toString());
//
//            // Set response status to 500 (Internal Server Error)
//            response.setStatus(500);
//        } finally {
//            out.close();
//        }
//    }

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