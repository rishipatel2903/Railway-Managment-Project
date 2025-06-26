import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

class CSVUtils {
    public static List<String[]> readCSV(String filePath) {
        List<String[]> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                records.add(values);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return records;
    }

    public static void writeCSV(List<String[]> data, String filePath) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            data.forEach(record -> pw.println(String.join(",", record)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void appendCSV(String[] data, String filePath) {
        try (FileWriter fw = new FileWriter(filePath, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter pw = new PrintWriter(bw)) {
            pw.println(String.join(",", data));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

abstract class User {
    protected String username;
    protected String password;

    abstract boolean authenticate(String username, String password);
    abstract void displayMenu();
}

class NormalUser extends User {
    private String name;
    private int age;
    private String gender;
    private String dateOfBirth;
    private String phoneNumber;

    public NormalUser(String name, int age, String gender, String dateOfBirth, String phoneNumber, String username, String password) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.phoneNumber = phoneNumber;
        this.username = username;
        this.password = password;
    }

    @Override
    boolean authenticate(String username, String password) {
        List<String[]> records = CSVUtils.readCSV("src//credentials_user.csv");
        for (String[] record : records) {
            if (record[5].equals(username) && record[6].equals(password)) {
                return true;
            }
        }
        return false;
    }

    @Override
    void displayMenu() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        while (running) {
            System.out.println("\n1) Book Ticket\n2) Cancel Ticket\n3) View My Booking Details\n4) Exit");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    bookTicket();
                    break;
                case 2:
                    cancelTicket();
                    break;
                case 3:
                    viewBookingDetails();
                    break;
                case 4:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    private static boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(emailRegex);
    }

    private void bookTicket() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Start Destination: ");
        String start = scanner.nextLine();
        System.out.print("Enter End Destination: ");
        String end = scanner.nextLine();
        List<String[]> trains = CSVUtils.readCSV("src//trains.csv");
        List<String[]> filteredTrains = trains.stream()
                .filter(t -> t[2].equalsIgnoreCase(start) && t[3].equalsIgnoreCase(end))
                .collect(Collectors.toList());

        if (filteredTrains.isEmpty()) {
            System.out.println("No trains are available for the selected route.");
            return;
        }

        System.out.println("Available Trains :");
        filteredTrains.forEach(t -> System.out.println("Train Number: " + t[0] + ", Train Name: " + t[1] +", Departure Time: " + t[18] + ", Estimated time of arrival; " + t[19]));

        System.out.print("Enter Train Number: ");
        String trainNumber = scanner.nextLine();

        Optional<String[]> selectedTrain = filteredTrains.stream()
                .filter(t -> t[0].equals(trainNumber))
                .findFirst();

        if (!selectedTrain.isPresent()) {
            System.out.println("Invalid Train Number. Please try again.");
            return;
        }

        String[] train = selectedTrain.get();
        String startTime = train[18];
        String estimatedArrivalTime = train[19];

        System.out.print("Enter your Name : ");
        String userName = scanner.nextLine();
        String phone;
        while (true) {
            System.out.print("Enter your phone number (10 digits) : ");
            phone = scanner.nextLine();
            if (phone.matches("\\d{10}")) break; // only 10 digit number (that is also from 0 to 9)
            System.out.println("Invalid phone number. Please enter a 10 digit phone number.");
        }
        String email;
        while (true) {
            System.out.print("Enter your email address: ");
            email = scanner.nextLine();
            if (isValidEmail(email)) break;
            System.out.println("Invalid email address. Please enter a valid email.");
        }

        System.out.print("Enter your Age: ");
        int age = scanner.nextInt();

        System.out.println("Food options:\n 1) No Food\n 2) Veg\n 3) Non-Veg");
        System.out.print("Choose your food option : ");
        int foodChoiceIndex = scanner.nextInt();
        String foodChoice = foodChoiceIndex == 1 ? "No Food" : foodChoiceIndex == 2 ? "Veg" : "Non-Veg";

        System.out.println("Select class:\n 1) 1A\n 2) 2A\n 3) 3A\n 4) CC");
        System.out.print("Choose your preferred class: ");
        int classChoice = scanner.nextInt();
        int classIndex = (classChoice - 1) * 3 + 6;

        System.out.print("Enter number of ticket you want to book : ");
        int numberOfTickets = scanner.nextInt();

        if (numberOfTickets > Integer.parseInt(train[classIndex + 1])) {
            System.out.println("Not enough seats available. Only " + train[classIndex + 1] + " seats left.");
            return;
        }

        double price = Double.parseDouble(train[classIndex + 2]);
        double totalCost = price * numberOfTickets;

        train[classIndex + 1] = Integer.toString(Integer.parseInt(train[classIndex + 1]) - numberOfTickets);
        train[5] = Integer.toString(Integer.parseInt(train[5]) - numberOfTickets);
        CSVUtils.writeCSV(trains, "src//trains.csv");
        CSVUtils.appendCSV(new String[]{trainNumber, userName, phone, email, Integer.toString(age), foodChoice, Integer.toString(numberOfTickets), getClassType(classChoice), String.format("%.2f", totalCost), startTime, estimatedArrivalTime}, "src//bookings.csv");
        System.out.println("Ticket booked successfully. Total cost of the ticket(s) : " + totalCost);
    }

    private String getClassType(int classChoice) {
        switch (classChoice) {
            case 1: return "1A";
            case 2: return "2A";
            case 3: return "3A";
            case 4: return "CC";
            default: return "Unknown";
        }
    }

    private void cancelTicket() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your phone number: ");
        String phone = scanner.nextLine();
        System.out.print("Enter your Email: ");
        String email = scanner.nextLine();
        System.out.print("Enter Train Number: ");
        String trainNumber = scanner.nextLine();

        List<String[]> bookings = CSVUtils.readCSV("src//bookings.csv");
        Optional<String[]> bookingToCancel = bookings.stream()
                .filter(b -> b[2].equals(phone) && b[3].equals(email) && b[0].equals(trainNumber))
                .findFirst();

        if (!bookingToCancel.isPresent()) {
            System.out.println("Booking does not exist.");
            return;
        }

        String[] booking = bookingToCancel.get();
        System.out.print("Enter number of tickets to cancel (you have " + booking[6] + "): ");
        int ticketsToCancel = scanner.nextInt();
        if (ticketsToCancel > Integer.parseInt(booking[6])) {
            System.out.println("Cannot cancel more tickets than booked.");
            return;
        }

        booking[6] = Integer.toString(Integer.parseInt(booking[6]) - ticketsToCancel);

        List<String[]> trains = CSVUtils.readCSV("src//trains.csv");
        for (String[] train : trains) {
            if (train[0].equals(trainNumber)) {
                int classIndex = getClassIndex(booking[7]) * 3 + 6;
                train[classIndex + 1] = Integer.toString(Integer.parseInt(train[classIndex + 1]) + ticketsToCancel);
                train[5] = Integer.toString(Integer.parseInt(train[5]) + ticketsToCancel);
                break;
            }
        }

        CSVUtils.writeCSV(trains, "src//trains.csv");
        CSVUtils.writeCSV(bookings, "src//bookings.csv");

        double refundAmount = 0.6 * ((ticketsToCancel * Double.parseDouble(booking[8])/Double.parseDouble(booking[6])));
        System.out.println("Cancellation confirmed. Refund amount: " + refundAmount);
    }

    private int getClassIndex(String classType) {
        switch (classType) {
            case "1A": return 0;
            case "2A": return 1;
            case "3A": return 2;
            case "CC": return 3;
            default: return -1;
        }
    }

    private void viewBookingDetails() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your phone number: ");
        String phoneNumber = scanner.nextLine();
        System.out.print("Enter your Gmail: ");
        String gmail = scanner.nextLine();
        System.out.print("Enter the train number: ");
        String trainNumber = scanner.nextLine();

        List<String[]> bookings = CSVUtils.readCSV("src//bookings.csv");
        Optional<String[]> matchingBooking = bookings.stream()
                .filter(b -> b[2].equals(phoneNumber) && b[3].equals(gmail) && b[0].equals(trainNumber))
                .findFirst();

        if (matchingBooking.isPresent()) {
            String[] booking = matchingBooking.get();
            System.out.println("Booking Details Found :");
            System.out.println("Train Number : " + booking[0]);
            System.out.println("Name : " + booking[1]);
            System.out.println("Phone : " + booking[2]);
            System.out.println("Gmail : " + booking[3]);
            System.out.println("Age : " + booking[4]);
            System.out.println("Food Choice : " + booking[5]);
            System.out.println("Number of Tickets : " + booking[6]);
            System.out.println("Class : " + booking[7]);
            System.out.println("Total Cost : " + booking[8]);
            System.out.println("Departure Time : " + booking[9]);
            System.out.println("Estimated time of arrival : " + booking[10]);

        } else {
            System.out.println("No booking found matching the provided details.");
        }
    }

    public void signUp() {
        CSVUtils.appendCSV(new String[]{name, Integer.toString(age), gender, dateOfBirth, phoneNumber, username, password}, "src//credentials_user.csv");
    }

    public static NormalUser createUserFromInput() {//this is the method which returns normaluser
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Name : ");
        String name = scanner.nextLine();
        System.out.print("Enter Age : ");
        int age = scanner.nextInt(); scanner.nextLine();
        System.out.print("Enter Gender : ");
        String gender = scanner.nextLine();
        System.out.print("Enter Date of Birth (dd-mm-yyyy) : ");
        String dob = scanner.nextLine();
        String phone;
        while (true) {
            System.out.print("Enter phone number (10 digits) : ");
            phone = scanner.nextLine();
            if (phone.matches("\\d{10}")) break;
            System.out.println("Invalid phone number. Please enter a 10-digit number.");
        }
        System.out.print("Enter username : ");
        String username = scanner.nextLine();
        System.out.print("Enter password : ");
        String password = scanner.nextLine();
        NormalUser newUser = new NormalUser(name, age, gender, dob, phone, username, password);
        return newUser;
    }
}

class Admin extends User {
    @Override
    boolean authenticate(String username, String password) {
        List<String[]> records = CSVUtils.readCSV("src//credentials_admin.csv");
        for (String[] record : records) {
            if (record[0].equals(username) && record[1].equals(password)) {
                return true;
            }
        }
        return false;
    }

    @Override
    void displayMenu() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        while (running) {
            System.out.println("\n1) View Trains\n2) View Bookings\n3) Add Train\n4) Delete Train\n5) Exit");
            System.out.print("Choose an option : ");
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    viewTrains();
                    break;
                case 2:
                    viewBookings();
                    break;
                case 3:
                    addTrain();
                    break;
                case 4:
                    deleteTrain();
                    break;
                case 5:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void viewTrains() {
        List<String[]> trains = CSVUtils.readCSV("src//trains.csv");
        trains.forEach(t -> System.out.println(Arrays.toString(t)));
    }

    private void viewBookings() {
        List<String[]> bookings = CSVUtils.readCSV("src//bookings.csv");
        bookings.forEach(b -> System.out.println(Arrays.toString(b)));
    }

    private void addTrain() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Train Number : ");
        String trainNumber = scanner.nextLine();
        System.out.print("Enter Train Name : ");
        String trainName = scanner.nextLine();
        System.out.print("Enter Start Destination : ");
        String startDestination = scanner.nextLine();
        System.out.print("Enter End Destination : ");
        String endDestination = scanner.nextLine();
        System.out.print("Enter Total Seats : ");
        int totalSeats = scanner.nextInt();
        System.out.print("Enter remaining total seats : ");
        int remainingTotalSeats = scanner.nextInt();
        System.out.print("Enter total seats in 1A : ");
        int totalSeats1A = scanner.nextInt();
        System.out.print("Enter remaining seats in 1A : ");
        int remainingSeats1A = scanner.nextInt();
        System.out.print("Enter price of 1A : ");
        double price1A = scanner.nextDouble();
        System.out.print("Enter total seats in 2A : ");
        int totalSeats2A = scanner.nextInt();
        System.out.print("Enter remaining seats in 2A : ");
        int remainingSeats2A = scanner.nextInt();
        System.out.print("Enter price of 2A : ");
        double price2A = scanner.nextDouble();
        System.out.print("Enter total seats in 3A: ");
        int totalSeats3A = scanner.nextInt();
        System.out.print("Enter remaining seats in 3A: ");
        int remainingSeats3A = scanner.nextInt();
        System.out.print("Enter price of 3A: ");
        double price3A = scanner.nextDouble();
        System.out.print("Enter total seats in CC: ");
        int totalSeatsCC = scanner.nextInt();
        System.out.print("Enter remaining seats in CC: ");
        int remainingSeatsCC = scanner.nextInt();
        System.out.print("Enter price of CC: ");
        double priceCC = scanner.nextDouble();

        CSVUtils.appendCSV(new String[]{
                trainNumber, trainName, startDestination, endDestination,
                Integer.toString(totalSeats), Integer.toString(remainingTotalSeats),
                Integer.toString(totalSeats1A), Integer.toString(remainingSeats1A), Double.toString(price1A),
                Integer.toString(totalSeats2A), Integer.toString(remainingSeats2A), Double.toString(price2A),
                Integer.toString(totalSeats3A), Integer.toString(remainingSeats3A), Double.toString(price3A),
                Integer.toString(totalSeatsCC), Integer.toString(remainingSeatsCC), Double.toString(priceCC)
        }, "src//trains.csv");

        System.out.println("Train added successfully.");
    }

    private void deleteTrain() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Train number to delete : ");
        String trainNumber = scanner.nextLine();

        List<String[]> trains = CSVUtils.readCSV("src//trains.csv");
        List<String[]> updatedTrains = trains.stream()
                .filter(t -> !t[0].equals(trainNumber))
                .collect(Collectors.toList());

        if (updatedTrains.size() == trains.size()) {
            System.out.println("No train found with that number.");
            return;
        }

        CSVUtils.writeCSV(updatedTrains, "src//trains.csv");
        System.out.println("Train deleted successfully.");
    }
}

public class RailwayManagementSystem {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to the Rail Pal!");
        while (true) {
            System.out.println("\n\t\t\t================================");
            System.out.println("\t\t\t\t\t=   RAIL PAL   =");
            System.out.println("\t\t\t================================\n\n");
            System.out.println("\n1) User Login/Sign-Up\n2) Admin Login\n3) Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();
            switch (choice) {
                case 1:
                    userProcess(scanner);
                    break;
                case 2:
                    adminLogin(scanner);
                    break;
                case 3:
                    System.out.println("Exiting system...");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void userProcess(Scanner scanner) {
        System.out.println("\n1) Login\n2) Sign-Up\n3) Exit");
        System.out.print("Enter your choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        if (choice == 1) {
            System.out.print("Enter username : ");
            String username = scanner.nextLine();
            System.out.print("Enter password : ");
            String password = scanner.nextLine();
            NormalUser user = new NormalUser("", 0, "", "", "", username, password);
            if (user.authenticate(username, password)) {
                user.displayMenu();
            } else {
                System.out.println("User does not exist. Redirecting to signup...");
                userProcess(scanner);
            }
        } else if (choice == 2) {
            NormalUser newUser = NormalUser.createUserFromInput();
            newUser.signUp();
            System.out.println("Sign-Up successful. Please log in.");
        }
        else if (choice == 3){
            System.exit(0);
        }
        else {
            System.out.println("Invalid choice. Returning to main menu.");
        }
    }

    private static void adminLogin(Scanner scanner) {
        System.out.print("Enter admin username : ");
        String username = scanner.nextLine();
        System.out.print("Enter admin password : ");
        String password = scanner.nextLine();
        Admin admin = new Admin();
        if (admin.authenticate(username, password)) {
            admin.displayMenu();
        } else {
            System.out.println("Admin does not exist.");
        }
    }
}
