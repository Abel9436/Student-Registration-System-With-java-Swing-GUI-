import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;

class SStudentDataEntryApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createAndShowGUUI());
    }

    static void createAndShowGUUI() {
        JFrame frame = new JFrame("Student Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);


        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Insert and Update grade", new StudentDataEntryPanel());
        tabbedPane.addTab("Registration a Student", new StudentRegistrationPanel());
        tabbedPane.addTab("Search a Student", new StudentSearchPanel());
        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(Color.BLUE);
        logoutButton.setForeground(Color.WHITE);

        // Define the action when the Logout Button is clicked
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Handle the logout action here, e.g., by closing the current window or returning to a login page.
                int choice = JOptionPane.showConfirmDialog(frame, "Are you sure you want to log out?", "Logout", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    // Handle the logout action here, e.g., by closing the current window or returning to a login page.
                    frame.dispose();
                    LoginPage.createAndShowUI();// Close the current window
                } // Close the current window

            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(logoutButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
        frame.add(tabbedPane);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
class StudentDataEntryPanel extends JPanel {
    private JComboBox<String> courseCodeComboBox;

    public StudentDataEntryPanel() {
        setLayout(new BorderLayout());

        JLabel idLabel = new JLabel("Student ID");
        JTextField idField = new JTextField(20);

        JLabel gradeLabel = new JLabel("Grade");
        String[] gradeOptions = {"A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D", "F"};
        JComboBox<String> gradeComboBox = new JComboBox<>(gradeOptions);

        JLabel actionLabel = new JLabel("Action");
        String[] actionOptions = {"Insert", "Update"};
        JComboBox<String> actionComboBox = new JComboBox<>(actionOptions);

        courseCodeComboBox = new JComboBox<>(fetchCourseCodesFromDatabase());

        JButton executeButton = new JButton("Execute");
        executeButton.setBackground(Color.BLUE);
        executeButton.setForeground(Color.WHITE);

        executeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String studentID = idField.getText();
                String courseCode = (String) courseCodeComboBox.getSelectedItem();
                String selectedGrade = (String) gradeComboBox.getSelectedItem();
                String selectedAction = (String) actionComboBox.getSelectedItem();

                // Check if the student ID exists in the database
                boolean exists = isStudentIDExists(studentID);
                if (!exists) {
                    JOptionPane.showMessageDialog(null, "Student ID does not exist in the database.", "Invalid Student ID", JOptionPane.ERROR_MESSAGE);
                } else {
                    String dbUrl = "jdbc:mysql://localhost:3306/studentregistration";
                String dbUser = "root";
                String dbPassword = "Betelhem@sql";
                try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
                    if ("Insert".equals(selectedAction)) {
                        // Check if the record already exists
                        String checkSql = "SELECT * FROM studentcourseregistration WHERE student_id_number = ? AND course_code = ?";
                        try (PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {
                            checkPstmt.setString(1, studentID);
                            checkPstmt.setString(2, courseCode);
                            ResultSet resultSet = checkPstmt.executeQuery();
                            if (resultSet.next()) {
                                JOptionPane.showMessageDialog(getParent(), "Data already exists for the given ID and Course Code.");
                            } else {
                                String insertSql = "INSERT INTO studentcourseregistration (student_id_number, course_code, course_grade) VALUES (?, ?, ?)";
                                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                                    pstmt.setString(1, studentID);
                                    pstmt.setString(2, courseCode);
                                    pstmt.setString(3, selectedGrade);

                                    int rowsAffected = pstmt.executeUpdate();
                                    if (rowsAffected > 0) {
                                        JOptionPane.showMessageDialog(getParent(), "Data inserted into the database.");
                                    } else {
                                        JOptionPane.showMessageDialog(getParent(), "Data insertion failed.");
                                    }
                                }
                            }
                        }
                    } else if ("Update".equals(selectedAction)) {
                        // Check if the record exists for an update
                        String checkSql = "SELECT * FROM studentcourseregistration WHERE student_id_number = ? AND course_code = ?";
                        try (PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {
                            checkPstmt.setString(1, studentID);
                            checkPstmt.setString(2, courseCode);
                            ResultSet resultSet = checkPstmt.executeQuery();
                            if (resultSet.next()) {
                                String updateSql = "UPDATE studentcourseregistration SET course_grade = ? WHERE student_id_number = ? AND course_code = ?";
                                try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                                    pstmt.setString(1, selectedGrade);
                                    pstmt.setString(2, studentID);
                                    pstmt.setString(3, courseCode);

                                    int rowsAffected = pstmt.executeUpdate();
                                    if (rowsAffected > 0) {
                                        JOptionPane.showMessageDialog(getParent(), "Data updated in the database.");
                                    } else {
                                        JOptionPane.showMessageDialog(getParent(), "Data update failed.");
                                    }
                                }
                            } else {
                                JOptionPane.showMessageDialog(getParent(), "Data does not exist for the given ID and Course Code.");
                            }
                        }
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(getParent(), "Error: " + ex.getMessage());
                }
                    // Student ID exists, proceed with inserting or updating the grade
                    // Rest of the code for inserting or updating student grades
                }
            }
        });

        JPanel dataEntryPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 10, 5, 10);

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        dataEntryPanel.add(idLabel, constraints);
        constraints.gridx = 1;
        constraints.anchor = GridBagConstraints.WEST;
        dataEntryPanel.add(idField, constraints);

        JLabel courseCodeLabel = new JLabel("Course Code");
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.anchor = GridBagConstraints.WEST;
        dataEntryPanel.add(courseCodeLabel, constraints);
        constraints.gridx = 1;
        constraints.anchor = GridBagConstraints.WEST;
        dataEntryPanel.add(courseCodeComboBox, constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.anchor = GridBagConstraints.WEST;
        dataEntryPanel.add(gradeLabel, constraints);
        constraints.gridx = 1;
        constraints.anchor = GridBagConstraints.WEST;
        dataEntryPanel.add(gradeComboBox, constraints);

        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.anchor = GridBagConstraints.WEST;
        dataEntryPanel.add(actionLabel, constraints);
        constraints.gridx = 1;
        constraints.anchor = GridBagConstraints.WEST;
        dataEntryPanel.add(actionComboBox, constraints);

        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        dataEntryPanel.add(executeButton, constraints);

        add(dataEntryPanel, BorderLayout.CENTER);
    }

    private boolean isStudentIDExists(String studentID) {
        boolean exists = false;

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/studentregistration", "root", "Betelhem@sql")) {
            String sql = "SELECT COUNT(*) FROM Registeredstudent WHERE student_id_number = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, studentID);
                ResultSet resultSet = pstmt.executeQuery();
                if (resultSet.next() && resultSet.getInt(1) > 0) {
                    exists = true;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occurred while checking the student ID.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        return exists;
    }
    // Fetch course codes from the database and create an array
    private String[] fetchCourseCodesFromDatabase() {
        List<String> courseCodes = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/studentregistration", "root", "Betelhem@sql")) {
            String sql = "SELECT course_code FROM base_course";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                ResultSet resultSet = pstmt.executeQuery();
                while (resultSet.next()) {
                    String courseCode = resultSet.getString("course_code");
                    courseCodes.add(courseCode);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return courseCodes.toArray(new String[0]);
    }
}



class StudentRegistrationPanel extends JPanel {
    private JComboBox<String> departmentComboBox;
    private JComboBox<String> semesterComboBox;
    private JPanel coursesPanel;

    public StudentRegistrationPanel() {
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
                                JOptionPane.showMessageDialog(null, "Student registered successfully!");

                                insertCourseRegistrations(conn, generatedKey, selectedCourses);
                            } else {
                                JOptionPane.showMessageDialog(null, "Student registration failed.");
                            }
                        }
                    } catch (SQLException ex) {
                        //ex.printStackTrace();

                        JOptionPane.showMessageDialog(null, "Error registering the student: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
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

    // Use BoxLayout to arrange checkboxes vertically
    coursesPanel.setLayout(new BoxLayout(coursesPanel, BoxLayout.Y_AXIS));

    for (JCheckBox courseCheckbox : courseCheckboxes) {
        coursesPanel.add(courseCheckbox);
        System.out.println("Added course checkbox: " + courseCheckbox.getText());
    }

    // Revalidate and repaint the coursesPanel
    coursesPanel.revalidate();
    coursesPanel.repaint();
}

    private List<JCheckBox> fetchCoursesFromDatabase(String department, String semester) {
        List<JCheckBox> courseCheckboxes = new ArrayList<>();

        if (department.isEmpty() || semester.isEmpty()) {
            // If no department or semester is selected, return an empty list
            return courseCheckboxes;
        }

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/studentregistration", "root", "Betelhem@sql")) {
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
    }
}

class StudentSearchPanel extends JPanel {
    public StudentSearchPanel() {
        setLayout(new BorderLayout());

        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        JTable studentTable = new JTable();

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        searchButton.setBackground(Color.BLUE);
        searchButton.setForeground(Color.WHITE);

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String searchTerm = searchField.getText();
                updateStudentTable(searchTerm, studentTable);
            }
        });

        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        JScrollPane tableScrollPane = new JScrollPane(studentTable);

        add(searchPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
    }

    private void updateStudentTable(String searchTerm, JTable studentTable) {
        // Connect to the database and fetch student data
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/studentregistration", "root", "Betelhem@sql")) {
            String query = "SELECT student_id, full_name, department FROM Registeredstudent WHERE student_id_number LIKE ?";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, "%" + searchTerm + "%");
            ResultSet resultSet = preparedStatement.executeQuery();

            // Create a table model and populate it with the data
            Vector<String> columnNames = new Vector<>();
            columnNames.add("No_");
            columnNames.add("Student Name");
            columnNames.add("Student Department");

            Vector<Vector<String>> data = new Vector<>();
            while (resultSet.next()) {
                Vector<String> row = new Vector<>();
                row.add(resultSet.getString("student_id"));
                row.add(resultSet.getString("full_name"));
                row.add(resultSet.getString("department"));
                data.add(row);
            }

            DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);
            studentTable.setModel(tableModel);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
