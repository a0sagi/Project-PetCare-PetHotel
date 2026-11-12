package database;

import model.Pet;
import model.AnimalFactory;
import model.BookingStatus;
import model.PetBooking;
import model.PetType;
import model.ServiceType;

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
            statement.setInt(1, booking.getBookingId());
            statement.setString(2, booking.getPetName());
            statement.setString(3, booking.getPetType().name());
            statement.setString(4, booking.getOwnerName());
            statement.setInt(5, booking.getOwnerPhone());
            statement.setString(6, booking.getServiceType().name());
            statement.setInt(7, booking.getDurationDays());
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
                    OR CAST(owner_phone AS CHAR) LIKE ?
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

    public void updateStatus(int bookingId, BookingStatus status) throws SQLException {
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
            statement.setString(1, booking.getPetName());
            statement.setString(2, booking.getPetType().name());
            statement.setString(3, booking.getOwnerName());
            statement.setInt(4, booking.getOwnerPhone());
            statement.setString(5, booking.getServiceType().name());
            statement.setInt(6, booking.getDurationDays());
            statement.setBigDecimal(7, booking.getTotalBill());
            statement.setInt(8, booking.getBookingId());
            if (statement.executeUpdate() == 0) {
                requireBookingExists(connection, booking.getBookingId());
            }
        }
    }

    public void checkout(PetBooking booking) throws SQLException {
        String sql = "UPDATE bookings SET status = ?, total_bill = ? WHERE booking_id = ?";
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, BookingStatus.CHECKED_OUT.name());
                statement.setBigDecimal(2, booking.calculateTotalBill());
                statement.setInt(3, booking.getBookingId());
                requireUpdatedRow(statement.executeUpdate(), booking.getBookingId());
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
        PetType petType = PetType.valueOf(resultSet.getString("pet_type"));
        Pet animal = AnimalFactory.create(petType, resultSet.getString("pet_name"));
        PetBooking booking = new PetBooking(
                resultSet.getInt("booking_id"),
                resultSet.getString("owner_name"),
                resultSet.getInt("owner_phone"),
                animal,
                ServiceType.valueOf(resultSet.getString("service_type")),
                resultSet.getInt("duration_days"),
                BookingStatus.valueOf(resultSet.getString("status")));
        booking.setTotalBill(resultSet.getBigDecimal("total_bill"));
        return booking;
    }
}
