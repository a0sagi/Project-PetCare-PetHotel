package service;

import database.BookingDAO;
import model.Hewan;
import model.StatusPenitipan;
import model.PetBooking;
import model.JenisLayanan;
import repository.BookingRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class BookingService {
    private final BookingRepository repository;
    private final BookingDAO dao;

    public BookingService(BookingRepository repository, BookingDAO dao) throws SQLException {
        this.repository = Objects.requireNonNull(repository);
        this.dao = dao;
        if (dao != null) {
            for (PetBooking booking : dao.findAll()) {
                repository.addBooking(booking);
            }
        }
    }

    public PetBooking register(int idBooking, String namaPemilik, String nomorTelpon,
                               Hewan hewan, JenisLayanan jenisLayanan, int durationDays)
            throws SQLException {
        if (repository.findById(idBooking) != null) {
            throw new IllegalArgumentException("ID booking sudah terdaftar.");
        }
        PetBooking booking = new PetBooking(idBooking, namaPemilik, nomorTelpon,
                hewan, jenisLayanan, durationDays);
        if (dao != null) {
            dao.insert(booking);
        }
        repository.addBooking(booking);
        return booking;
    }

    public StatusPenitipan advanceStatus(int idBooking) throws SQLException {
        PetBooking booking = requireBooking(idBooking);
        StatusPenitipan nextStatus = booking.getStatus().next();
        if (dao != null) {
            dao.updateStatus(idBooking, nextStatus);
        }
        repository.updateStatus(idBooking, nextStatus);
        return nextStatus;
    }

    public StatusPenitipan updateStatus(int idBooking, StatusPenitipan status) throws SQLException {
        PetBooking booking = requireBooking(idBooking);
        if (booking.getStatus() == StatusPenitipan.CHECK_OUT) {
            throw new IllegalStateException("Status booking yang sudah checkout tidak dapat diubah.");
        }
        if (status == null || status == StatusPenitipan.CHECK_OUT) {
            throw new IllegalArgumentException("Gunakan proses checkout untuk menyelesaikan booking.");
        }
        if (booking.getStatus() == status) {
            return status;
        }
        if (dao != null) {
            dao.updateStatus(idBooking, status);
        }
        repository.updateStatus(idBooking, status);
        return status;
    }

    public PetBooking checkout(int idBooking) throws SQLException {
        PetBooking booking = requireBooking(idBooking);
        if (booking.getStatus() != StatusPenitipan.PERAWATAN) {
            throw new IllegalStateException("Majukan status ke Sedang Perawatan terlebih dahulu.");
        }
        if (dao != null) {
            dao.checkout(booking);
        }
        booking.markCheckedOut();
        return booking;
    }

    public PetBooking updateBooking(int idBooking, String namaPemilik, String nomorTelpon,
                                    Hewan hewan, JenisLayanan jenisLayanan, int durationDays)
            throws SQLException {
        PetBooking current = requireBooking(idBooking);
        if (current.getStatus() == StatusPenitipan.CHECK_OUT) {
            throw new IllegalStateException("Data booking yang sudah checkout tidak dapat diubah.");
        }
        PetBooking updated = new PetBooking(idBooking, namaPemilik, nomorTelpon,
                hewan, jenisLayanan, durationDays, current.getStatus());
        if (dao != null) {
            dao.updateBooking(updated);
        }
        repository.updateBooking(updated);
        return updated;
    }

    public PetBooking findById(int idBooking) {
        return repository.findById(idBooking);
    }

    public List<PetBooking> getAllBookings() {
        return repository.getAllBookings();
    }

    public List<PetBooking> searchBookings(String query) throws SQLException {
        String normalized = query == null ? "" : query.trim();
        if (dao != null) {
            return dao.search(normalized);
        }
        if (normalized.isEmpty()) {
            return getAllBookings();
        }
        String searchTerm = normalized.toLowerCase(Locale.ROOT);
        return getAllBookings().stream()
                .filter(booking -> Integer.toString(booking.getIdBooking()).contains(searchTerm)
                        || booking.getNamaPemilik().toLowerCase(Locale.ROOT).contains(searchTerm)
                        || booking.getNomorTelpon().contains(searchTerm)
                        || booking.getNamaHewan().toLowerCase(Locale.ROOT).contains(searchTerm)
                        || booking.getJenisHewan().getLabel().toLowerCase(Locale.ROOT).contains(searchTerm)
                        || booking.getJenisLayanan().getLabel().toLowerCase(Locale.ROOT).contains(searchTerm)
                        || booking.getStatus().getLabel().toLowerCase(Locale.ROOT).contains(searchTerm))
                .collect(Collectors.toList());
    }

    public void deleteBooking(int idBooking) throws SQLException {
        requireBooking(idBooking);
        if (dao != null) {
            dao.delete(idBooking);
        }
        repository.deleteBooking(idBooking);
    }

    private PetBooking requireBooking(int idBooking) {
        PetBooking booking = repository.findById(idBooking);
        if (booking == null) {
            throw new IllegalArgumentException("Booking tidak ditemukan.");
        }
        return booking;
    }
}
