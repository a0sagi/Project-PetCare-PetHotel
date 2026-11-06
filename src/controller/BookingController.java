package controller;

import model.Hewan;
import model.AnimalFactory;
import model.PemesananHewan;
import model.PetType;
import model.JenisLayanan;
import service.LayananPemesanan;
import view.PetCareMainFrame;

import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;

public class BookingController {
    private final PetCareMainFrame view;
    private final LayananPemesanan service;

    public BookingController(PetCareMainFrame view, LayananPemesanan service) {
        this.view = view;
        this.service = service;
        bindEvents();
        refreshTable();
    }

    private void bindEvents() {
        view.getCheckInButton().addActionListener(event -> registerBooking());
        view.getUpdateStatusButton().addActionListener(event -> advanceStatus());
        view.getCheckoutButton().addActionListener(event -> checkout());
        view.getResetButton().addActionListener(event -> clearForm());
        view.getBookingsTable().getSelectionModel().addListSelectionListener(this::selectBooking);
        view.getOwnerPhoneField().addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent event) {
                if (!Character.isDigit(event.getKeyChar()) && !Character.isISOControl(event.getKeyChar())) {
                    event.consume();
                }
            }
        });
    }

    private void registerBooking() {
        try {
            int bookingId = Integer.parseInt(view.getBookingIdField().getText().trim());
            PetType petType = PetType.fromLabel((String) view.getPetTypeBox().getSelectedItem());
            Hewan animal = AnimalFactory.create(petType, view.getPetNameField().getText().trim());
            PemesananHewan booking = service.register(
                    bookingId,
                    view.getOwnerNameField().getText().trim(),
                    Integer.parseInt(view.getOwnerPhoneField().getText().trim()),
                    animal,
                    JenisLayanan.fromLabel((String) view.getServiceBox().getSelectedItem()),
                    (Integer) view.getDurationSpinner().getValue());
            view.getTableModel().addBooking(booking);
            clearForm();
            JOptionPane.showMessageDialog(view, "Pendaftaran berhasil disimpan.",
                    "Berhasil", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException | SQLException exception) {
            showError(exception);
        }
    }

    private void advanceStatus() {
        PemesananHewan booking = selectedBooking();
        if (booking == null) {
            return;
        }
        try {
            service.advanceStatus(booking.getBookingId());
            refreshTable();
        } catch (IllegalArgumentException | IllegalStateException | SQLException exception) {
            showError(exception);
        }
    }

    private void checkout() {
        PemesananHewan selected = selectedBooking();
        if (selected == null) {
            return;
        }
        try {
            PemesananHewan booking = service.checkout(selected.getBookingId());
            refreshTable();
            String invoice = "======= INVOICE PET CARE & HOTEL =======\n"
                    + "ID Booking : " + booking.getBookingId() + "\n"
                    + "Pemilik    : " + booking.getOwnerName() + "\n"
                    + "Peliharaan : " + booking.getPetName() + " ("
                    + booking.getPetType().getLabel() + ")\n"
                    + "Layanan    : " + booking.getServiceType().getLabel() + "\n"
                    + "Durasi     : " + booking.getDurationDays() + " hari\n"
                    + "Tarif/hari : " + formatCurrency(booking.getDailyRate()) + "\n"
                    + "Biaya jasa : " + formatCurrency(booking.getServiceFee()) + "\n"
                    + "TOTAL      : " + formatCurrency(booking.getTotalBill()) + "\n"
                    + "========================================";
            JOptionPane.showMessageDialog(view, invoice, "Invoice Tagihan",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException | IllegalStateException | SQLException exception) {
            showError(exception);
        }
    }

    private PemesananHewan selectedBooking() {
        int selectedRow = view.getBookingsTable().getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(view, "Pilih booking pada tabel terlebih dahulu.",
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int modelRow = view.getBookingsTable().convertRowIndexToModel(selectedRow);
        int bookingId = (int) view.getTableModel().getValueAt(modelRow, 0);
        return service.findById(bookingId);
    }

    private void selectBooking(ListSelectionEvent event) {
        if (event.getValueIsAdjusting()) {
            return;
        }
        PemesananHewan booking = selectedBookingWithoutWarning();
        if (booking != null) {
            view.getBookingIdField().setText(Integer.toString(booking.getBookingId()));
            view.getOwnerNameField().setText(booking.getOwnerName());
            view.getOwnerPhoneField().setText(Integer.toString(booking.getOwnerPhone()));
            view.getPetNameField().setText(booking.getPetName());
            view.getPetTypeBox().setSelectedItem(booking.getPetType().getLabel());
            view.getServiceBox().setSelectedItem(booking.getServiceType().getLabel());
            view.getDurationSpinner().setValue(booking.getDurationDays());
        }
    }

    private PemesananHewan selectedBookingWithoutWarning() {
        int selectedRow = view.getBookingsTable().getSelectedRow();
        if (selectedRow < 0) {
            return null;
        }
        int modelRow = view.getBookingsTable().convertRowIndexToModel(selectedRow);
        int bookingId = (int) view.getTableModel().getValueAt(modelRow, 0);
        return service.findById(bookingId);
    }

    private void refreshTable() {
        view.getTableModel().setRowCount(0);
        for (PemesananHewan booking : service.getAllBookings()) {
            view.getTableModel().addBooking(booking);
        }
    }

    private void clearForm() {
        view.getBookingIdField().setText("");
        view.getOwnerNameField().setText("");
        view.getOwnerPhoneField().setText("");
        view.getPetNameField().setText("");
        view.getPetTypeBox().setSelectedIndex(0);
        view.getServiceBox().setSelectedIndex(0);
        view.getDurationSpinner().setValue(1);
        view.getBookingsTable().clearSelection();
    }

    private String formatCurrency(java.math.BigDecimal amount) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).format(amount);
    }

    private void showError(Exception exception) {
        JOptionPane.showMessageDialog(view, exception.getMessage(),
                "Tidak dapat memproses permintaan", JOptionPane.ERROR_MESSAGE);
    }
}
