import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class CastsHandler extends DefaultHandler {

    private static int actorCounter = 1; // Initialize counter for actor IDs
    private Set<String> actorCache; // Cache to track actors already seen
    private Connection conn; // Database connection
    private int errorCount = 0; // Track number of errors
    private int duplicateCount = 0; // Track duplicates skipped
    private int insertActors = 0;
    private int movieNotFoundCount = 0; // Track movies not found

    private String currentElement = "";
    private String movieId = null;
    private String movieTitle = null;
    private String actorName = null;

    private FileWriter logWriter; // FileWriter for logging

    // Constructor to initialize the database connection and log file
    public CastsHandler(Connection conn, String logFilePath) throws IOException {
        this.conn = conn;
        this.actorCache = new HashSet<>();
        this.logWriter = new FileWriter(logFilePath, true); // Open the log file in append mode
    }

    // Method to generate unique actor IDs in the format "xmlXXXXXXX"
    private String generateActorId() {
        String id = "xml" + String.format("%07d", actorCounter); // Format with leading zeros
        actorCounter++; // Increment for the next actor
        return id;
    }

    // Method to check if the actor is a duplicate based on actor name
    private boolean isDuplicate(String actorName) {
        return actorCache.contains(actorName); // Cache check for duplicate
    }

    // Method to insert actor data into the database
    private void insertActorIntoDatabase(String actorId, String actorName) throws SQLException {
        String query = "INSERT INTO stars (id, name) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, actorId);
            stmt.setString(2, actorName);
            stmt.executeUpdate();
            actorCache.add(actorName); // Add the actor name to the cache
        }
    }

    // Method to insert the relationship between actor and movie into stars_in_movies table
    private void insertActorMovieRelation(String actorId, String movieId) throws SQLException {
        String query = "INSERT INTO stars_in_movies (starId, movieId) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, actorId);
            stmt.setString(2, movieId);
            stmt.executeUpdate();
        }
    }

    // Method to check if the movie exists in the database
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

    // SAX event when an element starts
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        currentElement = qName; // Set current element name
    }

    // SAX event when an element ends
    @Override
    public void endElement(String uri, String localName, String qName) {
        if (qName.equals("m")) {
            try {
                // Skip entries with missing data
                if (movieId == null || movieId.isEmpty() || movieTitle == null || movieTitle.isEmpty() || actorName == null || actorName.isEmpty()) {
                    errorCount++;
                    logWriter.write("Skipping entry due to missing data: " + actorName + "\n");
                } else {
                    // Check if the movie exists
                    if (!movieExists(movieId)) {
                        movieNotFoundCount++;
                        logWriter.write("Movie not found in database: " + movieTitle + "\n");
                    } else {
                        // Check if the actor is a duplicate
                        if (isDuplicate(actorName)) {
                            duplicateCount++;
                            logWriter.write("Duplicate actor skipped: " + actorName + "\n");
                        } else {
                            // Generate a unique actor ID and insert the actor into the database
                            String actorId = generateActorId();
                            insertActorIntoDatabase(actorId, actorName);

                            // Insert the relationship between the actor and movie
                            insertActorMovieRelation(actorId, movieId);
                            logWriter.write("Inserted actor and movie relation: " + actorName + " in " + movieTitle + "\n");
                            insertActors++;
                        }
                    }
                }
            } catch (Exception e) {
                errorCount++;
                try {
                    logWriter.write("Error processing entry: " + actorName + " in movie " + movieTitle + " - " + e.getMessage() + "\n");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
            // Reset for next movie
            movieId = movieTitle = actorName = null;
        }
        currentElement = "";
    }

    // SAX event when parsing characters within an element
    @Override
    public void characters(char[] ch, int start, int length) {
        String content = new String(ch, start, length).trim();
        if (content.isEmpty()) return; // Skip empty content

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

    // Getter methods to check counts of errors, duplicates, and inserted actors
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

    // Main method to run the CastsHandler directly
    public static void main(String[] args) {
        // Database connection setup
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb", "mytestuser", "My6$Password")) {
            // Initialize the SAX parser and handler
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();

            // Specify the path to the log file
            String logFilePath = "CastsInfo.txt";

            // Create an instance of the handler with file logging
            CastsHandler handler = new CastsHandler(conn, logFilePath);

            // Parse the XML file
            File xmlFile = new File("data/xml/casts124.xml");
            parser.parse(xmlFile, handler);

            // Output summary of errors and duplicates
            try (FileWriter summaryWriter = new FileWriter(logFilePath, true)) {
                summaryWriter.write("Parsing completed!\n");
                summaryWriter.write("Actors Inserted: " + handler.getInsertActors() + "\n");
                summaryWriter.write("Errors encountered: " + handler.getErrorCount() + "\n");
                summaryWriter.write("Duplicates skipped: " + handler.getDuplicateCount() + "\n");
                summaryWriter.write("Movies not found: " + handler.getMovieNotFoundCount() + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
