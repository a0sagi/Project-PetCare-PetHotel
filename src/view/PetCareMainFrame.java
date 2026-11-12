package view;

import model.BookingStatus;
import model.PetBooking;
import model.ServiceType;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SpinnerNumberModel;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PetCareMainFrame extends JFrame {
    private static final String[] TAB_NAMES = {
            "Dashboard", "Data Pemilik", "Data Hewan", "Pet Care",
            "Pet Hotel", "Checkout"
    };

    private final JTextField bookingIdField = new JTextField(16);
    private final JTextField ownerNameField = new JTextField(16);
    private final JTextField ownerPhoneField = new JTextField(16);
    private final JTextField petNameField = new JTextField(16);
    private final JTextField searchField = new JTextField(20);
    private final JComboBox<String> petTypeBox =
            new JComboBox<>(new String[]{"Kucing", "Anjing", "Kelinci"});
    private final JComboBox<String> serviceBox =
            new JComboBox<>(new String[]{"Grooming", "Boarding", "Full Service"});
    private final JComboBox<String> statusBox =
            new JComboBox<>(new String[]{"Check-In", "Sedang Perawatan", "Siap Check-Out"});
    private final JSpinner durationSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 365, 1));
    private final JLabel durationLabel = new JLabel("Durasi (hari)");
    private final JLabel statusLabel = new JLabel("Status");
    private final JButton addBookingButton = new JButton("Tambah Booking");
    private final JButton updateDataButton = new JButton("Update Data");
    private final JButton checkoutButton = new JButton("Checkout & Invoice");
    private final JButton searchButton = new JButton("Cari");
    private final JButton deleteButton = new JButton("Hapus");
    private final JButton resetButton = new JButton("Reset");
    private final BookingTableModel tableModel = new BookingTableModel();
    private final JTabbedPane pages = new JTabbedPane();
    private final List<JTable> tables = new ArrayList<>();
    private final List<TableRowSorter<BookingTableModel>> sorters = new ArrayList<>();
    private final JLabel totalCount = new JLabel("0");
    private final JLabel careCount = new JLabel("0");
    private final JLabel hotelCount = new JLabel("0");
    private final JLabel checkoutCount = new JLabel("0");
    private final JTextArea checkoutInvoice = new JTextArea(
            "Pilih booking berstatus Siap Check-Out, lalu tekan Checkout untuk melihat invoice.");

    public PetCareMainFrame() {
        super("Pet Care & Pet Hotel");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(12, 12));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        add(createHeader(), BorderLayout.NORTH);
        add(createFormPanel(), BorderLayout.WEST);
        add(createContentPanel(), BorderLayout.CENTER);
        setMinimumSize(new java.awt.Dimension(1100, 650));
        setSize(1240, 730);
        setLocationRelativeTo(null);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Pet Care & Pet Hotel");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        JLabel date = new JLabel(LocalDate.now()
                .format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", new Locale("id", "ID"))));
        date.setForeground(new Color(95, 107, 120));
        header.add(title, BorderLayout.WEST);
        header.add(date, BorderLayout.EAST);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        return header;
    }

    private JPanel createContentPanel() {
        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.add(createToolbar(), BorderLayout.NORTH);
        for (int index = 0; index < TAB_NAMES.length; index++) {
            pages.addTab(TAB_NAMES[index], createPage(index));
        }
        pages.addChangeListener(event -> updateStatusControlsVisibility());
        updateStatusControlsVisibility();
        content.add(pages, BorderLayout.CENTER);
        return content;
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        toolbar.add(new JLabel("Cari pemilik / hewan / ID"));
        toolbar.add(searchField);
        toolbar.add(searchButton);
        toolbar.add(deleteButton);
        toolbar.add(statusLabel);
        toolbar.add(statusBox);
        toolbar.add(checkoutButton);
        toolbar.add(resetButton);
        return toolbar;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Pendaftaran Layanan"));

        JPanel form = new JPanel(new GridBagLayout());
        String[] labels = {"ID Booking", "Nama Pemilik", "No. HP", "Nama Hewan",
                "Jenis Hewan", "Layanan", "Durasi (hari)"};
        java.awt.Component[] fields = {bookingIdField, ownerNameField, ownerPhoneField,
                petNameField, petTypeBox, serviceBox, durationSpinner};
        for (int row = 0; row < labels.length; row++) {
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.insets = new Insets(6, 5, 6, 5);
            constraints.anchor = GridBagConstraints.LINE_START;
            constraints.gridx = 0;
            constraints.gridy = row;
            form.add(row == labels.length - 1 ? durationLabel : new JLabel(labels[row]), constraints);
            constraints.gridx = 1;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.weightx = 1;
            form.add(fields[row], constraints);
        }
        JLabel hint = new JLabel("<html>Isi data booking, lalu tekan Tambah Booking.<br>"
                + "Pilih baris tabel untuk update status,<br>tagihan, atau checkout.</html>");
        hint.setForeground(new Color(95, 107, 120));
        hint.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        actions.add(addBookingButton);
        updateDataButton.setEnabled(false);
        actions.add(updateDataButton);
        GridBagConstraints panelConstraints = new GridBagConstraints();
        panelConstraints.gridx = 0;
        panelConstraints.gridy = 0;
        panelConstraints.weightx = 1;
        panelConstraints.fill = GridBagConstraints.HORIZONTAL;
        panelConstraints.insets = new Insets(0, 4, 0, 4);
        panel.add(form, panelConstraints);
        panelConstraints.gridy = 1;
        panelConstraints.insets = new Insets(4, 4, 0, 4);
        panel.add(actions, panelConstraints);
        panelConstraints.gridy = 2;
        panelConstraints.insets = new Insets(4, 4, 0, 4);
        panel.add(hint, panelConstraints);
        panelConstraints.gridy = 3;
        panelConstraints.weighty = 1;
        panelConstraints.fill = GridBagConstraints.BOTH;
        panel.add(new JPanel(), panelConstraints);
        panel.setPreferredSize(new java.awt.Dimension(340, 420));
        serviceBox.addActionListener(event -> updateDurationVisibility());
        updateDurationVisibility();
        return panel;
    }

    private void updateDurationVisibility() {
        boolean durationRequired = !"Grooming".equals(serviceBox.getSelectedItem());
        durationLabel.setVisible(durationRequired);
        durationSpinner.setVisible(durationRequired);
        revalidate();
        repaint();
    }

    private void updateStatusControlsVisibility() {
        boolean visible = isStatusManagementPage();
        statusLabel.setVisible(visible);
        statusBox.setVisible(visible);
        checkoutButton.setVisible(isCheckoutActionPage());
        if (!visible) {
            statusBox.setEnabled(false);
        }
        revalidate();
        repaint();
    }

    private JPanel createPage(int index) {
        JPanel page = new JPanel(new BorderLayout(8, 8));
        if (index == 0) {
            page.add(createDashboardSummary(), BorderLayout.NORTH);
        } else {
            JLabel description = new JLabel(pageDescription(index));
            description.setBorder(BorderFactory.createEmptyBorder(6, 4, 2, 4));
            description.setForeground(new Color(95, 107, 120));
            page.add(description, BorderLayout.NORTH);
        }

        JTable table = new JTable(tableModel);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        TableRowSorter<BookingTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        sorters.add(sorter);
        table.setFillsViewportHeight(true);
        table.setRowHeight(26);
        configureColumns(table, index);
        tables.add(table);
        page.add(new JScrollPane(table), BorderLayout.CENTER);
        if (index == 5) {
            checkoutInvoice.setEditable(false);
            checkoutInvoice.setLineWrap(true);
            checkoutInvoice.setWrapStyleWord(true);
            checkoutInvoice.setRows(5);
            checkoutInvoice.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            checkoutInvoice.setBorder(BorderFactory.createTitledBorder("Invoice Checkout Terakhir"));
            page.add(new JScrollPane(checkoutInvoice), BorderLayout.SOUTH);
        }
        return page;
    }

    private JPanel createDashboardSummary() {
        JPanel summary = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        summary.add(statCard("Semua Booking", totalCount));
        summary.add(statCard("Pet Care", careCount));
        summary.add(statCard("Pet Hotel", hotelCount));
        summary.add(statCard("Selesai Checkout", checkoutCount));
        return summary;
    }

    private JPanel statCard(String title, JLabel value) {
        JPanel card = new JPanel(new BorderLayout(8, 4));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 231)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        value.setFont(value.getFont().deriveFont(Font.BOLD, 20f));
        card.add(new JLabel(title), BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    private String pageDescription(int index) {
        return switch (index) {
            case 1 -> "Daftar pemilik beserta nomor kontak dan booking terkait.";
            case 2 -> "Daftar hewan dan pemilik yang terdaftar pada booking.";
            case 3 -> "Layanan grooming dan perawatan hewan.";
            case 4 -> "Layanan penitipan dan hotel hewan.";
            case 5 -> "Booking siap checkout dan yang sudah selesai.";
            default -> "";
        };
    }

    private void configureColumns(JTable table, int pageIndex) {
        int[] visibleColumns = switch (pageIndex) {
            case 1 -> new int[]{0, 1, 2};
            case 2 -> new int[]{0, 1, 2, 3, 4};
            case 5 -> new int[]{0, 1, 3, 5, 7, 8};
            default -> new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8};
        };
        for (int column = table.getColumnModel().getColumnCount() - 1; column >= 0; column--) {
            TableColumn tableColumn = table.getColumnModel().getColumn(column);
            if (!contains(visibleColumns, tableColumn.getModelIndex())) {
                table.removeColumn(tableColumn);
            }
        }
    }

    private boolean contains(int[] values, int sought) {
        for (int value : values) {
            if (value == sought) {
                return true;
            }
        }
        return false;
    }

    public void showBookings(List<PetBooking> bookings) {
        tableModel.setBookings(bookings);
        totalCount.setText(Integer.toString(bookings.size()));
        careCount.setText(Long.toString(bookings.stream()
                .filter(booking -> booking.getServiceType() != ServiceType.BOARDING).count()));
        hotelCount.setText(Long.toString(bookings.stream()
                .filter(booking -> booking.getServiceType() != ServiceType.GROOMING).count()));
        checkoutCount.setText(Long.toString(bookings.stream()
                .filter(booking -> booking.getStatus() == BookingStatus.CHECKED_OUT).count()));
    }

    public void filterPages(String query) {
        String search = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        for (int pageIndex = 0; pageIndex < sorters.size(); pageIndex++) {
            final int selectedPage = pageIndex;
            final String searchTerm = search;
            sorters.get(pageIndex).setRowFilter(new RowFilter<BookingTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends BookingTableModel, ? extends Integer> entry) {
                    if (selectedPage == 3 && "Boarding".equals(entry.getStringValue(5))) {
                        return false;
                    }
                    if (selectedPage == 4 && "Grooming".equals(entry.getStringValue(5))) {
                        return false;
                    }
                    if (selectedPage == 5
                            && !"Selesai".equals(entry.getStringValue(7))
                            && !"Siap Check-Out".equals(entry.getStringValue(7))) {
                        return false;
                    }
                    if (searchTerm.isEmpty()) {
                        return true;
                    }
                    for (int column = 0; column < entry.getValueCount(); column++) {
                        if (String.valueOf(entry.getValue(column)).toLowerCase(Locale.ROOT)
                                .contains(searchTerm)) {
                            return true;
                        }
                    }
                    return false;
                }
            });
        }
    }

    public JTextField getBookingIdField() { return bookingIdField; }
    public JTextField getOwnerNameField() { return ownerNameField; }
    public JTextField getOwnerPhoneField() { return ownerPhoneField; }
    public JTextField getPetNameField() { return petNameField; }
    public JComboBox<String> getPetTypeBox() { return petTypeBox; }
    public JComboBox<String> getServiceBox() { return serviceBox; }
    public JSpinner getDurationSpinner() { return durationSpinner; }
    public JTextField getSearchField() { return searchField; }
    public JButton getAddBookingButton() { return addBookingButton; }
    public JButton getUpdateDataButton() { return updateDataButton; }
    public JComboBox<String> getStatusBox() { return statusBox; }
    public boolean isStatusManagementPage() {
        return pages.getSelectedIndex() == 3 || pages.getSelectedIndex() == 4;
    }
    public boolean isCheckoutActionPage() {
        int selectedPage = pages.getSelectedIndex();
        return selectedPage == 0 || selectedPage == 3 || selectedPage == 4;
    }
    public void setUpdateDataEnabled(boolean enabled) { updateDataButton.setEnabled(enabled); }
    public JButton getCheckoutButton() { return checkoutButton; }
    public JButton getSearchButton() { return searchButton; }
    public JButton getDeleteButton() { return deleteButton; }
    public JButton getResetButton() { return resetButton; }
    public JTable getSelectedBookingsTable() { return tables.get(pages.getSelectedIndex()); }
    public List<JTable> getTables() { return new ArrayList<>(tables); }
    public JTabbedPane getPages() { return pages; }
    public BookingTableModel getTableModel() { return tableModel; }
    public void showCheckoutInvoice(String invoice) { checkoutInvoice.setText(invoice); }
}
