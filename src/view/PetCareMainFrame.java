package view;

import model.JenisLayanan;
import model.PetBooking;
import model.StatusPenitipan;

import com.formdev.flatlaf.ui.FlatDropShadowBorder;
import javax.swing.border.AbstractBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PetCareMainFrame extends JFrame {
    private static final Color CREAM = new Color(0xFAF9F2);
    private static final Color SAGE = new Color(0xB6CCB0);
    private static final Color DARK_SAGE = new Color(0x7C9A76);
    private static final Color PINK = new Color(0xF8E6F0);
    private static final Color STATUS_CHECKIN = new Color(0xDCFFD4); //warna check-in
    private static final Color STATUS_DONE = new Color(0xBABABA); //warna sudah selesai
    private static final Color STATUS_CARE = new Color(0xFFF0CC); //warna sedang dirawat
    private static final Color BORDER = new Color(0x8EA888);
    private static final Color TEXT = new Color(0x2B2F2B);
    private static final Color SECONDARY_TEXT = new Color(0x5A625A);

    private static final String[] TAB_NAMES = {
            "Ringkasan", "Data Pemilik", "Data Hewan", "Perawatan Hewan",
            "Penitipan Hewan", "Tagihan"
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
            new JComboBox<>(new String[]{
                    "Check-In", "Sedang Dirawat", "Siap Check-Out"
            });
    private final JSpinner durationSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 365, 1));
    private final JLabel durationLabel = new JLabel("Durasi (hari)");
    private final JLabel statusLabel = new JLabel("Status");
    private final JButton addBookingButton = new JButton("Tambah Pemesanan");
    private final JButton updateDataButton = new JButton("Perbarui Data");
    private final JButton checkoutButton = new JButton("Check Out");
    private final JButton boardingCheckoutButton = new JButton("Check Out");
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
            "Pilih pemesanan berstatus Siap Check-Out, lalu tekan Check Out untuk melihat tagihan.");

    public PetCareMainFrame() {
        super("Pet Care & Hotel Management");
        setCheckoutEnabled(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setBackground(CREAM);
        setLayout(new BorderLayout());
        add(createHeader(), BorderLayout.NORTH);
        JPanel workspace = new JPanel(new BorderLayout(14, 0));
        workspace.setBackground(CREAM);
        workspace.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));
        workspace.add(createFormPanel(), BorderLayout.WEST);
        workspace.add(createContentPanel(), BorderLayout.CENTER);
        add(workspace, BorderLayout.CENTER);
        setMinimumSize(new java.awt.Dimension(1100, 650));
        setSize(1240, 730);
        setLocationRelativeTo(null);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(14, 0));
        header.setBackground(SAGE);
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brand.setOpaque(false);
        JLabel icon = new JLabel(new HousePawIcon());
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel petCare = new JLabel("Pet Care");
        petCare.setFont(petCare.getFont().deriveFont(Font.BOLD, 26f));
        petCare.setForeground(TEXT);
        JLabel subtitle = new JLabel("& Hotel Management");
        subtitle.setFont(subtitle.getFont().deriveFont(Font.BOLD, 17f));
        subtitle.setForeground(TEXT);
        titles.add(petCare);
        titles.add(subtitle);
        brand.add(icon);
        brand.add(titles);
        JLabel date = new JLabel(LocalDate.now()
                .format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", new Locale("id", "ID"))));
        date.setForeground(SECONDARY_TEXT);
        header.add(brand, BorderLayout.WEST);
        header.add(date, BorderLayout.EAST);
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        return header;
    }

    private JPanel createContentPanel() {
        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBackground(CREAM);
        content.add(createToolbar(), BorderLayout.NORTH);
        for (int index = 0; index < TAB_NAMES.length; index++) {
            pages.addTab(TAB_NAMES[index], createPage(index));
        }
        pages.putClientProperty("FlatLaf.style",
                "selectedBackground: #B6CCB0; underlineColor: #7C9A76; tabArc: 12; tabHeight: 32");
        pages.setBackground(CREAM);
        pages.setForeground(TEXT);
        pages.addChangeListener(event -> updateStatusControlsVisibility());
        updateStatusControlsVisibility();
        content.add(pages, BorderLayout.CENTER);
        return content;
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        toolbar.setBackground(CREAM);
        toolbar.add(new JLabel("Cari pemilik / hewan / ID pemesanan"));
        styleInput(searchField);
        styleButton(searchButton, SAGE, TEXT, false);
        styleButton(deleteButton, PINK, TEXT, false);
        styleButton(resetButton, Color.WHITE, TEXT, false);
        toolbar.add(searchField);
        toolbar.add(searchButton);
        toolbar.add(deleteButton);
        toolbar.add(statusLabel);
        toolbar.add(statusBox);
        toolbar.add(resetButton);
        return toolbar;
    }

    private JPanel createFormPanel() {
        RoundedPanel panel = new RoundedPanel(new BorderLayout(8, 8), 16, CREAM, true);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.setPreferredSize(new java.awt.Dimension(340, 420));
        RoundedPanel badge = new RoundedPanel(new FlowLayout(FlowLayout.LEFT, 8, 3),
                12, SAGE, false);
        badge.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        JLabel badgeTitle = new JLabel("Pendaftaran Layanan");
        badgeTitle.setForeground(TEXT);
        badgeTitle.setFont(badgeTitle.getFont().deriveFont(Font.BOLD));
        badge.add(badgeTitle);
        panel.add(badge, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        String[] labels = {"ID Pemesanan", "Nama Pemilik", "No. HP", "Nama Hewan",
                "Catatan Khusus", "Jenis Hewan", "Layanan", "Durasi (hari)"};
        java.awt.Component[] fields = {idBookingField, namaPemilikField, nomorTelponField,
                namaHewanField, catatanKhususField, jenisHewanBox, serviceBox, durationSpinner};
        for (java.awt.Component field : fields) {
            if (field instanceof JComponent) {
                styleInput((JComponent) field);
            }
        }
        styleInput(searchField);
        JComponent spinnerEditor = (JComponent) durationSpinner.getEditor();
        styleInput(spinnerEditor);
        if (spinnerEditor instanceof JSpinner.DefaultEditor) {
            styleInput(((JSpinner.DefaultEditor) spinnerEditor).getTextField());
        }
        styleButton(addBookingButton, DARK_SAGE, Color.WHITE, true);
        styleButton(updateDataButton, DARK_SAGE, Color.WHITE, false);
        updateDataButton.putClientProperty("FlatLaf.style",
                "arc: 12; disabledBackground: #B9CEB4; disabledTextColor: #5A625A");
        styleButton(checkoutButton, DARK_SAGE, Color.WHITE, true);
        styleButton(boardingCheckoutButton, DARK_SAGE, Color.WHITE, true);
        styleInput(statusBox);
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
                + "Pilih baris tabel untuk memperbarui status atau data.</html>");
        hint.setForeground(SECONDARY_TEXT);
        hint.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        actions.setOpaque(false);
        actions.add(addBookingButton);
        updateDataButton.setEnabled(false);
        actions.add(updateDataButton);
        JPanel body = new JPanel(new BorderLayout(4, 4));
        body.setOpaque(false);
        body.add(form, BorderLayout.NORTH);
        body.add(actions, BorderLayout.CENTER);
        body.add(hint, BorderLayout.SOUTH);
        panel.add(body, BorderLayout.CENTER);
        serviceBox.addActionListener(event -> updateDurationVisibility());
        updateDurationVisibility();
        return panel;
    }

    private void styleInput(JComponent component) {
        component.putClientProperty("FlatLaf.style",
                "arc: 12; borderColor: #8EA888; focusedBorderColor: #7C9A76; background: #FFFFFF");
        component.setForeground(TEXT);
    }

    private void styleButton(JButton button, Color background, Color foreground, boolean bold) {
        button.putClientProperty("FlatLaf.style", "arc: 12");
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        if (bold) {
            button.setFont(button.getFont().deriveFont(Font.BOLD));
        }
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
        if (!visible) {
            statusBox.setEnabled(false);
        }
        revalidate();
        repaint();
    }

    private JPanel createPage(int index) {
        JPanel page = new JPanel(new BorderLayout(8, 8));
        page.setBackground(CREAM);
        if (index == 0) {
            page.add(createDashboardSummary(), BorderLayout.NORTH);
        } else {
            JLabel description = new JLabel(pageDescription(index));
            description.setBorder(BorderFactory.createEmptyBorder(6, 4, 2, 4));
            description.setForeground(SECONDARY_TEXT);
            if (index == 3 || index == 4) {
                JPanel pageHeader = new JPanel(new BorderLayout());
                pageHeader.setBackground(CREAM);
                pageHeader.add(description, BorderLayout.CENTER);
                pageHeader.add(index == 3 ? checkoutButton : boardingCheckoutButton,
                        BorderLayout.EAST);
                page.add(pageHeader, BorderLayout.NORTH);
            } else {
                page.add(description, BorderLayout.NORTH);
            }
        }

        JTable table = new JTable(tableModel);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        TableRowSorter<BookingTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        sorters.add(sorter);
        table.setFillsViewportHeight(true);
        table.setRowHeight(26);
        table.setBackground(Color.WHITE);
        table.setForeground(TEXT);
        table.setSelectionBackground(SAGE);
        table.setSelectionForeground(TEXT);
        table.setGridColor(new Color(0xE2E9DF));
        table.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable source, Object value,
                    boolean selected, boolean focused, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        source, value, selected, focused, row, column);
                label.setForeground(TEXT);
                label.setBackground(statusColor(value));
                return label;
            }
        });
        table.getTableHeader().setBackground(SAGE);
        table.getTableHeader().setForeground(TEXT);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD));
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable source, Object value,
                    boolean selected, boolean focused, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        source, value, selected, focused, row, column);
                label.setOpaque(true);
                label.setBackground(SAGE);
                label.setForeground(TEXT);
                label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, BORDER));
                return label;
            }
        });
        configureColumns(table, index);
        tables.add(table);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBackground(CREAM);
        tableScroll.getViewport().setBackground(Color.WHITE);
        tableScroll.setBorder(BorderFactory.createCompoundBorder(
                new FlatDropShadowBorder(new Color(0, 0, 0), new Insets(0, 0, 4, 0), 0.12f),
                new RoundedLineBorder(BORDER, 12)));
        page.add(tableScroll, BorderLayout.CENTER);
        if (index == 5) {
            checkoutInvoice.setEditable(false);
            checkoutInvoice.setLineWrap(true);
            checkoutInvoice.setWrapStyleWord(true);
            checkoutInvoice.setRows(5);
            checkoutInvoice.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            checkoutInvoice.setBackground(Color.WHITE);
            checkoutInvoice.setForeground(TEXT);
            checkoutInvoice.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(BORDER), "Rincian Tagihan Terakhir"));
            JScrollPane invoiceScroll = new JScrollPane(checkoutInvoice);
            invoiceScroll.setBorder(BorderFactory.createLineBorder(BORDER));
            page.add(invoiceScroll, BorderLayout.SOUTH);
        }
        return page;
    }

    private Color statusColor(Object value) {
        if ("Check-In".equals(value)) {
            return STATUS_CHECKIN;
        }
        if ("Sedang Dirawat".equals(value)) {
            return STATUS_CARE;
        }
        if ("Siap Check-Out".equals(value)) {
            return PINK;
        }
        if ("Selesai".equals(value)) {
            return STATUS_DONE;
        }
        return Color.WHITE;
    }

    private JPanel createDashboardSummary() {
        JPanel summary = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        summary.setBackground(CREAM);
        summary.add(statCard("Semua Pemesanan", totalCount, SAGE));
        summary.add(statCard("Perawatan Hewan", careCount, PINK));
        summary.add(statCard("Penitipan Hewan", hotelCount, PINK));
        summary.add(statCard("Pelunasan Selesai", checkoutCount, PINK));
        return summary;
    }

    private JPanel statCard(String title, JLabel value, Color background) {
        RoundedPanel card = new RoundedPanel(new BorderLayout(8, 4), 16, background, true);
        card.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        card.setPreferredSize(new Dimension(138, 86));
        JLabel titleLabel = new JLabel("<html>" + title + "</html>");
        titleLabel.setForeground(TEXT);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN, 12f));
        value.setFont(value.getFont().deriveFont(Font.BOLD, 20f));
        value.setForeground(TEXT);
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    private static class RoundedPanel extends JPanel {
        private final int arc;
        private final boolean shadow;

        RoundedPanel(LayoutManager layout, int arc, Color background, boolean shadow) {
            super(layout);
            this.arc = arc;
            this.shadow = shadow;
            setBackground(background);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int inset = shadow ? 4 : 0;
            if (shadow) {
                g2.setColor(new Color(43, 47, 43, 18));
                g2.fill(new RoundRectangle2D.Float(5, 6, getWidth() - 10, getHeight() - 10,
                        arc, arc));
            }
            RoundRectangle2D shape = new RoundRectangle2D.Float(
                    inset, inset, getWidth() - inset * 2 - 1, getHeight() - inset * 2 - 1,
                    arc, arc);
            g2.setColor(getBackground());
            g2.fill(shape);
            g2.setColor(BORDER);
            g2.draw(shape);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    private static class RoundedLineBorder extends AbstractBorder {
        private final Color color;
        private final int arc;

        RoundedLineBorder(Color color, int arc) {
            this.color = color;
            this.arc = arc;
        }

        @Override
        public void paintBorder(Component component, Graphics graphics,
                int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, arc, arc);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component component) {
            return new Insets(2, 2, 2, 2);
        }
    }

    private static class HousePawIcon implements Icon {
        @Override
        public int getIconWidth() {
            return 50;
        }

        @Override
        public int getIconHeight() {
            return 50;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.translate(x, y);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 1. Outline rumah segi lima tegak dengan sudut membulat
            g2.setColor(new Color(0x829C7E));
            g2.setStroke(new BasicStroke(4.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D house = new Path2D.Float();
            house.moveTo(25, 6);   // Puncak atap
            house.lineTo(43, 21);  // Sudut kanan atap
            house.lineTo(43, 42);  // Sudut kanan bawah (tegak lurus)
            house.lineTo(7, 42);   // Sudut kiri bawah (tegak lurus)
            house.lineTo(7, 21);   // Sudut kiri atap
            house.closePath();
            g2.draw(house);

            // 2. Empat jari oval putih krem (simetris kiri-kanan)
            g2.setColor(CREAM);
            g2.fillOval(13, 22, 5, 7); // Jari paling kiri
            g2.fillOval(19, 18, 5, 7); // Jari tengah kiri
            g2.fillOval(26, 18, 5, 7); // Jari tengah kanan
            g2.fillOval(32, 22, 5, 7); // Jari paling kanan

            // 3. Bantalan telapak utama merah muda pastel (3 lobus menyatu rapi)
            g2.setColor(PINK);
            Path2D pad = new Path2D.Float();
            pad.moveTo(25, 29);
            pad.curveTo(22, 26, 18, 29, 17, 33);
            pad.curveTo(16, 37, 20, 38, 23, 37);
            pad.quadTo(25, 39, 27, 37);
            pad.curveTo(30, 38, 34, 37, 33, 33);
            pad.curveTo(32, 29, 28, 26, 25, 29);
            pad.closePath();
            g2.fill(pad);

            g2.dispose();
        }
    }

    private String pageDescription(int index) {
        return switch (index) {
            case 1 -> "Daftar pemilik beserta nomor kontak dan pemesanan terkait.";
            case 2 -> "Daftar hewan dan pemilik yang terdaftar pada pemesanan.";
            case 3 -> "Layanan perawatan dan kebersihan hewan.";
            case 4 -> "Layanan penitipan dan penginapan hewan.";
            case 5 -> "Daftar tagihan dan pemesanan yang siap check out.";
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
                            && !"Siap Check-Out".equals(entry.getStringValue(8))) {
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
        return selectedPage == 3 || selectedPage == 4;
    }
    public void setCheckoutEnabled(boolean enabled) {
        checkoutButton.setEnabled(enabled);
        boardingCheckoutButton.setEnabled(enabled);
    }
    public void setUpdateDataEnabled(boolean enabled) { updateDataButton.setEnabled(enabled); }
    public JButton getCheckoutButton()               { return checkoutButton; }
    public List<JButton> getCheckoutButtons() {
        return List.of(checkoutButton, boardingCheckoutButton);
    }
    public JButton getSearchButton()                 { return searchButton; }
    public JButton getDeleteButton()                 { return deleteButton; }
    public JButton getResetButton()                  { return resetButton; }
    public JTable getSelectedBookingsTable()         { return tables.get(pages.getSelectedIndex()); }
    public List<JTable> getTables()                  { return new ArrayList<>(tables); }
    public JTabbedPane getPages()                    { return pages; }
    public BookingTableModel getTableModel()         { return tableModel; }
    public void showCheckoutInvoice(String invoice)  { checkoutInvoice.setText(invoice); }
}
