package view;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PetCareMainFrame extends JFrame {
    private final JTextField bookingIdField = new JTextField(16);
    private final JTextField ownerNameField = new JTextField(16);
    private final JTextField ownerPhoneField = new JTextField(16);
    private final JTextField petNameField = new JTextField(16);
    private final JComboBox<String> petTypeBox =
            new JComboBox<>(new String[]{"Kucing", "Anjing", "Kelinci"});
    private final JComboBox<String> serviceBox =
            new JComboBox<>(new String[]{"Grooming", "Boarding", "Full Service"});
    private final JSpinner durationSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 365, 1));
    private final JButton checkInButton = new JButton("Check-In Baru");
    private final JButton updateStatusButton = new JButton("Update Status");
    private final JButton checkoutButton = new JButton("Checkout & Invoice");
    private final JButton resetButton = new JButton("Reset Form");
    private final BookingTableModel tableModel = new BookingTableModel();
    private final JTable bookingsTable = new JTable(tableModel);

    public PetCareMainFrame() {
        super("Pet Care & Pet Hotel");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(12, 12));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(createHeader(), BorderLayout.NORTH);
        add(createFormPanel(), BorderLayout.WEST);
        add(createTablePanel(), BorderLayout.CENTER);
        setSize(1100, 620);
        setLocationRelativeTo(null);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Pet Care & Pet Hotel Management");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        JLabel date = new JLabel(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy HH:mm")));
        header.add(title, BorderLayout.WEST);
        header.add(date, BorderLayout.EAST);
        return header;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Pendaftaran & Aksi"));

        JPanel form = new JPanel(new GridBagLayout());
        String[] labels = {"ID Booking", "Nama Pemilik", "No. HP", "Nama Hewan",
                "Jenis Hewan", "Layanan", "Durasi (hari)"};
        java.awt.Component[] fields = {bookingIdField, ownerNameField, ownerPhoneField,
                petNameField, petTypeBox, serviceBox, durationSpinner};
        for (int row = 0; row < labels.length; row++) {
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.insets = new Insets(5, 5, 5, 5);
            constraints.anchor = GridBagConstraints.LINE_START;
            constraints.gridx = 0;
            constraints.gridy = row;
            form.add(new JLabel(labels[row]), constraints);
            constraints.gridx = 1;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.weightx = 1;
            form.add(fields[row], constraints);
        }
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        buttons.add(checkInButton);
        buttons.add(updateStatusButton);
        buttons.add(checkoutButton);
        buttons.add(resetButton);
        panel.add(form, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        panel.setPreferredSize(new java.awt.Dimension(390, 400));
        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Tracking Booking"));
        bookingsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        bookingsTable.setAutoCreateRowSorter(true);
        panel.add(new JScrollPane(bookingsTable), BorderLayout.CENTER);
        return panel;
    }

    public JTextField getBookingIdField() { return bookingIdField; }
    public JTextField getOwnerNameField() { return ownerNameField; }
    public JTextField getOwnerPhoneField() { return ownerPhoneField; }
    public JTextField getPetNameField() { return petNameField; }
    public JComboBox<String> getPetTypeBox() { return petTypeBox; }
    public JComboBox<String> getServiceBox() { return serviceBox; }
    public JSpinner getDurationSpinner() { return durationSpinner; }
    public JButton getCheckInButton() { return checkInButton; }
    public JButton getUpdateStatusButton() { return updateStatusButton; }
    public JButton getCheckoutButton() { return checkoutButton; }
    public JButton getResetButton() { return resetButton; }
    public JTable getBookingsTable() { return bookingsTable; }
    public BookingTableModel getTableModel() { return tableModel; }
}
