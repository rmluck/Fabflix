import javax.sql.DataSource;
import java.sql.Connection;

public class DatabaseConnectionManager {

    private final DataSource masterDataSource;
    private final DataSource slaveDataSource;

    public DatabaseConnectionManager(DataSource masterDataSource, DataSource slaveDataSource) {
        this.masterDataSource = masterDataSource;
        this.slaveDataSource = slaveDataSource;
    }

    public Connection getConnection(String queryType) throws Exception {
        if ("WRITE".equalsIgnoreCase(queryType)) {
            return masterDataSource.getConnection();
        } else {
            return slaveDataSource.getConnection();
        }
    }
}
