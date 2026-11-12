package controller;

import model.AnimalFactory;
import model.BookingStatus;
import model.Pet;
import model.PetBooking;
import model.PetType;
import model.ServiceType;
import service.BookingService;
import view.PetCareMainFrame;

import javax.swing.JOptionPane;
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
        view.getCheckoutButton().addActionListener(event -> checkout());
        view.getSearchButton().addActionListener(event -> searchBookings());
        view.getSearchField().addActionListener(event -> searchBookings());
        view.getDeleteButton().addActionListener(event -> deleteBooking());
        view.getResetButton().addActionListener(event -> resetForm());
        view.getPages().addChangeListener(event -> syncSelectedControls());
        for (JTable table : view.getTables()) {
            table.getSelectionModel().addListSelectionListener(this::selectBooking);
        }
        view.getOwnerPhoneField().addKeyListener(new KeyAdapter() {
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
            int bookingId = Integer.parseInt(view.getBookingIdField().getText().trim());
            PetType petType = PetType.fromLabel((String) view.getPetTypeBox().getSelectedItem());
            Pet pet = AnimalFactory.create(petType, view.getPetNameField().getText().trim());
            service.register(
                    bookingId,
                    view.getOwnerNameField().getText().trim(),
                    Integer.parseInt(view.getOwnerPhoneField().getText().trim()),
                    pet,
                    ServiceType.fromLabel((String) view.getServiceBox().getSelectedItem()),
                    selectedDurationDays());
            refreshTable(view.getSearchField().getText());
            clearForm();
            JOptionPane.showMessageDialog(view, "Booking berhasil disimpan.",
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
            BookingStatus selectedStatus = statusFromLabel(
                    (String) view.getStatusBox().getSelectedItem());
            String nextStatus = service.updateStatus(booking.getBookingId(), selectedStatus).getLabel();
            refreshTable(view.getSearchField().getText());
            JOptionPane.showMessageDialog(view, "Status diperbarui menjadi " + nextStatus + ".",
                    "Update Status", JOptionPane.INFORMATION_MESSAGE);
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
            PetType petType = PetType.fromLabel((String) view.getPetTypeBox().getSelectedItem());
            Pet pet = AnimalFactory.create(petType, view.getPetNameField().getText().trim());
            service.updateBooking(
                    selected.getBookingId(),
                    view.getOwnerNameField().getText().trim(),
                    Integer.parseInt(view.getOwnerPhoneField().getText().trim()),
                    pet,
                    ServiceType.fromLabel((String) view.getServiceBox().getSelectedItem()),
                    selectedDurationDays());
            refreshTable(view.getSearchField().getText());
            clearForm();
            JOptionPane.showMessageDialog(view, "Data booking berhasil diperbarui.",
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
            PetBooking booking = service.checkout(selected.getBookingId());
            refreshTable(view.getSearchField().getText());
            String invoice = "======= INVOICE PET CARE & HOTEL =======\n"
                    + "ID Booking : " + booking.getBookingId() + "\n"
                    + "Pemilik    : " + booking.getOwnerName() + "\n"
                    + "Peliharaan : " + booking.getPetName() + " ("
                    + booking.getPetType().getLabel() + ")\n"
                    + "Layanan    : " + booking.getServiceType().getLabel() + "\n"
                    + (booking.getServiceType() == ServiceType.GROOMING
                    ? "TOTAL LAYANAN: " + formatCurrency(booking.getTotalBill()) + "\n"
                    : "Durasi     : " + booking.getDurationDays() + " hari\n"
                    + "Tarif/hari : " + formatCurrency(booking.getDailyRate()) + "\n"
                    + "Biaya jasa : " + formatCurrency(booking.getServiceFee()) + "\n"
                    + "TOTAL      : " + formatCurrency(booking.getTotalBill()) + "\n")
                    + "========================================";
            view.showCheckoutInvoice(invoice);
            view.getPages().setSelectedIndex(5);
            JOptionPane.showMessageDialog(view, invoice, "Invoice Tagihan",
                    JOptionPane.INFORMATION_MESSAGE);
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
                "Hapus booking ID " + booking.getBookingId() + "?",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            service.deleteBooking(booking.getBookingId());
            refreshTable(view.getSearchField().getText());
            clearForm();
            JOptionPane.showMessageDialog(view, "Booking berhasil dihapus.",
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
            JOptionPane.showMessageDialog(view, "Pilih booking pada tabel terlebih dahulu.",
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        int bookingId = (int) view.getTableModel().getValueAt(modelRow, 0);
        PetBooking booking = service.findById(bookingId);
        if (booking == null) {
            JOptionPane.showMessageDialog(view, "Booking tidak ditemukan.",
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
        }
        return booking;
    }

    private void selectBooking(ListSelectionEvent event) {
        if (event.getValueIsAdjusting()) {
            return;
        }
        PetBooking booking = selectedBookingWithoutWarning();
        if (booking == null) {
            view.setUpdateDataEnabled(false);
            view.getStatusBox().setEnabled(false);
            return;
        }
        view.getBookingIdField().setText(Integer.toString(booking.getBookingId()));
        view.getBookingIdField().setEditable(false);
        view.getOwnerNameField().setText(booking.getOwnerName());
        view.getOwnerPhoneField().setText(Integer.toString(booking.getOwnerPhone()));
        view.getPetNameField().setText(booking.getPetName());
        view.getPetTypeBox().setSelectedItem(booking.getPetType().getLabel());
        view.getServiceBox().setSelectedItem(booking.getServiceType().getLabel());
        view.getDurationSpinner().setValue(booking.getDurationDays());
        setSelectedStatus(booking);
        view.setUpdateDataEnabled(booking.getStatus() != BookingStatus.CHECKED_OUT);
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
        view.getBookingIdField().setText("");
        view.getBookingIdField().setEditable(true);
        view.getOwnerNameField().setText("");
        view.getOwnerPhoneField().setText("");
        view.getPetNameField().setText("");
        view.getPetTypeBox().setSelectedIndex(0);
        view.getServiceBox().setSelectedIndex(0);
        view.getDurationSpinner().setValue(1);
        view.getStatusBox().setEnabled(false);
        view.setUpdateDataEnabled(false);
        for (JTable table : view.getTables()) {
            table.clearSelection();
        }
    }

    private int selectedDurationDays() {
        return view.getServiceBox().getSelectedItem().equals("Grooming")
                ? 1 : (Integer) view.getDurationSpinner().getValue();
    }

    private void syncSelectedControls() {
        PetBooking booking = selectedBookingWithoutWarning();
        if (booking == null) {
            view.getStatusBox().setEnabled(false);
            view.setUpdateDataEnabled(false);
            return;
        }
        setSelectedStatus(booking);
        view.setUpdateDataEnabled(booking.getStatus() != BookingStatus.CHECKED_OUT);
    }

    private void setSelectedStatus(PetBooking booking) {
        loadingSelectedStatus = true;
        try {
            view.getStatusBox().setSelectedItem(booking.getStatus().getLabel());
        } finally {
            loadingSelectedStatus = false;
        }
        view.getStatusBox().setEnabled(view.isStatusManagementPage()
                && booking.getStatus() != BookingStatus.CHECKED_OUT);
    }

    private BookingStatus statusFromLabel(String label) {
        for (BookingStatus status : BookingStatus.values()) {
            if (status.getLabel().equals(label)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Status booking tidak dikenal: " + label);
    }

    private String formatCurrency(java.math.BigDecimal amount) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).format(amount);
    }

    private void showError(Exception exception) {
        JOptionPane.showMessageDialog(view, exception.getMessage(),
                "Tidak dapat memproses permintaan", JOptionPane.ERROR_MESSAGE);
    }
}
