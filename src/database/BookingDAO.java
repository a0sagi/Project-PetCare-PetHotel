package database;

import model.Hewan;
import model.AnimalFactory;
import model.BookingStatus;
import model.PemesananHewan;
import model.PetType;
import model.JenisLayanan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {
    public void insert(PemesananHewan booking) throws SQLException {
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

    public List<PemesananHewan> findAll() throws SQLException {
        String sql = """
                SELECT booking_id, pet_name, pet_type, owner_name, owner_phone,
                       service_type, duration_days, status, total_bill
                FROM bookings
                ORDER BY created_at DESC
                """;
        List<PemesananHewan> bookings = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                PetType petType = PetType.valueOf(resultSet.getString("pet_type"));
                Hewan animal = AnimalFactory.create(petType, resultSet.getString("pet_name"));
                PemesananHewan booking = new PemesananHewan(
                        resultSet.getInt("booking_id"),
                        resultSet.getString("owner_name"),
                        resultSet.getInt("owner_phone"),
                        animal,
                        JenisLayanan.valueOf(resultSet.getString("service_type")),
                        resultSet.getInt("duration_days"),
                        BookingStatus.valueOf(resultSet.getString("status")));
                booking.setTotalBill(resultSet.getBigDecimal("total_bill"));
                bookings.add(booking);
            }
        }
        return bookings;
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

    public void checkout(PemesananHewan booking) throws SQLException {
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

    private static void rollback(Connection connection, SQLException original) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            original.addSuppressed(rollbackException);
        }
    }
}
