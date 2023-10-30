import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

class StudentManagementApplication extends JFrame {
    private JTabbedPane tabbedPane;

    public  StudentManagementApplication() {
        setTitle("Student Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Student Registration", new StudentRegistrationPanel());
        tabbedPane.addTab("Student Results", new StudentResultsPanel());
        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(Color.BLUE);
        logoutButton.setForeground(Color.WHITE);

        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                int choice = JOptionPane.showConfirmDialog(StudentManagementApplication.this,"Are you sure you want to log out?", "Logout", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    // Handle the logout action here, e.g., by closing the current window or returning to a login page.
                   dispose();
                    LoginPage.createAndShowUI();// Close the current window
                }
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(logoutButton);
        add(buttonPanel, BorderLayout.SOUTH);

        add(tabbedPane);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StudentManagementApplication app = new StudentManagementApplication();
            app.setVisible(true);
        });
    }
}

class StudentRegistrationPanel2 extends JPanel {
    private JComboBox<String> departmentComboBox;
    private JComboBox<String> semesterComboBox;
    private JPanel coursesPanel;

    public StudentRegistrationPanel2() {
        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(Color.BLUE);
        logoutButton.setForeground(Color.WHITE);
        setLayout(new BorderLayout());
        JPanel registrationPanel = new JPanel();
        registrationPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 10, 5, 10);
        constraints.anchor = GridBagConstraints.CENTER;

        // Full Name
        JLabel nameLabel = new JLabel("Full Name");
        JTextField nameField = new JTextField(20);

        // Student ID
        JLabel idLabel = new JLabel("Student ID");
        JTextField idField = new JTextField(20);

        // Department (ComboBox)
        JLabel departmentLabel = new JLabel("Department");
        String[] departmentOptions = {"CSE", "SE", "ECE", "EPCE"};
        departmentComboBox = new JComboBox<>(departmentOptions);
        // Semester (ComboBox)
        JLabel semesterLabel = new JLabel("Semester");
        String[] semesterOptions = {"1", "2", "3", "4", "5", "6", "7"};
        semesterComboBox = new JComboBox<>(semesterOptions);

        // Courses (Checkbox)
        JLabel courseLabel = new JLabel("Courses");
        coursesPanel = new JPanel();  // Panel to hold the course checkboxes
        coursesPanel.setLayout(new GridLayout(0, 1));


// Address
        JLabel addressLabel = new JLabel("Address");
        JTextField addressField = new JTextField(20);

        // Contact
        JLabel contactLabel = new JLabel("Contact");
        JTextField contactField = new JTextField(20);

        // Register Button
        JButton registerButton = new JButton("Register");
        registerButton.setBackground(Color.BLUE);
        registerButton.setForeground(Color.WHITE);

        // Action Listener for the Register Button
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String fullName = nameField.getText();
                String studentID = idField.getText();
                String selectedDepartment = (String) departmentComboBox.getSelectedItem();
                String selectedSemester = (String) semesterComboBox.getSelectedItem();
                String address = addressField.getText();
                String contact = contactField.getText();
                List<String> selectedCourses = new ArrayList<>();

