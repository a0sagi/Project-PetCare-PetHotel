import controller.BookingController;
import database.BookingDAO;
import model.Pet;
import model.AnimalFactory;
import model.PetBooking;
import model.PetType;
import model.ServiceType;
import repository.BookingRepository;
import service.BookingService;
import view.PetCareMainFrame;

import javax.swing.SwingUtilities;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && "--demo".equalsIgnoreCase(args[0])) {
            runConsoleDemo();
            return;
        }
        boolean databaseMode = args.length > 0 && "--db".equalsIgnoreCase(args[0]);
        SwingUtilities.invokeLater(() -> launchGui(databaseMode));
    }

    private static void launchGui(boolean databaseMode) {
        try {
            BookingService service = new BookingService(
                    new BookingRepository(), databaseMode ? new BookingDAO() : null);
            PetCareMainFrame frame = new PetCareMainFrame();
            new BookingController(frame, service);
            frame.setVisible(true);
        } catch (SQLException exception) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Tidak dapat memuat database: " + exception.getMessage(),
                    "Koneksi Database Gagal", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void runConsoleDemo() {
        BookingRepository repository = new BookingRepository();
        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));
        for (PetType petType : PetType.values()) {
            Pet animal = AnimalFactory.create(petType, "Contoh " + petType.getLabel());
            PetBooking booking = new PetBooking(
                    petType.ordinal() + 1,
                    "Pemilik Contoh",
                    812345678,
                    animal,
                    ServiceType.BOARDING,
                    2);
            repository.addBooking(booking);
            System.out.printf("%s: tarif inap %s/hari, tagihan dua hari %s%n",
                    animal.getPetType().getLabel(), currency.format(animal.getDailyRate()),
                    currency.format(booking.getTotalBill()));
        }
        System.out.println("Pencarian booking 1: "
                + repository.findById(1).getPetName());
        try {
            repository.addBooking(repository.findById(1));
        } catch (IllegalArgumentException exception) {
            System.out.println("Validasi ID duplikat: " + exception.getMessage());
        }
    }
} 
