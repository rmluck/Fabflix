import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;

public class DatabaseConnectionManager {
    private DataSource masterDataSource;
    private DataSource slaveDataSource;

    public DatabaseConnectionManager() throws Exception {
        InitialContext ctx = new InitialContext();
        this.masterDataSource = (DataSource) ctx.lookup("java:comp/env/jdbc/MasterDB");
        this.slaveDataSource = (DataSource) ctx.lookup("java:comp/env/jdbc/SlaveDB");
    }

    public Connection getConnection(String queryType) throws Exception {
        if (queryType.equalsIgnoreCase("WRITE")) {
            return masterDataSource.getConnection();
        } else {
            return slaveDataSource.getConnection();
        }
    }
}
