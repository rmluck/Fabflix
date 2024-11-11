import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.HashSet;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class MainsHandler extends DefaultHandler {

    private static final Logger logger = Logger.getLogger(MainsHandler.class.getName());
    private String currentElement;
    private String fid, title, year, director, currentGenre;
    private int insertedCount = 0;
    private int duplicateCount = 0;
    private int errorCount = 0;
    private int insertedGenreCount = 0;
    private int insertedGenreInMoviesCount = 0;
    private HashSet<String> movieCache = new HashSet<>();
    private HashSet<String> genreCache = new HashSet<>();
    private Connection conn;

    public MainsHandler() {
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb", "mytestuser", "My6$Password");

            Logger rootLogger = Logger.getLogger("");
            for (var handler : rootLogger.getHandlers()) {
                rootLogger.removeHandler(handler);
            }

            FileHandler fh = new FileHandler("MovieInfo.txt", true);
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);

            loadGenres();

        } catch (IOException | SQLException e) {
            logger.severe("Error setting up database connection or logger: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            MainsHandler handler = new MainsHandler();
            saxParser.parse("data/xml/mains243.xml", handler);
            handler.logSummary();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        currentElement = qName;
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        String data = new String(ch, start, length).trim();

        if (currentElement.equals("fid")) {
            fid = data;
        } else if (currentElement.equals("t")) {
            title = data;
        } else if (currentElement.equals("year")) {
            year = data;
        } else if (currentElement.equals("dirn")) {
            director = data;
        } else if (currentElement.equals("cat")) {
            currentGenre = data;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if (qName.equals("film")) {
            if (fid == null || fid.isEmpty() || title == null || title.isEmpty() ||
                    year == null || year.isEmpty() || director == null || director.isEmpty()) {
                errorCount++;
                logger.severe("Error: Missing required fields for movie record. Skipped. fid: " + fid + " title: " + title + " year: " + year + " director: " + director);
            } else {
                try {
                    if (isDuplicate(fid, title, year, director)) {
                        duplicateCount++;
                        logger.info("Duplicate movie skipped: " + title);
                    } else {
                        insertMovieIntoDatabase(fid, title, year, director);
                    }
                } catch (Exception e) {
                    errorCount++;
                    logger.severe("Error processing movie: " + title + " - " + e.getMessage());
                }
            }
            fid = title = year = director = null;
        } else if (qName.equals("cat") && currentGenre != null) {
            addGenreToMovie(currentGenre, fid);
            currentGenre = null;
        }
        currentElement = "";
    }

    private boolean isDuplicate(String id, String title, String year, String director) {
        String uniqueKey = id + title + year + director;

        if (movieCache.contains(uniqueKey)) {
            return true;
        }

        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM movies WHERE id = ? AND title = ? AND year = ? AND director = ?")) {

            pstmt.setString(1, id);
            pstmt.setString(2, title);
            pstmt.setInt(3, Integer.parseInt(year));
            pstmt.setString(4, director);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    movieCache.add(uniqueKey);
                    return true;
                }
            }
        } catch (SQLException e) {
            logger.severe("Error checking duplicate movie: " + title + " - " + e.getMessage());
        }
        return false;
    }

    private void insertMovieIntoDatabase(String id, String title, String year, String director) {
        String insertMovieSql = "INSERT INTO movies (id, title, year, director) VALUES (?, ?, ?, ?)";
        String insertRatingSql = "INSERT INTO ratings (movieId, rating, numVotes) VALUES (?, 0, 0)";

        try (PreparedStatement movieStmt = conn.prepareStatement(insertMovieSql);
             PreparedStatement ratingStmt = conn.prepareStatement(insertRatingSql)) {

            movieStmt.setString(1, id);
            movieStmt.setString(2, title);
            movieStmt.setInt(3, Integer.parseInt(year));
            movieStmt.setString(4, director);
            movieStmt.executeUpdate();

            ratingStmt.setString(1, id);
            ratingStmt.executeUpdate();

            insertedCount++;
            movieCache.add(id + title + year + director);
            logger.info("Inserted movie: " + title);

        } catch (SQLException e) {
            errorCount++;
            logger.severe("Error inserting movie or rating for: " + title + " - " + e.getMessage());
        }
    }

    private void loadGenres() throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM genres")) {
            while (rs.next()) {
                genreCache.add(rs.getString("name"));
            }
        }
    }

    private void addGenreToMovie(String genreName, String movieId) {
        try {
            if (!movieExists(movieId)) {
                logger.severe("Movie not found for genre assignment: " + movieId);
                return;
            }

            if (!genreCache.contains(genreName)) {
                int genreId = insertGenre(genreName);
                genreCache.add(genreName);
                insertGenreInMovie(genreId, movieId);
                insertedGenreCount++;
            } else {
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "SELECT id FROM genres WHERE name = ?")) {
                    pstmt.setString(1, genreName);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            insertGenreInMovie(rs.getInt("id"), movieId);
                        }
                    }
                }
            }
            insertedGenreInMoviesCount++;
        } catch (SQLException e) {
            errorCount++;
            logger.severe("Error adding genre to movie: " + genreName + " - " + e.getMessage());
        }
    }

    private int insertGenre(String genreName) throws SQLException {
        String sql = "INSERT INTO genres (name) VALUES (?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, genreName);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to retrieve genre ID for: " + genreName);
    }

    private void insertGenreInMovie(int genreId, String movieId) throws SQLException {
        String sql = "INSERT INTO genres_in_movies (genreId, movieId) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, genreId);
            pstmt.setString(2, movieId);
            pstmt.executeUpdate();
        }
    }

    private boolean movieExists(String movieId) throws SQLException {
        String query = "SELECT COUNT(*) FROM movies WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, movieId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private void logSummary() {
        logger.info("Parsing completed.");
        logger.info("Movies inserted: " + insertedCount);
        logger.info("Duplicates skipped: " + duplicateCount);
        logger.info("Errors encountered: " + errorCount);
        logger.info("Genres inserted: " + insertedGenreCount);
        logger.info("Genres_in_movies inserted: " + insertedGenreInMoviesCount);

        try (PrintWriter summaryWriter = new PrintWriter(new FileWriter("parseSummary.txt", true))) {
            summaryWriter.println("Movies inserted: " + insertedCount);
            summaryWriter.println("Duplicates skipped: " + duplicateCount);
            summaryWriter.println("Errors encountered: " + errorCount);
            summaryWriter.println("Genres inserted: " + insertedGenreCount);
            summaryWriter.println("Genres_in_movies inserted: " + insertedGenreInMoviesCount);
        } catch (IOException e) {
            logger.severe("Error writing summary: " + e.getMessage());
        }
    }
}
