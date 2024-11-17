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
import jakarta.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
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
        HttpSession session = request.getSession();

        response.setContentType("application/json");
        String title = request.getParameter("title");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String star = request.getParameter("star");
        String genreId = request.getParameter("genreId");
        String letter = request.getParameter("letter");
        String sortParameters = request.getParameter("sortOptions");
        String moviesPerPage = request.getParameter("moviesPerPage");
        String page = request.getParameter("page");

        int currentPage = (page != null && !page.isEmpty()) ? Integer.parseInt(page) : 1;
        int offset = (currentPage - 1) * Integer.parseInt(moviesPerPage);

        String[] sortOptions = sortParameters.split(",");
        List<String> allowedSortOptions = Arrays.asList("m.title ASC", "m.title DESC", "r.rating ASC", "r.rating DESC");

//        for (String option : sortOptions) {
//            if (!allowedSortOptions.contains(option.trim())) {
//                throw new IllegalArgumentException("Invalid sort option: " + option);
//            }
//        }

        session.setAttribute("title", title);
        session.setAttribute("year", year);
        session.setAttribute("director", director);
        session.setAttribute("star", star);
        session.setAttribute("genreId", genreId);
        session.setAttribute("letter", letter);
        session.setAttribute("sortOptions", sortParameters);
        session.setAttribute("moviesPerPage", moviesPerPage);
        session.setAttribute("page", page);

        String action = request.getParameter("action");
        switch (action) {
            case "searchMovies":
                System.out.println(title);
                searchMovies(title, year, director, star, sortParameters, moviesPerPage, String.valueOf(offset), response);
                break;
            case "getMoviesByGenre":
                getMoviesByGenre(genreId, sortParameters, moviesPerPage, String.valueOf(offset), response);
                break;
            case "getMoviesByTitle":
                getMoviesByTitle(letter, sortParameters, moviesPerPage, String.valueOf(offset), response);
                break;
        }
    }

    private void searchMovies(String title, String year, String director, String star, String sortParameters, String limit, String offset, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT m.id, m.title, m.year, m.director, r.rating, " +
                    "(SELECT GROUP_CONCAT(CONCAT(three_genres.id, ':', three_genres.name) SEPARATOR ',') " +
                    " FROM (SELECT g.id, g.name " +
                    " FROM genres_in_movies AS gimov " +
                    " JOIN genres AS g ON gimov.genreId = g.id " +
                    " WHERE gimov.movieId = m.id " +
                    " ORDER BY g.name LIMIT 3) AS three_genres) AS genres, " +
                    "(SELECT GROUP_CONCAT(CONCAT(three_stars.id, ':', three_stars.name) SEPARATOR ',') " +
                    " FROM (SELECT s.id, s.name " +
                    " FROM stars_in_movies AS simov " +
                    " JOIN stars AS s ON simov.starId = s.id " +
                    " JOIN (SELECT starId, COUNT(movieId) AS movie_count " +
                    " FROM stars_in_movies " +
                    " GROUP BY starId) AS star_counts ON s.id = star_counts.starId " +
                    " WHERE simov.movieId = m.id " +
                    " ORDER BY star_counts.movie_count DESC, s.name ASC LIMIT 3) AS three_stars) AS stars, " +
                    " (SELECT COUNT(*) " +
                    " FROM (SELECT DISTINCT m_inner.id " +
                    " FROM movies AS m_inner " +
                    " JOIN ratings AS r_inner ON m_inner.id = r_inner.movieId ";

            int subcount = 0;
            if (title != null && !title.isEmpty()) {
//                query += " WHERE LOWER(m_inner.title) LIKE LOWER(?) ";
//                subcount = 1;
                query += " WHERE ";
                String[] tokens = title.split("\\s+");
                for (int i = 0; i < tokens.length; i++) {
                    if (i > 0) {
                        query += " AND ";
                    }
                    query += "LOWER(m_inner.title) LIKE LOWER(?) ";
                }
                subcount = 1;
            }
            if (year != null && !year.isEmpty()) {
                if (subcount == 1) {
                    query += " WHERE LOWER(m_inner.year) = LOWER(?) ";
                    subcount = 1;
                } else {
                    query += " AND LOWER(m_inner.year) = LOWER(?) ";
                }
            }
            if (director != null && !director.isEmpty()) {
                if (subcount == 1) {
                    query += " WHERE LOWER(m_inner.director) = LOWER(?) ";
                    subcount = 1;
                } else{
                    query += " AND LOWER(m_inner.director) LIKE LOWER(?) ";
                }
            }
            if (star != null && !star.isEmpty()) {
                if (subcount == 1) {
                    query += " WHERE EXISTS (SELECT 1 FROM stars_in_movies AS simov " +
                        " JOIN stars AS s ON simov.starId = s.id " +
                        " WHERE simov.movieId = m.id AND LOWER (s.name) LIKE LOWER(?))";
                    subcount = 1;
                } else {
                    query += " AND EXISTS (SELECT 1 FROM stars_in_movies AS simov " +
                        " JOIN stars AS s ON simov.starId = s.id " +
                        " WHERE simov.movieId = m.id AND LOWER (s.name) LIKE LOWER(?))";
                }
            }
            query += ") AS filtered_movies " +
                    ") AS total_records " +
                    " FROM movies AS m " +
                    " JOIN ratings AS r on m.id = r.movieId " +
                    " JOIN genres_in_movies AS gim ON m.id = gim.movieId " +
                    " WHERE 1=1 ";

            if (title != null && !title.isEmpty()) {
                String[] tokens = title.split("\\s+");
                for (int i = 0; i < tokens.length; i++) {
                    query += " AND LOWER(m.title) LIKE LOWER(?) ";
                }
            }
            if (year != null && !year.isEmpty()) {
                query += " AND LOWER(m.year) = LOWER(?) ";
            }
            if (director != null && !director.isEmpty()) {
                query += " AND LOWER(m.director) LIKE LOWER(?) ";
            }
            if (star != null && !star.isEmpty()) {
                query += " AND EXISTS (SELECT 1 FROM stars_in_movies AS simov " +
                        " JOIN stars AS s ON simov.starId = s.id " +
                        " WHERE simov.movieId = m.id AND LOWER (s.name) LIKE LOWER(?))";
            }
            query += " GROUP BY m.id, r.rating " +
                    " ORDER BY ";
            query += sortParameters;
            query += " LIMIT ? OFFSET ?;";

            PreparedStatement statement = conn.prepareStatement(query);

            int index = 1;
            if (title != null && !title.isEmpty()) {
//                statement.setString(index++, "%" + title + "%");
                String[] tokens = title.split("\\s+");
                for (String token : tokens) {
                    statement.setString(index++, "%" + token + "%");
                }
            }
            if (year != null && !year.isEmpty()) {
                statement.setString(index++, year);
            }
            if (director != null && !director.isEmpty()) {
                statement.setString(index++, "%" + director + "%");
            }
            if (star != null && !star.isEmpty()) {
                statement.setString(index++, "%" + star + "%");
            }
            if (title != null && !title.isEmpty()) {
//                statement.setString(index++, "%" + title + "%");
                String[] tokens = title.split("\\s+");
                for (String token : tokens) {
                    statement.setString(index++, "%" + token + "%");
                }
            }
            if (year != null && !year.isEmpty()) {
                statement.setString(index++, year);
            }
            if (director != null && !director.isEmpty()) {
                statement.setString(index++, "%" + director + "%");
            }
            if (star != null && !star.isEmpty()) {
                statement.setString(index++, "%" + star + "%");
            }
            statement.setInt(index++, Integer.parseInt(limit));
            statement.setInt(index++, Integer.parseInt(offset));

            System.out.println(statement);

            ResultSet rs = statement.executeQuery();

            List<Movie> movies = new ArrayList<>();
            int totalMovies = 0;
            while (rs.next()) {
                movies.add(new Movie(rs.getString("id"), rs.getString("title"),
                        rs.getInt("year"), rs.getString("director"),
                        rs.getString("genres"), rs.getString("stars"),
                        rs.getFloat("rating")));
                totalMovies = rs.getInt("total_records");
            }
            int totalPages = (int) Math.ceil((double) totalMovies / Integer.parseInt(limit));

            rs.close();
            statement.close();

            MoviesResponse moviesResponse = new MoviesResponse(movies, totalMovies, totalPages);

            // Send genres as JSON response
            out.write(new Gson().toJson(moviesResponse));
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

    private void getMoviesByGenre(String genreId, String sortParameters, String limit, String offset, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Fetch movies by genre from database
        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT m.id, m.title, m.year, m.director, r.rating, " +
                    "(SELECT GROUP_CONCAT(CONCAT(three_genres.id, ':', three_genres.name) SEPARATOR ',') " +
                    " FROM (SELECT g.id, g.name " +
                    " FROM genres_in_movies AS gimov " +
                    " JOIN genres AS g ON gimov.genreId = g.id " +
                    " WHERE gimov.movieId = m.id " +
                    " ORDER BY g.name LIMIT 3) AS three_genres) AS genres, " +
                    "(SELECT GROUP_CONCAT(CONCAT(three_stars.id, ':', three_stars.name) SEPARATOR ',') " +
                    " FROM (SELECT s.id, s.name " +
                    " FROM stars_in_movies AS simov " +
                    " JOIN stars AS s ON simov.starId = s.id " +
                    " JOIN (SELECT starId, COUNT(movieId) AS movie_count " +
                    " FROM stars_in_movies " +
                    " GROUP BY starId) AS star_counts ON s.id = star_counts.starId " +
                    " WHERE simov.movieId = m.id ORDER BY star_counts.movie_count DESC, s.name ASC LIMIT 3) AS three_stars) AS stars, " +
                    "(SELECT COUNT(*) FROM movies AS m_inner " +
                    " JOIN ratings AS r_inner ON m_inner.id = r_inner.movieId " +
                    " JOIN genres_in_movies AS gim_inner ON m_inner.id = gim_inner.movieId " +
                    " WHERE gim_inner.genreId = ?) AS total_records " +
                    " FROM movies AS m " +
                    " JOIN ratings AS r on m.id = r.movieId " +
                    " JOIN genres_in_movies AS gim on m.id = gim.movieId " +
                    " WHERE gim.genreId = ? " +
                    " GROUP BY m.id, r.rating " +
                    " ORDER BY " + sortParameters +
                    " LIMIT ? OFFSET ?;";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, genreId);
            statement.setString(2, genreId);
            statement.setInt(3, Integer.parseInt(limit));
            statement.setInt(4, Integer.parseInt(offset));

            ResultSet rs = statement.executeQuery();

            List<Movie> movies = new ArrayList<>();
            int totalMovies = 0;
            while (rs.next()) {
                movies.add(new Movie(rs.getString("id"), rs.getString("title"), rs.getInt("year"), rs.getString("director"), rs.getString("genres"), rs.getString("stars"), rs.getFloat("rating")));
                totalMovies = rs.getInt("total_records");
            }
            int totalPages = (int) Math.ceil((double) totalMovies / Integer.parseInt(limit));

            rs.close();
            statement.close();

            MoviesResponse moviesResponse = new MoviesResponse(movies, totalMovies, totalPages);

            // Send genres as JSON response
            out.write(new Gson().toJson(moviesResponse));
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

    private void getMoviesByTitle(String letter, String sortParameters, String limit, String offset, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Fetch movies by title from database
        try (Connection conn = dataSource.getConnection()) {
            String query;
            PreparedStatement statement;

            // Handle "*" character for non-alphanumeric titles
            if (letter.equals("*")) {
                query = "SELECT m.id, m.title, m.year, m.director, r.rating, " +
                    "(SELECT GROUP_CONCAT(CONCAT(three_genres.id, ':', three_genres.name) SEPARATOR ', ') " +
                    " FROM (SELECT g.id, g.name " +
                    " FROM genres_in_movies AS gimov " +
                    " JOIN genres AS g ON gimov.genreId = g.id " +
                    " WHERE gimov.movieId = m.id" +
                    " ORDER BY g.name LIMIT 3) AS three_genres) AS genres, " +
                    "(SELECT GROUP_CONCAT(CONCAT(three_stars.id, ':', three_stars.name) SEPARATOR ', ') " +
                    " FROM (SELECT s.id, s.name " +
                    " FROM stars_in_movies AS simov " +
                    " JOIN stars AS s on simov.starId = s.id " +
                    " JOIN (SELECT starId, COUNT(movieId) AS movie_count " +
                    " FROM stars_in_movies " +
                    " GROUP BY starId) AS star_counts ON s.id = star_counts.starId " +
                    " WHERE simov.movieId = m.id ORDER BY star_counts.movie_count DESC, s.name ASC LIMIT 3) AS three_stars) AS stars, " +
                    "(SELECT COUNT(*) FROM movies AS m_inner " +
                    " JOIN ratings AS r_inner ON m_inner.id = r_inner.movieId " +
                    " JOIN genres_in_movies AS gim_inner ON m_inner.id = gim_inner.movieId " +
                    " WHERE m_inner.title REGEXP '^[^a-zA-Z0-9]') AS total_records " +
                    " FROM movies AS m " +
                    " JOIN ratings AS r ON m.id = r.movieId " +
                    " JOIN genres_in_movies AS gim ON m.id = gim.movieId " +
                    " WHERE m.title REGEXP '^[^a-zA-Z0-9]' " +
                    " GROUP BY m.id, r.rating " +
                    " ORDER BY " + sortParameters +
                    " LIMIT ? OFFSET ?;";
                statement = conn.prepareStatement(query);
                statement.setInt(1, Integer.parseInt(limit));
                statement.setInt(2, Integer.parseInt(offset));
            } else {
                query = "SELECT m.id, m.title, m.year, m.director, r.rating, " +
                    "(SELECT GROUP_CONCAT(CONCAT(three_genres.id, ':', three_genres.name) SEPARATOR ', ') " +
                    " FROM (SELECT g.id, g.name " +
                    " FROM genres_in_movies AS gimov " +
                    " JOIN genres AS g ON gimov.genreId = g.id " +
                    " WHERE gimov.movieId = m.id" +
                    " ORDER BY g.name LIMIT 3) AS three_genres) AS genres, " +
                    "(SELECT GROUP_CONCAT(CONCAT(three_stars.id, ':', three_stars.name) SEPARATOR ', ') " +
                    " FROM (SELECT s.id, s.name " +
                    " FROM stars_in_movies AS simov " +
                    " JOIN stars AS s ON simov.starId = s.id " +
                    " JOIN (SELECT starId, COUNT(movieId) AS movie_count " +
                    " FROM stars_in_movies " +
                    " GROUP BY starId) AS star_counts ON s.id = star_counts.starId " +
                    " WHERE simov.movieId = m.id ORDER BY star_counts.movie_count DESC, s.name ASC LIMIT 3) AS three_stars) AS stars, " +
                    "(SELECT COUNT(*) FROM movies AS m_inner " +
                    " JOIN ratings AS r_inner ON m_inner.id = r_inner.movieId " +
                    " JOIN genres_in_movies AS gim_inner ON m_inner.id = gim_inner.movieId " +
                    " WHERE m_inner.title LIKE ?) AS total_records " +
                    " FROM movies AS m " +
                    " JOIN ratings AS r ON m.id = r.movieId " +
                    " JOIN genres_in_movies AS gim ON m.id = gim.movieId " +
                    " WHERE m.title LIKE ? " +
                    " GROUP BY m.id, r.rating" +
                    " ORDER BY " + sortParameters +
                    " LIMIT ? OFFSET ?;";
                statement = conn.prepareStatement(query);
                statement.setString(1, letter + "%");
                statement.setString(2, letter + "%");
                statement.setInt(3, Integer.parseInt(limit));
                statement.setInt(4, Integer.parseInt(offset));
            }

            ResultSet rs = statement.executeQuery();

            List<Movie> movies = new ArrayList<>();
            int totalMovies = 0;
            while (rs.next()) {
                movies.add(new Movie(rs.getString("id"), rs.getString("title"), rs.getInt("year"), rs.getString("director"), rs.getString("genres"), rs.getString("stars"), rs.getFloat("rating")));
                totalMovies = rs.getInt("total_records");
            }
            int totalPages = (int) Math.ceil((double) totalMovies / Integer.parseInt(limit));

            rs.close();
            statement.close();

            MoviesResponse moviesResponse = new MoviesResponse(movies, totalMovies, totalPages);
            // Send genres as JSON response
            out.write(new Gson().toJson(moviesResponse));
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