/* TASK FOUR: LIBRARY MANAGEMENT SYSTEM
Develop a Java program to manage library resources such as books, magazines, and DVDs. The system should support functionalities like adding new items to the library, checking out and returning items,
managing overdue fines, and searching for items by title, author, or category. Implement features for multiple user roles (e.g., librarian, patron), data persistence using databases or file handling, and a user
friendly interface for easy navigation.*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LibraryManagementSystem {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/library_db";
    private static final String USER = "root";
    private static final String PASSWORD = "Uday@2609";

    private JFrame frame;
    private JPanel loginPanel, librarianPanel, patronPanel;
    private Connection connection;

    public LibraryManagementSystem() {
        try {
            connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            System.out.println("Connected to the database successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        initializeGUI();
    }

    private void initializeGUI() {
        frame = new JFrame("Library Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(new CardLayout());

        createLoginPanel();
        createLibrarianPanel();
        createPatronPanel();

        frame.add(loginPanel, "Login");
        frame.add(librarianPanel, "Librarian");
        frame.add(patronPanel, "Patron");

        frame.setVisible(true);
    }

    private void createLoginPanel() {
        loginPanel = new JPanel();
        loginPanel.setLayout(new GridLayout(4, 2, 10, 10));

        JLabel userLabel = new JLabel("Username:");
        JTextField userField = new JTextField();
        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField();
        JButton loginButton = new JButton("Login");

        loginButton.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword()).trim();

            if ("librarian".equals(username) && "libpass".equals(password)) {
                switchToPanel("Librarian");
            } else if ("patron".equals(username) && "patpass".equals(password)) {
                switchToPanel("Patron");
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid credentials!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        loginPanel.add(userLabel);
        loginPanel.add(userField);
        loginPanel.add(passLabel);
        loginPanel.add(passField);
        loginPanel.add(new JLabel());
        loginPanel.add(loginButton);
    }

    private void createLibrarianPanel() {
        librarianPanel = new JPanel(new GridLayout(6, 1, 10, 10));

        JButton addItemButton = new JButton("Add Item");
        JButton searchItemButton = new JButton("Search Item");
        JButton overdueButton = new JButton("Check Overdue Items");
        JButton deleteItemButton = new JButton("Delete Item");
        JButton availableBooksButton = new JButton("View Available Books");
        JButton exitButton = new JButton("Logout");

        addItemButton.addActionListener(e -> openAddItemDialog());
        searchItemButton.addActionListener(e -> openSearchItemDialog());
        overdueButton.addActionListener(e -> openOverdueItemsDialog());
        deleteItemButton.addActionListener(e -> openDeleteItemDialog());
        availableBooksButton.addActionListener(e -> showAvailableBooksDialog());
        exitButton.addActionListener(e -> switchToPanel("Login"));

        librarianPanel.add(addItemButton);
        librarianPanel.add(searchItemButton);
        librarianPanel.add(overdueButton);
        librarianPanel.add(deleteItemButton);
        librarianPanel.add(availableBooksButton);
        librarianPanel.add(exitButton);
    }

    private void createPatronPanel() {
        patronPanel = new JPanel(new GridLayout(5, 1, 10, 10));

        JButton checkoutItemButton = new JButton("Check Out Item");
        JButton returnItemButton = new JButton("Return Item");
        JButton searchItemButton = new JButton("Search Item");
        JButton checkFinesButton = new JButton("Check Unpaid Fines");
        JButton exitButton = new JButton("Logout");

        checkoutItemButton.addActionListener(e -> openCheckoutItemDialog());
        returnItemButton.addActionListener(e -> openReturnItemDialog());
        searchItemButton.addActionListener(e -> openSearchItemDialog());
        checkFinesButton.addActionListener(e -> checkUnpaidFines());
        exitButton.addActionListener(e -> switchToPanel("Login"));

        patronPanel.add(checkoutItemButton);
        patronPanel.add(returnItemButton);
        patronPanel.add(searchItemButton);
        patronPanel.add(checkFinesButton);
        patronPanel.add(exitButton);
    }

    private void openAddItemDialog() {
        JTextField titleField = new JTextField();
        JTextField authorField = new JTextField();
        JTextField categoryField = new JTextField();
        JTextField typeField = new JTextField();

        Object[] fields = {
            "Title:", titleField,
            "Author:", authorField,
            "Category:", categoryField,
            "Type (Book/Magazine/DVD):", typeField
        };

        int result = JOptionPane.showConfirmDialog(frame, fields, "Add Item", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String sql = "INSERT INTO items (title, author, category, type) VALUES (?, ?, ?, ?)";
                PreparedStatement pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, titleField.getText().trim());
                pstmt.setString(2, authorField.getText().trim());
                pstmt.setString(3, categoryField.getText().trim());
                pstmt.setString(4, typeField.getText().trim());
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(frame, "Item added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(frame, "Error adding item: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openSearchItemDialog() {
        String keyword = JOptionPane.showInputDialog(frame, "Enter keyword to search (title/author/category):", "Search Item", JOptionPane.QUESTION_MESSAGE);
        if (keyword != null) {
            try {
                String sql = "SELECT * FROM items WHERE title LIKE ? OR author LIKE ? OR category LIKE ?";
                PreparedStatement pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, "%" + keyword + "%");
                pstmt.setString(2, "%" + keyword + "%");
                pstmt.setString(3, "%" + keyword + "%");
                ResultSet rs = pstmt.executeQuery();

                StringBuilder results = new StringBuilder("Search Results:\n\n");
                while (rs.next()) {
                    results.append("ID: ").append(rs.getInt("id"))
                           .append(", Title: ").append(rs.getString("title"))
                           .append(", Author: ").append(rs.getString("author"))
                           .append(", Category: ").append(rs.getString("category"))
                           .append(", Type: ").append(rs.getString("type"))
                           .append(", Available: ").append(rs.getBoolean("isAvailable"))
                           .append("\n");
                }

                JOptionPane.showMessageDialog(frame, results.toString(), "Search Results", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(frame, "Error searching items: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openOverdueItemsDialog() {
        try {
            String sql = "SELECT i.id, i.title, t.borrower_name, " +
                         "DATEDIFF(CURDATE(), t.checkout_date) AS overdue_days " +
                         "FROM items i " +
                         "INNER JOIN transactions t ON i.id = t.item_id " +
                         "WHERE t.return_date IS NULL AND DATEDIFF(CURDATE(), t.checkout_date) > 0";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            StringBuilder results = new StringBuilder("Overdue or Pending Items:\n\n");
            while (rs.next()) {
                results.append("ID: ").append(rs.getInt("id"))
                       .append(", Title: ").append(rs.getString("title"))
                       .append(", Borrower: ").append(rs.getString("borrower_name"))
                       .append(", Overdue Days: ").append(rs.getInt("overdue_days"))
                       .append("\n");
            }

            JOptionPane.showMessageDialog(frame, results.toString(), "Overdue Items", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error retrieving overdue items: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAvailableBooksDialog() {
        try {
            String sql = "SELECT id, title FROM items WHERE isAvailable = TRUE";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            StringBuilder availableBooks = new StringBuilder("Available Books:\n\n");
            while (rs.next()) {
                availableBooks.append("ID: ").append(rs.getInt("id"))
                              .append(", Title: ").append(rs.getString("title"))
                              .append("\n");
            }

            JOptionPane.showMessageDialog(frame, availableBooks.toString(), "Available Books", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error retrieving available books: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openDeleteItemDialog() {
        String itemId = JOptionPane.showInputDialog(frame, "Enter Item ID to Delete:", "Delete Item", JOptionPane.QUESTION_MESSAGE);
        if (itemId != null) {
            try {
                String sql = "DELETE FROM items WHERE id = ?";
                PreparedStatement pstmt = connection.prepareStatement(sql);
                pstmt.setInt(1, Integer.parseInt(itemId));
                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(frame, "Item deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(frame, "No item found with the given ID.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(frame, "Error deleting item: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openCheckoutItemDialog() {
        String itemId = JOptionPane.showInputDialog(frame, "Enter Item ID to Check Out:", "Check Out Item", JOptionPane.QUESTION_MESSAGE);
        String borrowerName = JOptionPane.showInputDialog(frame, "Enter Borrower Name:", "Check Out Item", JOptionPane.QUESTION_MESSAGE);

        if (itemId != null && borrowerName != null) {
            try {
                String checkSql = "SELECT isAvailable FROM items WHERE id = ?";
                PreparedStatement checkStmt = connection.prepareStatement(checkSql);
                checkStmt.setInt(1, Integer.parseInt(itemId));
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next() && rs.getBoolean("isAvailable")) {
                    String updateSql = "UPDATE items SET isAvailable = FALSE WHERE id = ?";
                    PreparedStatement updateStmt = connection.prepareStatement(updateSql);
                    updateStmt.setInt(1, Integer.parseInt(itemId));
                    updateStmt.executeUpdate();

                    String insertSql = "INSERT INTO transactions (item_id, borrower_name, checkout_date) VALUES (?, ?, CURDATE())";
                    PreparedStatement insertStmt = connection.prepareStatement(insertSql);
                    insertStmt.setInt(1, Integer.parseInt(itemId));
                    insertStmt.setString(2, borrowerName);
                    insertStmt.executeUpdate();

                    JOptionPane.showMessageDialog(frame, "Item checked out successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(frame, "Item is not available for checkout!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(frame, "Error during checkout: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openReturnItemDialog() {
        String itemId = JOptionPane.showInputDialog(frame, "Enter Item ID to Return:", "Return Item", JOptionPane.QUESTION_MESSAGE);

        if (itemId != null) {
            try {
                String updateSql = "UPDATE items SET isAvailable = TRUE WHERE id = ?";
                PreparedStatement updateStmt = connection.prepareStatement(updateSql);
                updateStmt.setInt(1, Integer.parseInt(itemId));
                updateStmt.executeUpdate();

                String insertSql = "UPDATE transactions SET return_date = CURDATE() WHERE item_id = ? AND return_date IS NULL";
                PreparedStatement insertStmt = connection.prepareStatement(insertSql);
                insertStmt.setInt(1, Integer.parseInt(itemId));
                insertStmt.executeUpdate();

                JOptionPane.showMessageDialog(frame, "Item returned successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(frame, "Error during return: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void checkUnpaidFines() {
        try {
            String sql = "SELECT borrower_name, SUM(fine) AS total_fine FROM transactions WHERE fine > 0 GROUP BY borrower_name";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            StringBuilder fineDetails = new StringBuilder("Unpaid Fines:\n\n");
            while (rs.next()) {
                fineDetails.append("Borrower: ").append(rs.getString("borrower_name"))
                           .append(", Total Fine: ").append(rs.getDouble("total_fine"))
                           .append("\n");
            }

            if (fineDetails.toString().equals("Unpaid Fines:\n\n")) {
                fineDetails.append("No unpaid fines found.");
            }

            JOptionPane.showMessageDialog(frame, fineDetails.toString(), "Unpaid Fines", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error retrieving unpaid fines: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void switchToPanel(String panelName) {
        CardLayout cl = (CardLayout) frame.getContentPane().getLayout();
        cl.show(frame.getContentPane(), panelName);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LibraryManagementSystem::new);
    }
}
