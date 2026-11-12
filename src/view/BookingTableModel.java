package view;

import model.PetBooking;

import javax.swing.table.DefaultTableModel;
import java.text.NumberFormat;
import java.util.Locale;

public class BookingTableModel extends DefaultTableModel {
    private static final String[] COLUMNS = {
            "ID Booking", "Nama Pemilik", "No. HP", "Nama Hewan", "Jenis Hewan",
            "Layanan", "Durasi", "Status", "Tagihan"
    };

    public BookingTableModel() {
        super(COLUMNS, 0);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    public void addBooking(PetBooking booking) {
        addRow(toRow(booking));
    }

    public void updateBooking(int row, PetBooking booking) {
        Object[] values = toRow(booking);
        for (int column = 0; column < values.length; column++) {
            setValueAt(values[column], row, column);
        }
    }

    public void setBookings(java.util.List<PetBooking> bookings) {
        setRowCount(0);
        for (PetBooking booking : bookings) {
            addBooking(booking);
        }
    }

    private Object[] toRow(PetBooking booking) {
        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));
        return new Object[]{
                booking.getBookingId(),
                booking.getOwnerName(),
                booking.getOwnerPhone(),
                booking.getPetName(),
                booking.getPetType().getLabel(),
                booking.getServiceType().getLabel(),
                booking.getDurationDays() + " hari",
                booking.getStatus().getLabel(),
                currency.format(booking.getTotalBill())
        };
    }
}
