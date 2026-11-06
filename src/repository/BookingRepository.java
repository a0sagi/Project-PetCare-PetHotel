package repository;

import model.BookingStatus;
import model.PemesananHewan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingRepository {
    private final List<PemesananHewan> bookingList = new ArrayList<>();
    private final Map<Integer, PemesananHewan> bookingMap = new HashMap<>();

    public void addBooking(PemesananHewan booking) {
        if (bookingMap.containsKey(booking.getBookingId())) {
            throw new IllegalArgumentException("ID booking sudah terdaftar.");
        }
        bookingList.add(booking);
        bookingMap.put(booking.getBookingId(), booking);
    }

    public PemesananHewan findById(int bookingId) {
        return bookingMap.get(bookingId);
    }

    public List<PemesananHewan> getAllBookings() {
        return new ArrayList<>(bookingList);
    }

    public void updateStatus(int bookingId, BookingStatus newStatus) {
        PemesananHewan booking = bookingMap.get(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking tidak ditemukan.");
        }
        booking.setStatus(newStatus);
    }
}
