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
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ActorsHandler extends DefaultHandler {

    private static final Logger logger = Logger.getLogger(ActorsHandler.class.getName());
    private static int actorCounter = 1;
    private Set<String> actorCache;
    private Connection conn;
    private int errorCount = 0;
    private int duplicateCount = 0;
    private int insertActors = 0;

    private String currentElement = "";
    private String stagename = null;
    private String dob = null;

    public ActorsHandler(Connection conn) {
        this.conn = conn;
        this.actorCache = new HashSet<>();
    }

    private String generateActorId() {
        String id = "xml" + String.format("%07d", actorCounter);
        actorCounter++;
        return id;
    }

    private boolean isDuplicate(String actorId, String stagename) {
        return actorCache.contains(actorId) || actorCache.contains(stagename);
    }

    // Method to insert actor data into the database
    private void insertActorIntoDatabase(String actorId, String stagename, String dob) throws SQLException {
        String query = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, actorId);
            stmt.setString(2, stagename);
            stmt.setInt(3, dob != null ? Integer.parseInt(dob) : null);
            stmt.executeUpdate();
            actorCache.add(actorId);
            actorCache.add(stagename);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        currentElement = qName;
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if (qName.equals("actor")) {
            try {
                if (stagename == null || stagename.isEmpty()) {
                    errorCount++;
                    logger.warning("Skipping actor due to missing data: " + stagename);
                } else {

                    String actorId = generateActorId();

                    if (isDuplicate(actorId, stagename)) {
                        duplicateCount++;
                        logger.info("Duplicate actor skipped: " + stagename);
                    } else {
                        insertActorIntoDatabase(actorId, stagename, dob);
                        logger.info("Inserted actor: " + stagename);
                        insertActors++;
                    }
                }
            } catch (Exception e) {
                errorCount++;
                logger.severe("Error processing actor: " + stagename + " - " + e.getMessage());
            }
            stagename = dob = null;
        }
        currentElement = "";
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        String content = new String(ch, start, length).trim();
        if (content.isEmpty()) return;

        switch (currentElement) {
            case "stagename":
                stagename = content;
                break;
            case "dob":
                dob = content;
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

    public static void main(String[] args) {
        try {
            Logger logger = Logger.getLogger(ActorsHandler.class.getName());

            FileHandler fileHandler = new FileHandler("ActorsInfo.txt", true); // Append to the file
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.INFO);
            logger.addHandler(fileHandler);

            logger.setUseParentHandlers(false);

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb", "mytestuser", "My6$Password")) {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser = factory.newSAXParser();

                ActorsHandler handler = new ActorsHandler(conn);

                File xmlFile = new File("data/xml/actors63.xml");
                parser.parse(xmlFile, handler);

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
