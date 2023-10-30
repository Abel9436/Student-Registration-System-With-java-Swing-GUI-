import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

class LoginPage {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createAndShowUI());
    }

    static void createAndShowUI() {
        JFrame frame = new JFrame("Login Page");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 500);
        frame.setLayout(new BorderLayout());

        JPanel loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 10, 5, 10);

        // Username
        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField(20);

        // Password
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField(20);

        // Log in as
        JLabel userTypeLabel = new JLabel("Log in as:");
        String[] userTypes = {"Admin", "Student"};
        JComboBox<String> userTypeComboBox = new JComboBox<>(userTypes);

        // Log in button
        JButton loginButton = new JButton("Log In");
        loginButton.setBackground(Color.BLUE);
        loginButton.setForeground(Color.WHITE);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                char[] password = passwordField.getPassword();
                String userType = (String) userTypeComboBox.getSelectedItem();

                if (login(username, new String(password), userType)) {
                    JOptionPane.showMessageDialog(frame, "Login successful as " + userType);
                    frame.setVisible(false);
                    if (userType=="Student"){
                      SwingUtilities.invokeLater(() -> {
            StudentManagementApplication app = new StudentManagementApplication();
            app.setVisible(true);
        });
                    }
                    else{
                        SStudentDataEntryApp.createAndShowGUUI();
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Login failed. Please check your credentials.");
                }
            }
        });


        constraints.gridx = 0;
        constraints.gridy = 0;
        loginPanel.add(usernameLabel, constraints);
        constraints.gridx = 1;
        loginPanel.add(usernameField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        loginPanel.add(passwordLabel, constraints);
        constraints.gridx = 1;
        loginPanel.add(passwordField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        loginPanel.add(userTypeLabel, constraints);
        constraints.gridx = 1;
        loginPanel.add(userTypeComboBox, constraints);

        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 2;
        loginPanel.add(loginButton, constraints);

        frame.add(loginPanel, BorderLayout.CENTER);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static boolean login(String username, String password, String userType) {
        String dbUrl = "jdbc:mysql://localhost:3306/studentregistration";
        String dbUser = "root";
        String dbPassword = "Betelhem@sql";

        String query = "SELECT * FROM users WHERE username = ? AND password = ? AND userType = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, userType);

            ResultSet resultSet = pstmt.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
