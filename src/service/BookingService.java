package service;

import database.BookingDAO;
import model.Pet;
import model.BookingStatus;
import model.PetBooking;
import model.ServiceType;
import repository.BookingRepository;

import java.sql.SQLException;
import java.util.Locale;
import java.util.List;
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

    public PetBooking register(int bookingId, String ownerName, int ownerPhone,
                                   Pet animal, ServiceType serviceType, int durationDays)
            throws SQLException {
        if (repository.findById(bookingId) != null) {
            throw new IllegalArgumentException("ID booking sudah terdaftar.");
        }
        PetBooking booking = new PetBooking(bookingId, ownerName, ownerPhone,
                animal, serviceType, durationDays);
        if (dao != null) {
            dao.insert(booking);
        }
        repository.addBooking(booking);
        return booking;
    }

    public BookingStatus advanceStatus(int bookingId) throws SQLException {
        PetBooking booking = requireBooking(bookingId);
        BookingStatus nextStatus = booking.getStatus().next();
        if (dao != null) {
            dao.updateStatus(bookingId, nextStatus);
        }
        repository.updateStatus(bookingId, nextStatus);
        return nextStatus;
    }

    public BookingStatus updateStatus(int bookingId, BookingStatus status) throws SQLException {
        PetBooking booking = requireBooking(bookingId);
        if (booking.getStatus() == BookingStatus.CHECKED_OUT) {
            throw new IllegalStateException("Status booking yang sudah checkout tidak dapat diubah.");
        }
        if (status == null || status == BookingStatus.CHECKED_OUT) {
            throw new IllegalArgumentException("Gunakan proses checkout untuk menyelesaikan booking.");
        }
        if (booking.getStatus() == status) {
            return status;
        }
        if (dao != null) {
            dao.updateStatus(bookingId, status);
        }
        repository.updateStatus(bookingId, status);
        return status;
    }

    public PetBooking checkout(int bookingId) throws SQLException {
        PetBooking booking = requireBooking(bookingId);
        if (booking.getStatus() != BookingStatus.READY_FOR_CHECKOUT) {
            throw new IllegalStateException("Majukan status sampai Siap Check-Out terlebih dahulu.");
        }
        if (dao != null) {
            dao.checkout(booking);
        }
        booking.markCheckedOut();
        return booking;
    }

    public PetBooking updateBooking(int bookingId, String ownerName, int ownerPhone,
                                    Pet animal, ServiceType serviceType, int durationDays)
            throws SQLException {
        PetBooking current = requireBooking(bookingId);
        if (current.getStatus() == BookingStatus.CHECKED_OUT) {
            throw new IllegalStateException("Data booking yang sudah checkout tidak dapat diubah.");
        }
        PetBooking updated = new PetBooking(bookingId, ownerName, ownerPhone,
                animal, serviceType, durationDays, current.getStatus());
        if (dao != null) {
            dao.updateBooking(updated);
        }
        repository.updateBooking(updated);
        return updated;
    }

    public PetBooking findById(int bookingId) {
        return repository.findById(bookingId);
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
                .filter(booking -> Integer.toString(booking.getBookingId()).contains(searchTerm)
                        || booking.getOwnerName().toLowerCase(Locale.ROOT).contains(searchTerm)
                        || Integer.toString(booking.getOwnerPhone()).contains(searchTerm)
                        || booking.getPetName().toLowerCase(Locale.ROOT).contains(searchTerm)
                        || booking.getPetType().getLabel().toLowerCase(Locale.ROOT).contains(searchTerm)
                        || booking.getServiceType().getLabel().toLowerCase(Locale.ROOT).contains(searchTerm)
                        || booking.getStatus().getLabel().toLowerCase(Locale.ROOT).contains(searchTerm))
                .collect(Collectors.toList());
    }

    public void deleteBooking(int bookingId) throws SQLException {
        requireBooking(bookingId);
        if (dao != null) {
            dao.delete(bookingId);
        }
        repository.deleteBooking(bookingId);
    }

    private PetBooking requireBooking(int bookingId) {
        PetBooking booking = repository.findById(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking tidak ditemukan.");
        }
        return booking;
    }
}
