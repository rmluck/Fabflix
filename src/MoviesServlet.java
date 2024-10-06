import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
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
import java.sql.Statement;

@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbexample");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT m.id, m.title, m.year, m.director, r.rating, " +
                    "(SELECT GROUP_CONCAT(DISTINCT g.name ORDER BY g.name) FROM genres_in_movies AS gimov " +
                    "JOIN genres AS g ON gimov.genreId = g.id WHERE gimov.movieId = m.id LIMIT 3) AS genres, " +
                    "(SELECT GROUP_CONCAT(DISTINCT s.name ORDER BY s.name) FROM stars_in_movies AS simov " +
                    "JOIN stars AS s ON simov.starId = s.id WHERE simov.movieId = m.id LIMIT 3) AS stars " +
                    "FROM movies AS m " +
                    "JOIN ratings AS r ON m.id = r.movieId " +
                    "GROUP BY m.id, r.rating " +
                    "ORDER BY r.rating DESC " +
                    "LIMIT 20;";

            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            JsonArray array = new JsonArray();

            while (rs.next()) {
                String movie_id = rs.getString("id");
                String title = rs.getString("title");
                String year = rs.getString("year");
                String director = rs.getString("director");
                String rating = rs.getString("rating");
                String genres = rs.getString("genres");
                String stars = rs.getString("stars");

                JsonObject object = new JsonObject();
                object.addProperty("movie_id", movie_id);
                object.addProperty("title", title);
                object.addProperty("year", year);
                object.addProperty("director", director);
                object.addProperty("rating", rating);
                object.addProperty("genres", genres);
                object.addProperty("stars", stars);

                array.add(object);
            }

            rs.close();
            ps.close();

            request.getServletContext().log("getting " + array.size() + " results");

            out.write(array.toString());
            response.setStatus(200);
        } catch (Exception e) {
            JsonObject object = new JsonObject();
            object.addProperty("errorMessage", e.getMessage());
            out.write(object.toString());

            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}
