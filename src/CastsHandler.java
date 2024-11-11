import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class CastsHandler extends DefaultHandler {

    private static int actorCounter = 1;
    private Set<String> actorCache;
    private Connection conn;
    private int errorCount = 0;
    private int duplicateCount = 0;
    private int insertActors = 0;
    private int movieNotFoundCount = 0;

    private String currentElement = "";
    private String movieId = null;
    private String movieTitle = null;
    private String actorName = null;

    private FileWriter detailedLogWriter;
    private FileWriter summaryLogWriter;

    public CastsHandler(Connection conn, String detailedLogFilePath, String summaryLogFilePath) throws IOException {
        this.conn = conn;
        this.actorCache = new HashSet<>();
        this.detailedLogWriter = new FileWriter(detailedLogFilePath, true);
        this.summaryLogWriter = new FileWriter(summaryLogFilePath, true);
    }

    private String generateActorId() {
        String id = "xml" + String.format("%07d", actorCounter);
        actorCounter++;
        return id;
    }

    private boolean isDuplicate(String actorName) {
        return actorCache.contains(actorName);
    }

    private void insertActorIntoDatabase(String actorId, String actorName) throws SQLException {
        String query = "INSERT INTO stars (id, name) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, actorId);
            stmt.setString(2, actorName);
            stmt.executeUpdate();
            actorCache.add(actorName);
        }
    }

    private void insertActorMovieRelation(String actorId, String movieId) throws SQLException {
        String query = "INSERT INTO stars_in_movies (starId, movieId) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, actorId);
            stmt.setString(2, movieId);
            stmt.executeUpdate();
        }
    }

    private boolean movieExists(String movieId) throws SQLException {
        String query = "SELECT COUNT(*) FROM movies WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, movieId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        currentElement = qName;
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if (qName.equals("m")) {
            try {
                if (movieId == null || movieId.isEmpty() || movieTitle == null || movieTitle.isEmpty() || actorName == null || actorName.isEmpty()) {
                    errorCount++;
                    detailedLogWriter.write("Skipping entry due to missing data: " + actorName + "\n");
                } else {
                    if (!movieExists(movieId)) {
                        movieNotFoundCount++;
                        detailedLogWriter.write("Movie not found in database: " + movieTitle + "\n");
                    } else {
                        if (isDuplicate(actorName)) {
                            duplicateCount++;
                            detailedLogWriter.write("Duplicate actor skipped: " + actorName + "\n");
                        } else {
                            String actorId = generateActorId();
                            insertActorIntoDatabase(actorId, actorName);
                            insertActorMovieRelation(actorId, movieId);
                            detailedLogWriter.write("Inserted actor and movie relation: " + actorName + " in " + movieTitle + "\n");
                            insertActors++;
                        }
                    }
                }
            } catch (Exception e) {
                errorCount++;
                try {
                    detailedLogWriter.write("Error processing entry: " + actorName + " in movie " + movieTitle + " - " + e.getMessage() + "\n");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
            movieId = movieTitle = actorName = null;
        }
        currentElement = "";
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        String content = new String(ch, start, length).trim();
        if (content.isEmpty()) return;

        switch (currentElement) {
            case "f":
                movieId = content;
                break;
            case "t":
                movieTitle = content;
                break;
            case "a":
                actorName = content;
                break;
        }
    }

    public int getErrorCount() {
        return errorCount;
    }

    public int getDuplicateCount() {
        return duplicateCount;
    }

    public int getInsertActors() {
        return insertActors;
    }

    public int getMovieNotFoundCount() {
        return movieNotFoundCount;
    }

    public void closeConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            try {
                detailedLogWriter.write("Error closing database connection: " + e.getMessage() + "\n");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public void logSummary() throws IOException {
        summaryLogWriter.write("Parsing completed!\n");
        summaryLogWriter.write("Actors Inserted: " + getInsertActors() + "\n");
        summaryLogWriter.write("Errors encountered: " + getErrorCount() + "\n");
        summaryLogWriter.write("Duplicates skipped: " + getDuplicateCount() + "\n");
        summaryLogWriter.write("Movies not found: " + getMovieNotFoundCount() + "\n");
    }

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb", "mytestuser", "My6$Password")) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();

            String detailedLogFilePath = "CastsInfo.txt";
            String summaryLogFilePath = "parseSummary.txt";

            CastsHandler handler = new CastsHandler(conn, detailedLogFilePath, summaryLogFilePath);

            File xmlFile = new File("data/xml/casts124.xml");
            parser.parse(xmlFile, handler);

            handler.logSummary();

            handler.detailedLogWriter.close();
            handler.summaryLogWriter.close();
            handler.closeConnection();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
