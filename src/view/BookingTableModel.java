package view;

import model.PetBooking;

import javax.swing.table.DefaultTableModel;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BookingTableModel extends DefaultTableModel {
    private static final String[] COLUMNS = {
            "ID Pemesanan", "Nama Pemilik", "No. HP", "Nama Hewan", "Jenis Hewan",
            "Catatan Khusus", "Layanan", "Durasi", "Status", "Tagihan"
    };
    private static final Map<String, String> SERVICE_LABELS = Map.of(
            "Grooming", "Perawatan",
            "Boarding", "Penitipan",
            "Full Service", "Layanan Lengkap");
    private static final Map<String, String> STATUS_LABELS = Map.of(
            "Check-In", "Check-In",
            "Sedang Perawatan", "Sedang Dirawat",
            "Check-Out", "Selesai");

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

    public void setBookings(List<PetBooking> bookings) {
        setRowCount(0);
        for (PetBooking booking : bookings) {
            addBooking(booking);
        }
    }

    private Object[] toRow(PetBooking booking) {
        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));
        return new Object[]{
                booking.getIdBooking(),
                booking.getNamaPemilik(),
                booking.getNomorTelpon(),
                booking.getNamaHewan(),
                booking.getJenisHewan().getLabel(),
                booking.getCatatanKhusus(),
                SERVICE_LABELS.getOrDefault(booking.getJenisLayanan().getLabel(),
                        booking.getJenisLayanan().getLabel()),
                booking.getDurasiHari() + " hari",
                STATUS_LABELS.getOrDefault(booking.getStatus().getLabel(),
                        booking.getStatus().getLabel()),
                currency.format(booking.getTotalBill())
        };
    }
}
