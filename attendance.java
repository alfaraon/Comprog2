package milestonepayroll;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DataGatheringPanel extends JFrame {
    private JTextField idField, birthdayField, positionField, hourlyRateField;
    private JTextField logInField, logOutField;
    private DefaultTableModel tableModel;
    private JTable employeeTable;

    public DataGatheringPanel() {
        setTitle("MotorPH Employee Manager & Payroll");
        setSize(850, 550);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(7, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Employee Info"));

        idField = new JTextField();
        birthdayField = new JTextField();
        positionField = new JTextField();
        hourlyRateField = new JTextField();
        logInField = new JTextField("08:00");
        logOutField = new JTextField("17:00");

        inputPanel.add(new JLabel("ID:"));
        inputPanel.add(idField);
        inputPanel.add(new JLabel("Birthday (MM-DD-YYYY):"));
        inputPanel.add(birthdayField);
        inputPanel.add(new JLabel("Position:"));
        inputPanel.add(positionField);
        inputPanel.add(new JLabel("Hourly Rate:"));
        inputPanel.add(hourlyRateField);
        inputPanel.add(new JLabel("Log In Time (HH:MM):"));
        inputPanel.add(logInField);
        inputPanel.add(new JLabel("Log Out Time (HH:MM):"));
        inputPanel.add(logOutField);

        JButton addBtn = new JButton("Add");
        JButton updateBtn = new JButton("Update");
        JButton deleteBtn = new JButton("Delete");
        JButton loadBtn = new JButton("View All");
        JButton payrollBtn = new JButton("Calculate Payroll");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addBtn);
        buttonPanel.add(updateBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(loadBtn);
        buttonPanel.add(payrollBtn);

        String[] cols = {"ID", "Birthday", "Position", "Hourly Rate"};
        tableModel = new DefaultTableModel(cols, 0);
        employeeTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(employeeTable);

        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> addEmployee());
        updateBtn.addActionListener(e -> updateEmployee());
        deleteBtn.addActionListener(e -> deleteEmployee());
        loadBtn.addActionListener(e -> loadEmployees());
        payrollBtn.addActionListener(e -> calculatePayroll());

        employeeTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = employeeTable.getSelectedRow();
                idField.setText(tableModel.getValueAt(row, 0).toString());
                birthdayField.setText(tableModel.getValueAt(row, 1).toString());
                positionField.setText(tableModel.getValueAt(row, 2).toString());
                hourlyRateField.setText(tableModel.getValueAt(row, 3).toString());
            }
        });

        setVisible(true);
    }

    private void addEmployee() {
        String id = idField.getText().trim();
        String bday = birthdayField.getText().trim();
        String position = positionField.getText().trim();
        double rate;
        try {
            rate = Double.parseDouble(hourlyRateField.getText().trim());
            EmployeeDatabase.addEmployee(id, bday, position, rate);
            JOptionPane.showMessageDialog(this, "Employee added successfully!");
            loadEmployees();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid hourly rate!");
        }
    }

    private void updateEmployee() {
        String id = idField.getText().trim();
        String[] updated = {
            id,
            birthdayField.getText().trim(),
            positionField.getText().trim(),
            hourlyRateField.getText().trim()
        };
        boolean success = EmployeeDatabase.updateEmployee(id, updated);
        if (success) {
            JOptionPane.showMessageDialog(this, "Employee updated successfully!");
            loadEmployees();
        } else {
            JOptionPane.showMessageDialog(this, "Employee ID not found.");
        }
    }

    private void deleteEmployee() {
        String id = idField.getText().trim();
        boolean success = EmployeeDatabase.deleteEmployee(id);
        if (success) {
            JOptionPane.showMessageDialog(this, "Employee deleted.");
            loadEmployees();
        } else {
            JOptionPane.showMessageDialog(this, "Employee ID not found.");
        }
    }

    private void loadEmployees() {
        tableModel.setRowCount(0);
        List<String[]> data = EmployeeDatabase.loadEmployees();
        for (String[] row : data) {
            tableModel.addRow(row);
        }
    }

    private void calculatePayroll() {
        String employeeId = idField.getText().trim();
        double hourlyRate;

        try {
            hourlyRate = Double.parseDouble(hourlyRateField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid hourly rate!");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader("attendance.csv"))) {
            String line;
            boolean found = false;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3 && parts[0].trim().equalsIgnoreCase(employeeId)) {
                    found = true;

                    LocalTime in = LocalTime.parse(parts[1].trim(), DateTimeFormatter.ofPattern("HH:mm"));
                    LocalTime out = LocalTime.parse(parts[2].trim(), DateTimeFormatter.ofPattern("HH:mm"));

                    if (out.isBefore(in)) {
                        out = out.plusHours(24);  // Handle overnight shifts
                    }

                    long minutes = Duration.between(in, out).toMinutes();
                    double hours = minutes / 60.0;

                    double gross = hourlyRate * hours;
                    double sss = gross * 0.045;
                    double philHealth = gross * 0.035;
                    double pagibig = gross * 0.02;
                    double tax = gross > 20000 ? gross * 0.10 : gross * 0.05;
                    double net = gross - (sss + philHealth + pagibig + tax);

                    String summary = String.format(
                        "PAYROLL SUMMARY:\nHours Worked: %.2f\nGross Salary: ₱%.2f\nSSS: ₱%.2f\nPhilHealth: ₱%.2f\nPag-IBIG: ₱%.2f\nTax: ₱%.2f\nNet Pay: ₱%.2f",
                        hours, gross, sss, philHealth, pagibig, tax, net
                    );
                    JOptionPane.showMessageDialog(this, summary, "Payroll Calculation", JOptionPane.INFORMATION_MESSAGE);
                    break;
                }
            }

            if (!found) {
                JOptionPane.showMessageDialog(this, "No attendance record found for employee ID: " + employeeId);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error reading attendance data.");
            e.printStackTrace();
        }
    }
}