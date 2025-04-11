import java.sql.*;
import java.util.Scanner;

public class hotel_reservation_project{
    private static final String URL = "jdbc:mysql://localhost:3306/Hotel_db";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Use Your Password!!

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            

            try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
                 Scanner scanner = new Scanner(System.in)) {
                

                while (true) {
                    System.out.println("\n_________________ RESERVATION MANAGEMENT SYSTEM_________________");
                    System.out.println("1. Reserve A Room");
                    System.out.println("2. View Reservation");
                    System.out.println("3. Get Room Number");
                    System.out.println("4. Update Reservation");
                    System.out.println("5. Delete Reservation");
                    System.out.println("0. Exit");
                    System.out.print("Choose an option: ");

                    int choice = scanner.nextInt();
                    scanner.nextLine(); 

                    switch (choice) {
                        case 1 : reserveRoom(con, scanner);
                        		break;
                        case 2 : viewReservations(con);
                        		break;
                        case 3 : getRoomNumber(con, scanner);
                        		break;
                        case 4 : updateReservation(con, scanner);
                        		break;
                        case 5 : deleteReservation(con, scanner);
                        		break;
                        case 0 : {
                            exit();
                            return;
                        }
                        default : System.out.println("Invalid Choice. Try Again");
                        System.out.println();
                        
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void reserveRoom(Connection con, Scanner scanner) {
        try {
            System.out.print("Enter your name: ");
            String guestName = scanner.nextLine();

            System.out.print("Enter room number: ");
            int roomNumber = scanner.nextInt();
            scanner.nextLine(); 

            System.out.print("Enter your contact details: ");
            String contact = scanner.nextLine();
            
            System.out.println("Enter number of days you want to stay: ");
            int days=scanner.nextInt(); //800 rupees per day
            int total_amount = (800*days)+50;

            String sql = "INSERT INTO reservations (guest_name, room_number, contact, Total_Amount) VALUES (?, ?, ?,?)";
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setString(1, guestName);
                pstmt.setInt(2, roomNumber);
                pstmt.setString(3, contact);
                pstmt.setInt(4, total_amount);

                int affectedRows = pstmt.executeUpdate();
                if(affectedRows > 0)
                {
                	System.out.println("___________________Reservation Successful____________________");
                	System.out.println();
                }
                else
                {
                	System.out.println("____________________Reservation Failed_____________________");
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void viewReservations(Connection con) {
        String sql = "SELECT reservation_id, guest_name, room_number, contact, reservation_date, Total_Amount FROM reservations";
        try (Statement stmt = con.createStatement();
             ResultSet resultSet = stmt.executeQuery(sql)) {

            System.out.println("______________Current Reservations________________");
            while (resultSet.next()) {
                System.out.println("Reservation ID: " + resultSet.getInt("reservation_id") +
                        ", Guest Name: " + resultSet.getString("guest_name") +
                        ", Room Number: " + resultSet.getInt("room_number") +
                        ", Contact: " + resultSet.getString("contact") +
                        ", Reservation Date: " + resultSet.getTimestamp("reservation_date")+
                        ", Total_Amount: " + resultSet.getInt("total_amount") );
            }
        } catch (Exception e) {
            System.out.println("Error fetching reservations: " + e.getMessage());
        }
    }

    private static void getRoomNumber(Connection con, Scanner scanner) {
        try {
            System.out.print("Enter reservation ID: ");
            int reservationId = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            System.out.print("Enter guest name: ");
            String guestName = scanner.nextLine();

            String sql = "SELECT room_number FROM reservations WHERE reservation_id = ? AND guest_name = ?";
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setInt(1, reservationId);
                pstmt.setString(2, guestName);
                ResultSet resultSet = pstmt.executeQuery();

                if (resultSet.next()) {
                    System.out.println("Room Number: " + resultSet.getInt("room_number"));
                } else {
                    System.out.println("No reservation found for the given ID and guest name.");
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void updateReservation(Connection con, Scanner scanner) {
        try {
            System.out.print("Enter reservation ID to update: ");
            int reservationId = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            if (!reservationExists(con, reservationId)) {
                System.out.println("Reservation not found.");
                return;
            }

            System.out.print("Enter new guest name: ");
            String newGuestName = scanner.nextLine();

            System.out.print("Enter new room number: ");
            int newRoomNumber = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            System.out.print("Enter new contact number: ");
            String newContactNumber = scanner.nextLine();

            String sql = "UPDATE reservations SET guest_name = ?, room_number = ?, contact = ? WHERE reservation_id = ?";
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setString(1, newGuestName);
                pstmt.setInt(2, newRoomNumber);
                pstmt.setString(3, newContactNumber);
                pstmt.setInt(4, reservationId);

                int affectedRows = pstmt.executeUpdate();
                System.out.println(affectedRows > 0 ? "Reservation Updated Successfully!" : "Reservation Update Failed!");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void deleteReservation(Connection con, Scanner scanner) {
        try {
            System.out.print("Enter reservation ID to delete: ");
            int reservationId = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            if (!reservationExists(con, reservationId)) {
                System.out.println("Reservation not found.");
                return;
            }

            String sql = "DELETE FROM reservations WHERE reservation_id = ?";
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setInt(1, reservationId);

                int affectedRows = pstmt.executeUpdate();
                System.out.println(affectedRows > 0 ? "Reservation Deleted Successfully!" : "Reservation Deletion Failed!");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static boolean reservationExists(Connection con, int reservationId) {
        String sql = "SELECT reservation_id FROM reservations WHERE reservation_id = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, reservationId);
            ResultSet resultSet = pstmt.executeQuery();
            return resultSet.next();
        } catch (Exception e) {
            System.out.println("Error checking reservation: " + e.getMessage());
            return false;
        }
    }

    public static void exit() throws InterruptedException {
        System.out.print("Exiting System");
        for (int i = 5; i > 0; i--) {
            System.out.print(".");
            Thread.sleep(450);
        }
        System.out.println("\nThank You For Using Hotel Reservation System!!!");
    }
}

