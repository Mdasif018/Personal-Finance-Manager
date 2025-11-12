

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class FinanceClient extends JFrame {
    Socket socket;
    PrintWriter out;
    BufferedReader in;

    JTextField typeField, categoryField, amountField;
    JTextArea logArea;

    public FinanceClient() {
        setTitle("💼 Personal Finance Manager");
        setSize(500, 500);
        setLayout(new FlowLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        add(new JLabel("Type (Income/Expense):"));
        typeField = new JTextField(10); add(typeField);

        add(new JLabel("Category:"));
        categoryField = new JTextField(10); add(categoryField);

        add(new JLabel("Amount:"));
        amountField = new JTextField(10); add(amountField);

        JButton addBtn = new JButton("Add Transaction");
        JButton balBtn = new JButton("Check Balance");
        add(addBtn); add(balBtn);

        logArea = new JTextArea(18, 40);
        add(new JScrollPane(logArea));

        addBtn.addActionListener(e -> sendTransaction());
        balBtn.addActionListener(e -> requestBalance());

        connectToServer();
        setVisible(true);
    }

    void connectToServer() {
        try {
            socket = new Socket("localhost", 5051);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            logArea.append("✅ Connected to Finance Server\n");
        } catch (Exception e) {
            logArea.append("❌ Failed to connect to server\n");
        }
    }

    void sendTransaction() {
        try {
            String type = typeField.getText().trim();
            String cat = categoryField.getText().trim();
            String amt = amountField.getText().trim();

            if (type.isEmpty() || cat.isEmpty() || amt.isEmpty()) {
                logArea.append("⚠️ Fill all fields\n");
                return;
            }

            out.println(type + "," + cat + "," + amt);
            readServerReply();
        } catch (Exception e) {
            logArea.append("❌ Error sending transaction\n");
        }
    }

    void requestBalance() {
        try {
            out.println("BALANCE");
            readServerReply();
        } catch (Exception e) {
            logArea.append("❌ Error getting balance\n");
        }
    }

    void readServerReply() {
        try {
            String line;
            StringBuilder sb = new StringBuilder();

            while ((line = in.readLine()) != null) {
                if (line.equals("END")) break; // ✅ stop cleanly
                sb.append(line).append("\n");
            }

            logArea.append(sb.toString());
        } catch (IOException e) {
            logArea.append("⚠️ Error reading server response\n");
        }
    }

    public static void main(String[] args) {
        new FinanceClient();
    }
}
