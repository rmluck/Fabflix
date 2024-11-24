import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@WebListener
public class DatabaseConnectionManagerListener implements ServletContextListener {

    private DatabaseConnectionManager dbManager;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            InitialContext ctx = new InitialContext();
            DataSource masterDataSource = (DataSource) ctx.lookup("java:comp/env/jdbc/masterMoviedb");
            DataSource slaveDataSource = (DataSource) ctx.lookup("java:comp/env/jdbc/slaveMoviedb");

            dbManager = new DatabaseConnectionManager(masterDataSource, slaveDataSource);

            System.out.println("DatabaseConnectionManager initialized successfully.");

            ServletContext context = sce.getServletContext();
            context.setAttribute("DatabaseConnectionManager", dbManager);

        } catch (NamingException e) {
            throw new RuntimeException("Failed to initialize DatabaseConnectionManager", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("DatabaseConnectionManagerListener destroyed.");
    }
}
