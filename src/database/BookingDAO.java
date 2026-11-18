package database;

import model.Hewan;
import model.HewanFactory;
import model.JenisHewan;
import model.JenisLayanan;
import model.PetBooking;
import model.StatusPenitipan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {
    public void insert(PetBooking booking) throws SQLException {
        String sql = """
                INSERT INTO bookings
                    (booking_id, pet_name, pet_type, owner_name, owner_phone,
                     service_type, duration_days, status, total_bill)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, booking.getIdBooking());
            statement.setString(2, booking.getNamaHewan());
            statement.setString(3, booking.getJenisHewan().name());
            statement.setString(4, booking.getNamaPemilik());
            statement.setString(5, booking.getNomorTelpon());
            statement.setString(6, booking.getJenisLayanan().name());
            statement.setInt(7, booking.getDurasiHari());
            statement.setString(8, booking.getStatus().name());
            statement.setBigDecimal(9, booking.getTotalBill());
            statement.executeUpdate();
        }
    }

    public List<PetBooking> findAll() throws SQLException {
        String sql = """
                SELECT booking_id, pet_name, pet_type, owner_name, owner_phone,
                       service_type, duration_days, status, total_bill
                FROM bookings
                ORDER BY created_at DESC
                """;
        List<PetBooking> bookings = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                bookings.add(readBooking(resultSet));
            }
        }
        return bookings;
    }

    public List<PetBooking> search(String query) throws SQLException {
        String sql = """
                 SELECT booking_id, pet_name, pet_type, owner_name, owner_phone,
                        service_type, duration_days, status, total_bill
                 FROM bookings
                 WHERE CAST(booking_id AS CHAR) LIKE ?
                    OR pet_name LIKE ?
                    OR pet_type LIKE ?
                    OR owner_name LIKE ?
                    OR owner_phone LIKE ?
                    OR service_type LIKE ?
                    OR status LIKE ?
                 ORDER BY created_at DESC
                 """;
        String pattern = "%" + query.trim() + "%";
        List<PetBooking> bookings = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
              PreparedStatement statement = connection.prepareStatement(sql)) {
             for (int parameter = 1; parameter <= 7; parameter++) {
                 statement.setString(parameter, pattern);
             }
             try (ResultSet resultSet = statement.executeQuery()) {
                 while (resultSet.next()) {
                     bookings.add(readBooking(resultSet));
                 }
             }
        }
        return bookings;
    }

    public void delete(int bookingId) throws SQLException {
        String sql = "DELETE FROM bookings WHERE booking_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
              PreparedStatement statement = connection.prepareStatement(sql)) {
             statement.setInt(1, bookingId);
             requireUpdatedRow(statement.executeUpdate(), bookingId);
        }
    }

    public void updateStatus(int bookingId, StatusPenitipan status) throws SQLException {
        String sql = "UPDATE bookings SET status = ? WHERE booking_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setInt(2, bookingId);
            requireUpdatedRow(statement.executeUpdate(), bookingId);
        }
    }

    public void updateBooking(PetBooking booking) throws SQLException {
        String sql = """
                UPDATE bookings
                SET pet_name = ?, pet_type = ?, owner_name = ?, owner_phone = ?,
                    service_type = ?, duration_days = ?, total_bill = ?
                WHERE booking_id = ?
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, booking.getNamaHewan());
            statement.setString(2, booking.getJenisHewan().name());
            statement.setString(3, booking.getNamaPemilik());
            statement.setString(4, booking.getNomorTelpon());
            statement.setString(5, booking.getJenisLayanan().name());
            statement.setInt(6, booking.getDurasiHari());
            statement.setBigDecimal(7, booking.getTotalBill());
            statement.setInt(8, booking.getIdBooking());
            if (statement.executeUpdate() == 0) {
                requireBookingExists(connection, booking.getIdBooking());
            }
        }
    }

    public void checkout(PetBooking booking) throws SQLException {
        String sql = "UPDATE bookings SET status = ?, total_bill = ? WHERE booking_id = ?";
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, StatusPenitipan.CHECK_OUT.name());
                statement.setBigDecimal(2, booking.calculateTotalBill());
                statement.setInt(3, booking.getIdBooking());
                requireUpdatedRow(statement.executeUpdate(), booking.getIdBooking());
                connection.commit();
            } catch (SQLException exception) {
                rollback(connection, exception);
                throw exception;
            }
        }
    }

    private static void requireUpdatedRow(int count, int bookingId) throws SQLException {
        if (count != 1) {
            throw new SQLException("Booking tidak ditemukan: " + bookingId);
        }
    }

    private static void requireBookingExists(Connection connection, int bookingId)
            throws SQLException {
        String sql = "SELECT 1 FROM bookings WHERE booking_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookingId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("Booking tidak ditemukan: " + bookingId);
                }
            }
        }
    }

    private static void rollback(Connection connection, SQLException original) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            original.addSuppressed(rollbackException);
        }
    }

    private static PetBooking readBooking(ResultSet resultSet) throws SQLException {
        JenisHewan jenisHewan = JenisHewan.valueOf(resultSet.getString("pet_type"));
        Hewan hewan = HewanFactory.create(jenisHewan, resultSet.getString("pet_name"));
        PetBooking booking = new PetBooking(
                resultSet.getInt("booking_id"),
                resultSet.getString("owner_name"),
                resultSet.getString("owner_phone"),
                hewan,
                JenisLayanan.valueOf(resultSet.getString("service_type")),
                resultSet.getInt("duration_days"),
                StatusPenitipan.valueOf(resultSet.getString("status")));
        booking.setTotalBill(resultSet.getBigDecimal("total_bill"));
        return booking;
    }
}
