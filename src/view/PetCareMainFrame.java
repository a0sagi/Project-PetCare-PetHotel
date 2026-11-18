package view;

import model.JenisLayanan;
import model.PetBooking;
import model.StatusPenitipan;

import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PetCareMainFrame extends JFrame {
    private static final String[] TAB_NAMES = {
            "Ringkasan", "Data Pemilik", "Data Hewan", "Perawatan Hewan",
            "Penitipan Hewan", "Pelunasan"
    };

    private final JTextField idBookingField = new JTextField(16);
    private final JTextField namaPemilikField = new JTextField(16);
    private final JTextField nomorTelponField = new JTextField(16);
    private final JTextField namaHewanField = new JTextField(16);
    private final JTextField catatanKhususField = new JTextField(16);
    private final JTextField searchField = new JTextField(20);
    private final JComboBox<String> jenisHewanBox =
            new JComboBox<>(new String[]{"Kucing", "Anjing", "Kelinci"});
    private final JComboBox<String> serviceBox =
            new JComboBox<>(new String[]{"Perawatan", "Penitipan", "Layanan Lengkap"});
    private final JComboBox<String> statusBox =
            new JComboBox<>(new String[]{"Masuk", "Sedang Dirawat"});
    private final JSpinner durationSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 365, 1));
    private final JLabel durationLabel = new JLabel("Durasi (hari)");
    private final JLabel statusLabel = new JLabel("Status");
    private final JButton addBookingButton = new JButton("Tambah Pemesanan");
    private final JButton updateDataButton = new JButton("Perbarui Data");
    private final JButton checkoutButton = new JButton("Proses Pelunasan");
    private final JButton searchButton = new JButton("Cari");
    private final JButton deleteButton = new JButton("Hapus");
    private final JButton resetButton = new JButton("Atur Ulang");
    private final BookingTableModel tableModel = new BookingTableModel();
    private final JTabbedPane pages = new JTabbedPane();
    private final List<JTable> tables = new ArrayList<>();
    private final List<TableRowSorter<BookingTableModel>> sorters = new ArrayList<>();
    private final JLabel totalCount = new JLabel("0");
    private final JLabel careCount = new JLabel("0");
    private final JLabel hotelCount = new JLabel("0");
    private final JLabel checkoutCount = new JLabel("0");
    private final JTextArea checkoutInvoice = new JTextArea(
            "Pilih pemesanan berstatus Sedang Dirawat, lalu tekan Proses Pelunasan untuk melihat rincian tagihan.");

    public PetCareMainFrame() {
        super("Pet Care & Hotel Management");
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
        JLabel title = new JLabel("Pet Care & Hotel Management");
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
        toolbar.add(new JLabel("Cari pemilik / hewan / ID pemesanan"));
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
        String[] labels = {"ID Pemesanan", "Nama Pemilik", "No. HP", "Nama Hewan",
                "Catatan Khusus", "Jenis Hewan", "Layanan", "Durasi (hari)"};
        java.awt.Component[] fields = {idBookingField, namaPemilikField, nomorTelponField,
                namaHewanField, catatanKhususField, jenisHewanBox, serviceBox, durationSpinner};
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
        JLabel hint = new JLabel("<html>Isi data pemesanan, lalu tekan Tambah Pemesanan.<br>"
                + "Pilih baris tabel untuk memperbarui status,<br>tagihan, atau proses pelunasan.</html>");
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
        boolean durationRequired = serviceBox.getSelectedIndex() != 0;
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
            checkoutInvoice.setBorder(BorderFactory.createTitledBorder("Rincian Tagihan Terakhir"));
            page.add(new JScrollPane(checkoutInvoice), BorderLayout.SOUTH);
        }
        return page;
    }

    private JPanel createDashboardSummary() {
        JPanel summary = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        summary.add(statCard("Semua Pemesanan", totalCount));
        summary.add(statCard("Perawatan Hewan", careCount));
        summary.add(statCard("Penitipan Hewan", hotelCount));
        summary.add(statCard("Pelunasan Selesai", checkoutCount));
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
            case 1 -> "Daftar pemilik beserta nomor kontak dan pemesanan terkait.";
            case 2 -> "Daftar hewan dan pemilik yang terdaftar pada pemesanan.";
            case 3 -> "Layanan perawatan dan kebersihan hewan.";
            case 4 -> "Layanan penitipan dan penginapan hewan.";
            case 5 -> "Pemesanan yang siap dilunasi dan yang sudah selesai.";
            default -> "";
        };
    }

    private void configureColumns(JTable table, int pageIndex) {
        int[] visibleColumns = switch (pageIndex) {
            case 1 -> new int[]{0, 1, 2};
            case 2 -> new int[]{0, 1, 2, 3, 4, 5};
            case 5 -> new int[]{0, 1, 3, 6, 8, 9};
            default -> new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
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
                .filter(booking -> booking.getJenisLayanan() != JenisLayanan.BOARDING).count()));
        hotelCount.setText(Long.toString(bookings.stream()
                .filter(booking -> booking.getJenisLayanan() != JenisLayanan.GROOMING).count()));
        checkoutCount.setText(Long.toString(bookings.stream()
                .filter(booking -> booking.getStatus() == StatusPenitipan.CHECK_OUT).count()));
    }

    public void filterPages(String query) {
        String search = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        for (int pageIndex = 0; pageIndex < sorters.size(); pageIndex++) {
            final int selectedPage = pageIndex;
            final String searchTerm = search;
            sorters.get(pageIndex).setRowFilter(new RowFilter<BookingTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends BookingTableModel, ? extends Integer> entry) {
                    if (selectedPage == 3 && "Penitipan".equals(entry.getStringValue(6))) {
                        return false;
                    }
                    if (selectedPage == 4 && "Perawatan".equals(entry.getStringValue(6))) {
                        return false;
                    }
                    if (selectedPage == 5
                            && !"Selesai".equals(entry.getStringValue(8))
                            && !"Sedang Dirawat".equals(entry.getStringValue(8))) {
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

    public JTextField getIdBookingField()           { return idBookingField; }
    public JTextField getNamaPemilikField()          { return namaPemilikField; }
    public JTextField getNomorTelponField()          { return nomorTelponField; }
    public JTextField getNamaHewanField()            { return namaHewanField; }
    public JTextField getCatatanKhususField()        { return catatanKhususField; }
    public JComboBox<String> getJenisHewanBox()      { return jenisHewanBox; }
    public JComboBox<String> getServiceBox()         { return serviceBox; }
    public JSpinner getDurationSpinner()             { return durationSpinner; }
    public JTextField getSearchField()               { return searchField; }
    public JButton getAddBookingButton()             { return addBookingButton; }
    public JButton getUpdateDataButton()             { return updateDataButton; }
    public JComboBox<String> getStatusBox()          { return statusBox; }
    public boolean isStatusManagementPage() {
        return pages.getSelectedIndex() == 3 || pages.getSelectedIndex() == 4;
    }
    public boolean isCheckoutActionPage() {
        int selectedPage = pages.getSelectedIndex();
        return selectedPage == 0 || selectedPage == 3 || selectedPage == 4;
    }
    public void setUpdateDataEnabled(boolean enabled) { updateDataButton.setEnabled(enabled); }
    public JButton getCheckoutButton()               { return checkoutButton; }
    public JButton getSearchButton()                 { return searchButton; }
    public JButton getDeleteButton()                 { return deleteButton; }
    public JButton getResetButton()                  { return resetButton; }
    public JTable getSelectedBookingsTable()         { return tables.get(pages.getSelectedIndex()); }
    public List<JTable> getTables()                  { return new ArrayList<>(tables); }
    public JTabbedPane getPages()                    { return pages; }
    public BookingTableModel getTableModel()         { return tableModel; }
    public void showCheckoutInvoice(String invoice)  { checkoutInvoice.setText(invoice); }
}
