package ASimulatorSystem;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.sql.*;
import java.util.Date;

public class FastCash extends JFrame implements ActionListener {

    JLabel l1;
    JButton b1, b2, b3, b4, b5, b6, b7;
    String pin;

    FastCash(String pin) {
        this.pin = pin;

        ImageIcon i1 = new ImageIcon(ClassLoader.getSystemResource("ASimulatorSystem/icons/atm.jpg"));
        Image i2 = i1.getImage().getScaledInstance(1000, 1180, Image.SCALE_DEFAULT);
        JLabel l3 = new JLabel(new ImageIcon(i2));
        l3.setBounds(0, 0, 960, 1080);
        add(l3);

        l1 = new JLabel("SELECT WITHDRAWAL AMOUNT");
        l1.setForeground(Color.WHITE);
        l1.setFont(new Font("System", Font.BOLD, 16));
        l1.setBounds(235, 400, 700, 35);
        l3.add(l1);

        b1 = new JButton("Rs 100");
        b2 = new JButton("Rs 500");
        b3 = new JButton("Rs 1000");
        b4 = new JButton("Rs 2000");
        b5 = new JButton("Rs 5000");
        b6 = new JButton("Rs 10000");
        b7 = new JButton("BACK");

        JButton[] btns = {b1, b2, b3, b4, b5, b6, b7};
        int[][] pos = {{170,499},{390,499},{170,543},{390,543},{170,588},{390,588},{390,633}};
        for (int i = 0; i < btns.length; i++) {
            btns[i].setBounds(pos[i][0], pos[i][1], 150, 35);
            l3.add(btns[i]);
            btns[i].addActionListener(this);
        }

        setLayout(null);
        setSize(960, 1080);
        setUndecorated(true);
        setLocation(500, 0);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == b7) {
            setVisible(false);
            new Transactions(pin).setVisible(true);
            return;
        }

        int amount = Integer.parseInt(((JButton) ae.getSource()).getText().replace("Rs ", ""));

        try (Connection c = DriverManager.getConnection("jdbc:mysql:///atm_simulator", "root", "root")) {
            PreparedStatement ps = c.prepareStatement(
                    "SELECT SUM(CASE WHEN mode='Deposit' THEN amount ELSE -amount END) AS balance FROM bank WHERE pin=?");
            ps.setString(1, pin);
            ResultSet rs = ps.executeQuery();
            int balance = rs.next() ? rs.getInt("balance") : 0;

            if (amount > balance) {
                JOptionPane.showMessageDialog(null, "Insufficient balance. Current balance: Rs. " + balance);
                return;
            }

            PreparedStatement insert = c.prepareStatement(
                    "INSERT INTO bank(pin, transaction_date, mode, amount) VALUES (?, ?, ?, ?)");
            insert.setString(1, pin);
            insert.setTimestamp(2, new java.sql.Timestamp(new Date().getTime()));
            insert.setString(3, "Withdrawal");
            insert.setInt(4, amount);
            insert.executeUpdate();

            JOptionPane.showMessageDialog(null, "Rs. " + amount + " Withdrawn Successfully");
            setVisible(false);
            new Transactions(pin).setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    public static void main(String[] args) {
        new FastCash("").setVisible(true);
    }
}
// git remote add origin https://github.com/username/repo-name.git