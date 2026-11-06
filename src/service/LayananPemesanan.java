package service;

import database.BookingDAO;
import model.Hewan;
import model.BookingStatus;
import model.PemesananHewan;
import model.JenisLayanan;
import repository.BookingRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class LayananPemesanan {
    private final BookingRepository repository;
    private final BookingDAO dao;

    public LayananPemesanan(BookingRepository repository, BookingDAO dao) throws SQLException {
        this.repository = Objects.requireNonNull(repository);
        this.dao = dao;
        if (dao != null) {
            for (PemesananHewan booking : dao.findAll()) {
                repository.addBooking(booking);
            }
        }
    }

    public PemesananHewan register(int bookingId, String ownerName, int ownerPhone,
                                   Hewan animal, JenisLayanan serviceType, int durationDays)
            throws SQLException {
        if (repository.findById(bookingId) != null) {
            throw new IllegalArgumentException("ID booking sudah terdaftar.");
        }
        PemesananHewan booking = new PemesananHewan(bookingId, ownerName, ownerPhone,
                animal, serviceType, durationDays);
        if (dao != null) {
            dao.insert(booking);
        }
        repository.addBooking(booking);
        return booking;
    }

    public BookingStatus advanceStatus(int bookingId) throws SQLException {
        PemesananHewan booking = requireBooking(bookingId);
        BookingStatus nextStatus = booking.getStatus().next();
        if (dao != null) {
            dao.updateStatus(bookingId, nextStatus);
        }
        repository.updateStatus(bookingId, nextStatus);
        return nextStatus;
    }

    public PemesananHewan checkout(int bookingId) throws SQLException {
        PemesananHewan booking = requireBooking(bookingId);
        if (booking.getStatus() != BookingStatus.READY_FOR_CHECKOUT) {
            throw new IllegalStateException("Majukan status sampai Siap Check-Out terlebih dahulu.");
        }
        if (dao != null) {
            dao.checkout(booking);
        }
        booking.markCheckedOut();
        return booking;
    }

    public PemesananHewan findById(int bookingId) {
        return repository.findById(bookingId);
    }

    public List<PemesananHewan> getAllBookings() {
        return repository.getAllBookings();
    }

    private PemesananHewan requireBooking(int bookingId) {
        PemesananHewan booking = repository.findById(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking tidak ditemukan.");
        }
        return booking;
    }
}
