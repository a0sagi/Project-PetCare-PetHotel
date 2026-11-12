package repository;

import model.BookingStatus;
import model.PetBooking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingRepository {
    private final List<PetBooking> bookingList = new ArrayList<>();
    private final Map<Integer, PetBooking> bookingMap = new HashMap<>();

    public void addBooking(PetBooking booking) {
        if (bookingMap.containsKey(booking.getBookingId())) {
            throw new IllegalArgumentException("ID booking sudah terdaftar.");
        }
        bookingList.add(booking);
        bookingMap.put(booking.getBookingId(), booking);
    }

    public PetBooking findById(int bookingId) {
        return bookingMap.get(bookingId);
    }

    public List<PetBooking> getAllBookings() {
        return new ArrayList<>(bookingList);
    }

    public void updateStatus(int bookingId, BookingStatus newStatus) {
        PetBooking booking = bookingMap.get(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking tidak ditemukan.");
        }
        booking.setStatus(newStatus);
    }

    public void updateBooking(PetBooking booking) {
        PetBooking current = bookingMap.get(booking.getBookingId());
        if (current == null) {
            throw new IllegalArgumentException("Booking tidak ditemukan.");
        }
        int index = bookingList.indexOf(current);
        bookingList.set(index, booking);
        bookingMap.put(booking.getBookingId(), booking);
    }

    public void deleteBooking(int bookingId) {
        PetBooking booking = bookingMap.remove(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking tidak ditemukan.");
        }
        bookingList.remove(booking);
    }
}
