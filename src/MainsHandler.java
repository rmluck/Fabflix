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
    private String fid, title, year, director;
    private int insertedCount = 0;
    private int duplicateCount = 0;
    private int errorCount = 0;
    private HashSet<String> movieCache = new HashSet<>();
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
        String sql = "INSERT INTO movies (id, title, year, director) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, title);
            pstmt.setInt(3, Integer.parseInt(year));
            pstmt.setString(4, director);
            pstmt.executeUpdate();

            insertedCount++;
            movieCache.add(id + title + year + director);
            logger.info("Inserted movie: " + title);

        } catch (SQLException e) {
            errorCount++;
            logger.severe("Error inserting movie: " + title + " - " + e.getMessage());
        }
    }

    private void logSummary() {
        logger.info("Parsing completed.");
        logger.info("Movies inserted: " + insertedCount);
        logger.info("Duplicates skipped: " + duplicateCount);
        logger.info("Errors encountered: " + errorCount);

        try (PrintWriter out = new PrintWriter(new FileWriter("MovieInfo.txt", true))) {
            out.println("Parsing completed.");
            out.println("Movies inserted: " + insertedCount);
            out.println("Duplicates skipped: " + duplicateCount);
            out.println("Errors encountered: " + errorCount);
        } catch (IOException e) {
            logger.severe("Error writing summary to file: " + e.getMessage());
        }
    }

    public void closeConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            logger.severe("Error closing database connection: " + e.getMessage());
        }
    }
}
