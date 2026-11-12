package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DatabaseConnection {
    private static final Logger LOGGER =
            Logger.getLogger(DatabaseConnection.class.getName());
    private static final String URL =
            "jdbc:mysql://localhost:3306/petcare_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException exception) {
            SQLException sqlException = new SQLException(
                    "Driver MySQL tidak ditemukan. Pastikan Connector/J ada di folder lib.",
                    exception);
            LOGGER.log(Level.SEVERE, sqlException.getMessage(), sqlException);
            throw sqlException;
        }

        try {
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            LOGGER.info("Koneksi ke database petcare_db berhasil.");
            return connection;
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Gagal terhubung ke database petcare_db.", exception);
            throw exception;
        }
    }
}
