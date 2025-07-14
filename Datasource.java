import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

public class Datasource {

    private static final String DB_HOST = System.getenv("DB_HOST"); // you would define this in some secure and common place (Example: oracle.company.com)
    private static final String DB_NAME = System.getenv("DB_NAME"); // DB name to connect to (Example: Transactions)
    private static final String DB_USER = System.getenv("DB_USER"); 
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD"); // You would use some token authentication here to get the pass (Example: Fidelius)
    private static final String TRUSTSTORE_PATH = System.getenv("DB_TRUSTSTORE_PATH"); // API to get the auth token
    private static final String TRUSTSTORE_PASSWORD = System.getenv("DB_TS_PASS");

    public static Connection getSecureConnection() throws Exception {
        System.setProperty("javax.net.ssl.trustStore", TRUSTSTORE_PATH);
        System.setProperty("javax.net.ssl.trustStorePassword", TRUSTSTORE_PASSWORD);

        String jdbcUrl = String.format("jdbc:mysql://%s:3306/%s?useSSL=true&requireSSL=true&verifyServerCertificate=true",DB_HOST, DB_NAME); // URL with SSL/TLS mandatory for connection, this format is necessary for https connection to jdbc

        Properties connProps = new Properties();
        connProps.put("user", DB_USER);
        connProps.put("password", DB_PASSWORD);

        return DriverManager.getConnection(jdbcUrl, connProps);
    }

    public static void fetchEmployeeRecord(String username) { // This will fetch the reccords of the employee that fell for the phishing email, just using https call
        String sql = "SELECT id, full_name, email FROM employees WHERE username = ?";
        try (Connection conn = getSecureConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username); //set user parameters (employee username)

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) { //Print all the records found 
                    System.out.printf("ID: %d, Name: %s, Email: %s%n",
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("email")
                    );
                } else {
                    System.out.println("No such user.");
                }
            }

        } catch (Exception e) {
            // print any error for debugging
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        fetchEmployeeRecord("alice");
    }
}
