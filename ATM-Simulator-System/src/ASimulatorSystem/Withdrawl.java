package ASimulatorSystem;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.sql.*;
import java.util.Date;

public class Withdrawl extends JFrame implements ActionListener {
    JTextField t1;
    JButton b1, b2;
    String pin;

    Withdrawl(String pin) {
        this.pin = pin;

        setLayout(null);

        JLabel l1 = new JLabel("ENTER AMOUNT YOU WANT TO WITHDRAW");
        l1.setFont(new Font("System", Font.BOLD, 16));
        l1.setBounds(150, 150, 400, 30);
        add(l1);

        t1 = new JTextField();
        t1.setBounds(150, 200, 200, 30);
        add(t1);

        b1 = new JButton("Withdraw");
        b1.setBounds(150, 250, 100, 30);
        add(b1);

        b2 = new JButton("Back");
        b2.setBounds(260, 250, 100, 30);
        add(b2);

        b1.addActionListener(this);
        b2.addActionListener(this);

        setSize(600, 400);
        setLocation(400, 200);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == b2) {
            setVisible(false);
            new Transactions(pin).setVisible(true);
            return;
        }

        String amountStr = t1.getText().trim();
        if (amountStr.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter the amount to withdraw");
            return;
        }

        try {
            int amount = Integer.parseInt(amountStr);
            if (amount <= 0) {
                JOptionPane.showMessageDialog(null, "Amount must be positive");
                return;
            }

            try (Connection c = DriverManager.getConnection("jdbc:mysql:///atm_simulator", "root", "root")) {
                // Check balance
                int balance = 0;
                Statement stmt = c.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT mode, amount FROM bank WHERE pin = '" + pin + "'");
                while (rs.next()) {
                    if ("Deposit".equals(rs.getString("mode"))) {
                        balance += rs.getInt("amount");
                    } else {
                        balance -= rs.getInt("amount");
                    }
                }

                if (balance < amount) {
                    JOptionPane.showMessageDialog(null, "Insufficient Balance. Current: Rs. " + balance);
                    return;
                }

                // Insert withdrawal transaction
                String q1 = "INSERT INTO bank (pin, transaction_date, mode, amount) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmt = c.prepareStatement(q1)) {
                    pstmt.setString(1, pin);
                    pstmt.setTimestamp(2, new Timestamp(new Date().getTime()));
                    pstmt.setString(3, "Withdrawal");
                    pstmt.setInt(4, amount);
                    pstmt.executeUpdate();

                    JOptionPane.showMessageDialog(null, "Rs. " + amount + " Withdrawn Successfully");
                    setVisible(false);
                    new Transactions(pin).setVisible(true);
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Please enter a valid numeric amount");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new Withdrawl("").setVisible(true);
    }
}
