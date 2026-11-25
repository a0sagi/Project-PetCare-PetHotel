package controller;

import model.HewanFactory;
import model.JenisHewan;
import model.JenisLayanan;
import model.Hewan;
import model.PetBooking;
import model.StatusPenitipan;
import service.BookingService;
import view.PetCareMainFrame;

import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;

public class BookingController {
    private final PetCareMainFrame view;
    private final BookingService service;
    private boolean loadingSelectedStatus;

    public BookingController(PetCareMainFrame view, BookingService service) {
        this.view = view;
        this.service = service;
        bindEvents();
        refreshTable("");
    }

    private void bindEvents() {
        view.getAddBookingButton().addActionListener(event -> registerBooking());
        view.getUpdateDataButton().addActionListener(event -> updateBookingData());
        view.getStatusBox().addActionListener(event -> {
            if (!loadingSelectedStatus) {
                updateStatus();
            }
        });
        for (JButton button : view.getCheckoutButtons()) {
            button.addActionListener(event -> checkout());
        }
        view.getSearchButton().addActionListener(event -> searchBookings());
        view.getSearchField().addActionListener(event -> searchBookings());
        view.getDeleteButton().addActionListener(event -> deleteBooking());
        view.getResetButton().addActionListener(event -> resetForm());
        view.getPages().addChangeListener(event -> syncSelectedControls());
        for (JTable table : view.getTables()) {
            table.getSelectionModel().addListSelectionListener(this::selectBooking);
        }
        view.getNomorTelponField().addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent event) {
                if (!Character.isDigit(event.getKeyChar())
                        && !Character.isISOControl(event.getKeyChar())) {
                    event.consume();
                }
            }
        });
    }

    private void registerBooking() {
        try {
            int bookingId = Integer.parseInt(view.getIdBookingField().getText().trim());
            JenisHewan jenisHewan = JenisHewan.fromLabel(
                    (String) view.getJenisHewanBox().getSelectedItem());
            String catatanKhusus = view.getCatatanKhususField().getText().trim();
            Hewan hewan = HewanFactory.create(
                    jenisHewan, view.getNamaHewanField().getText().trim(), catatanKhusus);
            service.register(
                    bookingId,
                    view.getNamaPemilikField().getText().trim(),
                    view.getNomorTelponField().getText().trim(),
                    hewan,
                    selectedJenisLayanan(),
                    selectedDurasiHari());
            refreshTable(view.getSearchField().getText());
            clearForm();
            JOptionPane.showMessageDialog(view, "Pemesanan berhasil disimpan.",
                    "Berhasil", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException | SQLException exception) {
            showError(exception);
        }
    }

    private void updateStatus() {
        if (!view.isStatusManagementPage()) {
            return;
        }
        PetBooking booking = selectedBooking();
        if (booking == null) {
            view.setUpdateDataEnabled(false);
            view.getStatusBox().setEnabled(false);
            return;
        }
        try {
            StatusPenitipan selectedStatus = statusFromLabel(
                    (String) view.getStatusBox().getSelectedItem());
            String nextStatus = displayStatusLabel(
                    service.updateStatus(booking.getIdBooking(), selectedStatus).getLabel());
            refreshTable(view.getSearchField().getText());
            JOptionPane.showMessageDialog(view, "Status diperbarui menjadi " + nextStatus + ".",
                    "Perbarui Status", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException | IllegalStateException | SQLException exception) {
            showError(exception);
        }
    }

    private void updateBookingData() {
        PetBooking selected = selectedBooking();
        if (selected == null) {
            return;
        }
        try {
            JenisHewan jenisHewan = JenisHewan.fromLabel(
                    (String) view.getJenisHewanBox().getSelectedItem());
            String catatanKhusus = view.getCatatanKhususField().getText().trim();
            Hewan hewan = HewanFactory.create(
                    jenisHewan, view.getNamaHewanField().getText().trim(), catatanKhusus);
            service.updateBooking(
                    selected.getIdBooking(),
                    view.getNamaPemilikField().getText().trim(),
                    view.getNomorTelponField().getText().trim(),
                    hewan,
                    selectedJenisLayanan(),
                    selectedDurasiHari());
            refreshTable(view.getSearchField().getText());
            clearForm();
            JOptionPane.showMessageDialog(view, "Data pemesanan berhasil diperbarui.",
                    "Berhasil", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException | IllegalStateException | SQLException exception) {
            showError(exception);
        }
    }

    private void checkout() {
        if (!view.isCheckoutActionPage()) {
            return;
        }
        PetBooking selected = selectedBooking();
        if (selected == null) {
            return;
        }
        try {
            PetBooking booking = service.checkout(selected.getIdBooking());
            refreshTable(view.getSearchField().getText());
            String invoice = "======= TAGIHAN PERAWATAN & PENITIPAN =======\n"
                    + "ID Pemesanan : " + booking.getIdBooking() + "\n"
                    + "Pemilik    : " + booking.getNamaPemilik() + "\n"
                    + "Peliharaan : " + booking.getNamaHewan() + " ("
                    + booking.getJenisHewan().getLabel() + ")\n"
                    + "Layanan    : " + displayServiceLabel(booking.getJenisLayanan().getLabel()) + "\n"
                    + (booking.getJenisLayanan() == JenisLayanan.GROOMING
                    ? "TOTAL LAYANAN: " + formatCurrency(booking.getTotalBill()) + "\n"
                    : "Durasi     : " + booking.getDurasiHari() + " hari\n"
                    + "Tarif/hari : " + formatCurrency(booking.getTarifHarian()) + "\n"
                    + "Biaya jasa : " + formatCurrency(booking.getServiceFee()) + "\n"
                    + "TOTAL      : " + formatCurrency(booking.getTotalBill()) + "\n")
                    + "========================================";
            view.showCheckoutInvoice(invoice);
            view.getPages().setSelectedIndex(5);
            view.showCheckoutDialog(booking, displayServiceLabel(
                    booking.getJenisLayanan().getLabel()));
        } catch (IllegalArgumentException | IllegalStateException | SQLException exception) {
            showError(exception);
        }
    }

    private void deleteBooking() {
        PetBooking booking = selectedBooking();
        if (booking == null) {
            return;
        }
        int answer = JOptionPane.showConfirmDialog(view,
                "Hapus pemesanan dengan ID " + booking.getIdBooking() + "?",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            service.deleteBooking(booking.getIdBooking());
            refreshTable(view.getSearchField().getText());
            clearForm();
            JOptionPane.showMessageDialog(view, "Pemesanan berhasil dihapus.",
                    "Berhasil", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException | SQLException exception) {
            showError(exception);
        }
    }

    private void searchBookings() {
        refreshTable(view.getSearchField().getText());
    }

    private PetBooking selectedBooking() {
        JTable table = view.getSelectedBookingsTable();
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(view, "Pilih pemesanan pada tabel terlebih dahulu.",
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        int bookingId = (int) view.getTableModel().getValueAt(modelRow, 0);
        PetBooking booking = service.findById(bookingId);
        if (booking == null) {
            JOptionPane.showMessageDialog(view, "Pemesanan tidak ditemukan.",
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
        }
        return booking;
    }

    private PetBooking selectedBookingWithoutWarning() {
        JTable table = view.getSelectedBookingsTable();
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        int bookingId = (int) view.getTableModel().getValueAt(modelRow, 0);
        return service.findById(bookingId);
    }

    private void refreshTable(String query) {
        try {
            view.showBookings(service.searchBookings(query));
            view.filterPages(query);
        } catch (SQLException exception) {
            showError(exception);
        }
    }

    private void resetForm() {
        clearForm();
        view.getSearchField().setText("");
        refreshTable("");
        syncSelectedControls();
    }

    private void clearForm() {
        view.getIdBookingField().setText("");
        view.getIdBookingField().setEditable(true);
        view.getNamaPemilikField().setText("");
        view.getNomorTelponField().setText("");
        view.getNamaHewanField().setText("");
        view.getCatatanKhususField().setText("");
        view.getJenisHewanBox().setSelectedIndex(0);
        view.getServiceBox().setSelectedIndex(0);
        view.getDurationSpinner().setValue(1);
        view.getStatusBox().setEnabled(false);
        view.setUpdateDataEnabled(false);
        view.setCheckoutEnabled(false);
        for (JTable table : view.getTables()) {
            table.clearSelection();
        }
    }

    private int selectedDurasiHari() {
        return view.getServiceBox().getSelectedIndex() == 0
                ? 1 : (Integer) view.getDurationSpinner().getValue();
    }

    private JenisLayanan selectedJenisLayanan() {
        return switch ((String) view.getServiceBox().getSelectedItem()) {
            case "Perawatan" -> JenisLayanan.GROOMING;
            case "Penitipan" -> JenisLayanan.BOARDING;
            case "Layanan Lengkap" -> JenisLayanan.FULL_SERVICE;
            default -> throw new IllegalArgumentException("Jenis layanan tidak dikenal.");
        };
    }

    private void syncSelectedControls() {
        PetBooking booking = selectedBookingWithoutWarning();
        if (booking == null) {
            view.getStatusBox().setEnabled(false);
            view.setUpdateDataEnabled(false);
            view.setCheckoutEnabled(false);
            return;
        }
        setSelectedStatus(booking);
        view.setUpdateDataEnabled(booking.getStatus() != StatusPenitipan.CHECK_OUT);
    }

    private void selectBooking(ListSelectionEvent event) {
        if (event.getValueIsAdjusting()) {
            return;
        }
        PetBooking booking = selectedBookingWithoutWarning();
        if (booking == null) {
            view.setUpdateDataEnabled(false);
            view.getStatusBox().setEnabled(false);
            view.setCheckoutEnabled(false);
            return;
        }
        view.getIdBookingField().setText(Integer.toString(booking.getIdBooking()));
        view.getIdBookingField().setEditable(false);
        view.getNamaPemilikField().setText(booking.getNamaPemilik());
        view.getNomorTelponField().setText(booking.getNomorTelpon());
        view.getNamaHewanField().setText(booking.getNamaHewan());
        view.getCatatanKhususField().setText(booking.getCatatanKhusus());
        view.getJenisHewanBox().setSelectedItem(booking.getJenisHewan().getLabel());
        view.getServiceBox().setSelectedItem(displayServiceLabel(booking.getJenisLayanan().getLabel()));
        view.getDurationSpinner().setValue(booking.getDurasiHari());
        setSelectedStatus(booking);
        view.setUpdateDataEnabled(booking.getStatus() != StatusPenitipan.CHECK_OUT);
    }

    private void setSelectedStatus(PetBooking booking) {
        loadingSelectedStatus = true;
        try {
            view.getStatusBox().setSelectedItem(displayStatusLabel(booking.getStatus().getLabel()));
        } finally {
            loadingSelectedStatus = false;
        }
        view.getStatusBox().setEnabled(view.isStatusManagementPage()
                && booking.getStatus() != StatusPenitipan.CHECK_OUT);
        view.setCheckoutEnabled(view.isCheckoutActionPage()
                && booking.getStatus() == StatusPenitipan.SIAP_CHECKOUT);
    }

    private StatusPenitipan statusFromLabel(String label) {
        return switch (label) {
            case "Check-In" -> StatusPenitipan.CHECK_IN;
            case "Sedang Dirawat" -> StatusPenitipan.PERAWATAN;
            case "Siap Check-Out" -> StatusPenitipan.SIAP_CHECKOUT;
            default -> throw new IllegalArgumentException("Status tidak dikenal: " + label);
        };
    }

    private String displayServiceLabel(String label) {
        return switch (label) {
            case "Grooming" -> "Perawatan";
            case "Boarding" -> "Penitipan";
            case "Full Service" -> "Layanan Lengkap";
            default -> label;
        };
    }

    private String displayStatusLabel(String label) {
        return switch (label) {
            case "Check-In" -> "Check-In";
            case "Sedang Perawatan" -> "Sedang Dirawat";
            case "Check-Out" -> "Selesai";
            default -> label;
        };
    }

    private String formatCurrency(java.math.BigDecimal amount) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).format(amount);
    }

    private void showError(Exception exception) {
        JOptionPane.showMessageDialog(view, exception.getMessage(),
                "Tidak dapat memproses permintaan", JOptionPane.ERROR_MESSAGE);
    }
}
