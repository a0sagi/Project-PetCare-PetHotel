package view;

import model.PemesananHewan;

import javax.swing.table.DefaultTableModel;
import java.text.NumberFormat;
import java.util.Locale;

public class BookingTableModel extends DefaultTableModel {
    private static final String[] COLUMNS = {
            "ID Booking", "Nama Hewan", "Jenis", "Layanan", "Durasi", "Status", "Tagihan"
    };

    public BookingTableModel() {
        super(COLUMNS, 0);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    public void addBooking(PemesananHewan booking) {
        addRow(toRow(booking));
    }

    public void updateBooking(int row, PemesananHewan booking) {
        Object[] values = toRow(booking);
        for (int column = 0; column < values.length; column++) {
            setValueAt(values[column], row, column);
        }
    }

    private Object[] toRow(PemesananHewan booking) {
        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));
        return new Object[]{
                booking.getBookingId(),
                booking.getPetName(),
                booking.getPetType().getLabel(),
                booking.getServiceType().getLabel(),
                booking.getDurationDays() + " hari",
                booking.getStatus().getLabel(),
                currency.format(booking.getTotalBill())
        };
    }
}