                Component[] components = coursesPanel.getComponents();
                for (Component component : components) {
                    if (component instanceof JCheckBox) {
                        JCheckBox courseCheckbox = (JCheckBox) component;
                        if (courseCheckbox.isSelected()) {
                            selectedCourses.add(courseCheckbox.getActionCommand());
                        }
                    }
                }
                if (fullName.isEmpty() || studentID.isEmpty() || selectedDepartment == null || selectedSemester == null || address.isEmpty() || contact.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please fill in all fields before registering.", "Input Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    // Insert data into the database
                    try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/studentregistration", "root", "Betelhem@sql")) {
                        String sql = "INSERT INTO Registeredstudent (student_id_number, full_name, department, semester, address, contact) VALUES (?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                            pstmt.setString(1, studentID);
                            pstmt.setString(2, fullName);
                            pstmt.setString(3, selectedDepartment);
                            pstmt.setInt(4, Integer.parseInt(selectedSemester));
                            pstmt.setString(5, address);
                            pstmt.setString(6, contact);

                            int rowsInserted = pstmt.executeUpdate();
                            if (rowsInserted > 0) {
                                int generatedKey = -1;
                                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                                if (generatedKeys.next()) {
                                    generatedKey = generatedKeys.getInt(1);
                                }
                                pstmt.close();
                                insertCourseRegistrations(conn, generatedKey, selectedCourses);
                                JOptionPane.showMessageDialog(null, "Student registered successfully!");
                            } else {
                                JOptionPane.showMessageDialog(null, "Student registration failed.");
                            }
                        }
                    } catch (SQLException ex) {
                         //JOptionPane.showMessageDialog(null, "Error registering the student: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();

                       
                    }
                }
            }
        });

        // Action Listener for the Department ComboBox
        departmentComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateCourseCheckboxes();
            }
        });
        // Action Listener for the Semester ComboBox
        semesterComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateCourseCheckboxes();
            }
        });


        constraints.gridx = 0;
        constraints.gridy = 0;
        registrationPanel.add(nameLabel, constraints);
        constraints.gridx = 1;
        registrationPanel.add(nameField, constraints);
        constraints.gridx = 0;
        constraints.gridy = 1;
        registrationPanel.add(idLabel, constraints);
        constraints.gridx = 1;
        registrationPanel.add(idField, constraints);
        constraints.gridx = 0;
        constraints.gridy = 2;
        registrationPanel.add(departmentLabel, constraints);
        constraints.gridx = 1;
        registrationPanel.add(departmentComboBox, constraints);
        constraints.gridx = 0;
        constraints.gridy = 3;
        registrationPanel.add(semesterLabel, constraints);
        constraints.gridx = 1;
        registrationPanel.add(semesterComboBox, constraints);
        constraints.gridx = 0;
        constraints.gridy = 4;
        registrationPanel.add(courseLabel, constraints);
        constraints.gridx = 1;
        registrationPanel.add(coursesPanel, constraints);
        constraints.gridx = 0;
        constraints.gridy = 5;
        registrationPanel.add(addressLabel, constraints);
        constraints.gridx = 1;
        registrationPanel.add(addressField, constraints);
        constraints.gridx = 0;
        constraints.gridy = 6;
        registrationPanel.add(contactLabel, constraints);
        constraints.gridx = 1;
        registrationPanel.add(contactField, constraints);
        constraints.gridx = 0;
        constraints.gridy = 7;
        constraints.gridwidth = 2;
        registrationPanel.add(registerButton, constraints);

        add(registrationPanel, BorderLayout.CENTER);
        // ... (The code for the StudentRegistrationPanel as you provided)
    }

    // ... (The rest of the code for StudentRegistrationPanel as you provided)
    private void updateCourseCheckboxes() {
        String selectedDepartment = (String) departmentComboBox.getSelectedItem();
        String selectedSemester = (String) semesterComboBox.getSelectedItem();
        List<JCheckBox> courseCheckboxes = fetchCoursesFromDatabase(selectedDepartment, selectedSemester);
        coursesPanel.removeAll();
        JScrollPane scrollPane = new JScrollPane();
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));

        for (JCheckBox courseCheckbox : courseCheckboxes) {
            coursesPanel.add(courseCheckbox);
        }
        scrollPane.setViewportView(scrollContent);
        coursesPanel.removeAll();
        coursesPanel.setLayout(new BorderLayout());
        coursesPanel.add(scrollPane, BorderLayout.CENTER);
        coursesPanel.revalidate();
        coursesPanel.repaint();
        coursesPanel.revalidate();
        coursesPanel.repaint();
    }

    private List<JCheckBox> fetchCoursesFromDatabase(String department, String semester) {
        List<JCheckBox> courseCheckboxes = new ArrayList<>();

        if (department.isEmpty() || semester.isEmpty()) {
            // If no department or semester is selected, return an empty list
            return courseCheckboxes;
        }

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/studentregistration", "root", "123456")) {
            String sql = "SELECT course_code, course_name FROM base_course WHERE department_name = ? AND semester = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, department);
                pstmt.setInt(2, Integer.parseInt(semester));
                ResultSet resultSet = pstmt.executeQuery();
                while (resultSet.next()) {
                    String courseCode = resultSet.getString("course_code");
                    String courseName = resultSet.getString("course_name");
                    JCheckBox courseCheckbox = new JCheckBox(courseName);
                    courseCheckbox.setActionCommand(courseCode);
                    courseCheckboxes.add(courseCheckbox);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return courseCheckboxes;
    }


    private void insertCourseRegistrations(Connection conn, int studentId, List<String> selectedCourses) throws SQLException {
        String sql = "INSERT INTO studentcourseregistration (student_id_number, course_code, course_grade) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (String courseCode : selectedCourses) {
                pstmt.setInt(1, studentId);
                pstmt.setString(2, courseCode);
                pstmt.setString(3, ""); // You can specify the course grade here if needed
                pstmt.executeUpdate();
            }
        }
        catch (SQLException ex) {
                         JOptionPane.showMessageDialog(null, "Error registering the student: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();

                       
                    }
    }
}

