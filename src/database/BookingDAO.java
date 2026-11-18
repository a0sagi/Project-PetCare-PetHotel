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
        String upsertOwnerSql = """
                INSERT INTO pemilik (nomor_telepon, nama_pemilik)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE nama_pemilik = VALUES(nama_pemilik)
                """;

        String insertPetSql = """
                INSERT INTO hewan (nama_hewan, jenis_hewan, nomor_telepon, catatan_khusus)
                VALUES (?, ?, ?, ?)
                """;

        String insertBookingSql = """
                INSERT INTO bookings
                    (id_booking, nama_hewan, jenis_hewan, catatan_khusus, nama_pemilik, nomor_telepon,
                     jenis_layanan, durasi_hari, status, total_bill)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                // 1. Simpan / perbarui relasi pemilik di tabel owners
                try (PreparedStatement ownerStmt = connection.prepareStatement(upsertOwnerSql)) {
                    ownerStmt.setString(1, booking.getNomorTelpon());
                    ownerStmt.setString(2, booking.getNamaPemilik());
                    ownerStmt.executeUpdate();
                }

                // 2. Catat hewan di tabel hewan
                try (PreparedStatement petStmt = connection.prepareStatement(insertPetSql)) {
                    petStmt.setString(1, booking.getNamaHewan());
                    petStmt.setString(2, booking.getJenisHewan().name());
                    petStmt.setString(3, booking.getNomorTelpon());
                    petStmt.setString(4, booking.getCatatanKhusus());
                    petStmt.executeUpdate();
                }

                // 3. Simpan transaksi pemesanan di tabel bookings
                try (PreparedStatement bookingStmt = connection.prepareStatement(insertBookingSql)) {
                    bookingStmt.setInt(1, booking.getIdBooking());
                    bookingStmt.setString(2, booking.getNamaHewan());
                    bookingStmt.setString(3, booking.getJenisHewan().name());
                    bookingStmt.setString(4, booking.getCatatanKhusus());
                    bookingStmt.setString(5, booking.getNamaPemilik());
                    bookingStmt.setString(6, booking.getNomorTelpon());
                    bookingStmt.setString(7, booking.getJenisLayanan().name());
                    bookingStmt.setInt(8, booking.getDurasiHari());
                    bookingStmt.setString(9, booking.getStatus().name());
                    bookingStmt.setBigDecimal(10, booking.getTotalBill());
                    bookingStmt.executeUpdate();
                }

                connection.commit();
            } catch (SQLException exception) {
                rollback(connection, exception);
                throw exception;
            }
        }
    }

    public List<PetBooking> findAll() throws SQLException {
        String sql = """
                SELECT id_booking, nama_hewan, jenis_hewan, catatan_khusus, nama_pemilik, nomor_telepon,
                       jenis_layanan, durasi_hari, status, total_bill
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
                 SELECT id_booking, nama_hewan, jenis_hewan, catatan_khusus, nama_pemilik, nomor_telepon,
                        jenis_layanan, durasi_hari, status, total_bill
                 FROM bookings
                 WHERE CAST(id_booking AS CHAR) LIKE ?
                    OR nama_hewan LIKE ?
                    OR jenis_hewan LIKE ?
                    OR COALESCE(catatan_khusus, '') LIKE ?
                    OR nama_pemilik LIKE ?
                    OR nomor_telepon LIKE ?
                    OR jenis_layanan LIKE ?
                    OR status LIKE ?
                 ORDER BY created_at DESC
                 """;
        String pattern = "%" + query.trim() + "%";
        List<PetBooking> bookings = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int parameter = 1; parameter <= 8; parameter++) {
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

    public void delete(int idBooking) throws SQLException {
        String sql = "DELETE FROM bookings WHERE id_booking = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idBooking);
            requireUpdatedRow(statement.executeUpdate(), idBooking);
        }
    }

    public void updateStatus(int idBooking, StatusPenitipan status) throws SQLException {
        String sql = "UPDATE bookings SET status = ? WHERE id_booking = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setInt(2, idBooking);
            requireUpdatedRow(statement.executeUpdate(), idBooking);
        }
    }

    public void updateBooking(PetBooking booking) throws SQLException {
        String upsertOwnerSql = """
                INSERT INTO pemilik (nomor_telepon, nama_pemilik)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE nama_pemilik = VALUES(nama_pemilik)
                """;

        String updateBookingSql = """
                UPDATE bookings
                SET nama_hewan = ?, jenis_hewan = ?, catatan_khusus = ?, nama_pemilik = ?, nomor_telepon = ?,
                    jenis_layanan = ?, durasi_hari = ?, total_bill = ?
                WHERE id_booking = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                // Pastikan data pemilik diperbarui
                try (PreparedStatement ownerStmt = connection.prepareStatement(upsertOwnerSql)) {
                    ownerStmt.setString(1, booking.getNomorTelpon());
                    ownerStmt.setString(2, booking.getNamaPemilik());
                    ownerStmt.executeUpdate();
                }

                try (PreparedStatement bookingStmt = connection.prepareStatement(updateBookingSql)) {
                    bookingStmt.setString(1, booking.getNamaHewan());
                    bookingStmt.setString(2, booking.getJenisHewan().name());
                    bookingStmt.setString(3, booking.getCatatanKhusus());
                    bookingStmt.setString(4, booking.getNamaPemilik());
                    bookingStmt.setString(5, booking.getNomorTelpon());
                    bookingStmt.setString(6, booking.getJenisLayanan().name());
                    bookingStmt.setInt(7, booking.getDurasiHari());
                    bookingStmt.setBigDecimal(8, booking.getTotalBill());
                    bookingStmt.setInt(9, booking.getIdBooking());
                    if (bookingStmt.executeUpdate() == 0) {
                        requireBookingExists(connection, booking.getIdBooking());
                    }
                }
                connection.commit();
            } catch (SQLException exception) {
                rollback(connection, exception);
                throw exception;
            }
        }
    }

    public void checkout(PetBooking booking) throws SQLException {
        String updateBookingSql = "UPDATE bookings SET status = ?, total_bill = ? WHERE id_booking = ?";
        String insertInvoiceSql = """
                INSERT INTO invoices (id_booking, total_amount, status_pembayaran)
                VALUES (?, ?, 'PAID')
                ON DUPLICATE KEY UPDATE total_amount = VALUES(total_amount), status_pembayaran = 'PAID'
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                // 1. Update status booking menjadi CHECK_OUT
                try (PreparedStatement bookingStmt = connection.prepareStatement(updateBookingSql)) {
                    bookingStmt.setString(1, StatusPenitipan.CHECK_OUT.name());
                    bookingStmt.setBigDecimal(2, booking.calculateTotalBill());
                    bookingStmt.setInt(3, booking.getIdBooking());
                    requireUpdatedRow(bookingStmt.executeUpdate(), booking.getIdBooking());
                }

                // 2. Terbitkan faktur pelunasan di tabel invoices
                try (PreparedStatement invoiceStmt = connection.prepareStatement(insertInvoiceSql)) {
                    invoiceStmt.setInt(1, booking.getIdBooking());
                    invoiceStmt.setBigDecimal(2, booking.calculateTotalBill());
                    invoiceStmt.executeUpdate();
                }

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
        String sql = "SELECT 1 FROM bookings WHERE id_booking = ?";
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
        JenisHewan jenisHewan = JenisHewan.valueOf(resultSet.getString("jenis_hewan"));
        String specialNotes = resultSet.getString("catatan_khusus");
        if (specialNotes == null) {
            specialNotes = "";
        }
        Hewan hewan = HewanFactory.create(jenisHewan, resultSet.getString("nama_hewan"), specialNotes);
        PetBooking booking = new PetBooking(
                resultSet.getInt("id_booking"),
                resultSet.getString("nama_pemilik"),
                resultSet.getString("nomor_telepon"),
                hewan,
                JenisLayanan.valueOf(resultSet.getString("jenis_layanan")),
                resultSet.getInt("durasi_hari"),
                StatusPenitipan.valueOf(resultSet.getString("status")));
        booking.setTotalBill(resultSet.getBigDecimal("total_bill"));
        return booking;
    }
}
