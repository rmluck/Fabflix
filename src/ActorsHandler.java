import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ActorsHandler extends DefaultHandler {

    private static final Logger logger = Logger.getLogger(ActorsHandler.class.getName());
    private static int actorCounter = 1; // Initialize counter for actor IDs
    private Set<String> actorCache; // Cache to track actors already seen
    private Connection conn; // Database connection
    private int errorCount = 0; // Track number of errors
    private int duplicateCount = 0; // Track duplicates skipped
    private int insertActors = 0;

    private String currentElement = "";
    private String stagename = null;
    private String dob = null;

    // Constructor to initialize the database connection
    public ActorsHandler(Connection conn) {
        this.conn = conn;
        this.actorCache = new HashSet<>();
    }

    // Method to generate unique actor IDs in the format "xmlXXXXXXX"
    private String generateActorId() {
        String id = "xml" + String.format("%07d", actorCounter); // Format with leading zeros
        actorCounter++; // Increment for the next actor
        return id;
    }

    // Method to check if the actor is a duplicate based on actor ID and stagename
    private boolean isDuplicate(String actorId, String stagename) {
        return actorCache.contains(actorId) || actorCache.contains(stagename); // Cache check for duplicate
    }

    // Method to insert actor data into the database
    private void insertActorIntoDatabase(String actorId, String stagename, String dob) throws SQLException {
        String query = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, actorId);
            stmt.setString(2, stagename);
            stmt.setInt(3, dob != null ? Integer.parseInt(dob) : null);
            stmt.executeUpdate();
            actorCache.add(actorId); // Add the actor ID to the cache
            actorCache.add(stagename); // Add the stagename to the cache
        }
    }

    // SAX event when an element starts
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        currentElement = qName; // Set current element name
    }

    // SAX event when an element ends
    @Override
    public void endElement(String uri, String localName, String qName) {
        if (qName.equals("actor")) {
            try {
                // Skip actors with missing data
                if (stagename == null || stagename.isEmpty()) {
                    errorCount++;
                    logger.warning("Skipping actor due to missing data: " + stagename);
                } else {
                    // Generate a unique actor ID in the format "xmlXXXXXXX"
                    String actorId = generateActorId();

                    // Check for duplicates in the database or cache
                    if (isDuplicate(actorId, stagename)) {
                        duplicateCount++;
                        logger.info("Duplicate actor skipped: " + stagename);
                    } else {
                        // Insert the actor into the database
                        insertActorIntoDatabase(actorId, stagename, dob);
                        logger.info("Inserted actor: " + stagename);
                        insertActors++;
                    }
                }
            } catch (Exception e) {
                errorCount++;
                logger.severe("Error processing actor: " + stagename + " - " + e.getMessage());
            }
            // Reset for next actor
            stagename = dob = null;
        }
        currentElement = "";
    }

    // SAX event when parsing characters within an element
    @Override
    public void characters(char[] ch, int start, int length) {
        String content = new String(ch, start, length).trim();
        if (content.isEmpty()) return; // Skip empty content

        switch (currentElement) {
            case "stagename":
                stagename = content;
                break;
            case "dob":
                dob = content;
                break;
        }
    }

    // Getter methods to check counts of errors and duplicates
    public int getErrorCount() {
        return errorCount;
    }

    public int getDuplicateCount() {
        return duplicateCount;
    }

    public int getInsertActors() {
        return insertActors;
    }

    // Main method to run the ActorsHandler directly
    public static void main(String[] args) {
        try {
            // Set up the logger to log to both console and file
            Logger logger = Logger.getLogger(ActorsHandler.class.getName());

            // Create a FileHandler that logs to ActorsInfo.txt
            FileHandler fileHandler = new FileHandler("ActorsInfo.txt", true); // Append to the file
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.INFO); // Set the log level to INFO
            logger.addHandler(fileHandler);

            // Optionally, also log to the console
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.INFO);
            logger.addHandler(consoleHandler);

            // Database connection setup
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb", "mytestuser", "My6$Password")) {
                // Initialize the SAX parser and handler
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser = factory.newSAXParser();

                // Create an instance of the handler
                ActorsHandler handler = new ActorsHandler(conn);

                // Parse the XML file
                File xmlFile = new File("data/xml/actors63.xml");
                parser.parse(xmlFile, handler);

                // Output summary of errors and duplicates
                logger.info("Parsing completed!");
                logger.info("Actors Inserted: " + handler.getInsertActors());
                logger.info("Errors encountered: " + handler.getErrorCount());
                logger.info("Duplicates skipped: " + handler.getDuplicateCount());

            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