class StudentResultsPanel extends JPanel {
    private JTextField idTextField;
    private JTable resultTable;
    private JLabel gpaLabel;

    public StudentResultsPanel() {
        setLayout(new BorderLayout());
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel idLabel = new JLabel("Student ID: ");
        idTextField = new JTextField(10);
        JButton showResultButton = new JButton("Show Results");
        showResultButton.setBackground(Color.BLUE);
        showResultButton.setForeground(Color.white);
        inputPanel.add(idLabel);
        inputPanel.add(idTextField);
        inputPanel.add(showResultButton);
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(resultTable);
        resultPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel gpaPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        gpaLabel = new JLabel();
        gpaPanel.add(gpaLabel);

        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(resultPanel, BorderLayout.CENTER);
        mainPanel.add(gpaPanel, BorderLayout.SOUTH);

        add(mainPanel);

        showResultButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String studentId = idTextField.getText();
                displayCourseResults(studentId);
            }
        });
        // ... (The code for the StudentResultsPanel as you provided)
    }
    private void displayCourseResults(String studentId) {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Course");
        model.addColumn("Grade");

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/studentregistration", "root", "Betelhem@sql")) {
            String query = "SELECT course_code, course_grade FROM studentcourseregistration WHERE student_id_number = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, studentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String courseCode = rs.getString("course_code");
                String grade = rs.getString("course_grade");
                model.addRow(new Object[]{courseCode, grade});
            }

            resultTable.setModel(model);

            double gpa = calculateGPA(model);
            gpaLabel.setText("GPA: " + gpa);

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to the database: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private double calculateGPA(DefaultTableModel model) {
        double totalPoints = 0.0;
        int totalCredits = 0;

        for (int row = 0; row < model.getRowCount(); row++) {
            String grade = model.getValueAt(row, 1).toString();
            int creditHour = getCreditHour(model.getValueAt(row, 0).toString());

            totalPoints += calculateGradePoints(grade) * creditHour;
            totalCredits += creditHour;
        }

        if (totalCredits == 0) {
            return 0.0;
        }

        return totalPoints / totalCredits;
    }

    private int getCreditHour(String courseCode) {
        // Retrieve the credit hour for the given course code from your database or a predefined map.
        // Replace this with your logic to fetch credit hours based on the course code.
        // For simplicity, you can use a predefined map for course codes and credit hours.
        // Example: Map<String, Integer> creditHourMap = new HashMap<>();
        // int creditHour = creditHourMap.getOrDefault(courseCode, 0);

        return 3; // Default credit hour (change as needed)
    }

    private double calculateGradePoints(String grade) {
        switch (grade) {
            case "A+":
            case "A":
                return 4.0;
            case "A-":
                return 3.7;
            case "B+":
                return 3.3;
            case "B":
                return 3.0;
            case "B-":
                return 2.7;
            case "C+":
                return 2.3;
            case "C":
                return 2.0;
            case "C-":
                return 1.7;
            case "D+":
                return 1.3;
            case "D":
                return 1.0;
            default:
                return 0.0; // Handle other grades or errors
        }
    // ... (The rest of the code for StudentResultsPanel as you provided)
}}
