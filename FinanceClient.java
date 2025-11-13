
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class FinanceClient extends JFrame {
    Socket socket;
    PrintWriter out;
    BufferedReader in;

    JTextField userField, typeField, categoryField, amountField;
    JPasswordField passField;
    JTextArea logArea;

    JButton loginBtn, signupBtn, addBtn, balBtn, logoutBtn;

    public FinanceClient() {
        connectToServer();
        showLoginUI();
    }

    // === LOGIN UI ===
    void showLoginUI() {
        getContentPane().removeAll();
        setTitle("🔐 Finance Manager Login");
        setSize(400, 300);
        setLayout(new FlowLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        add(new JLabel("Username:"));
        userField = new JTextField(15); add(userField);

        add(new JLabel("Password:"));
        passField = new JPasswordField(15); add(passField);

        loginBtn = new JButton("Login");
        signupBtn = new JButton("Sign Up");
        add(loginBtn); add(signupBtn);

        logArea = new JTextArea(8, 30);
        add(new JScrollPane(logArea));

        loginBtn.addActionListener(e -> login());
        signupBtn.addActionListener(e -> signup());

        revalidate();
        repaint();
        setVisible(true);
    }

    // === MAIN APP UI ===
    void showMainUI() {
        getContentPane().removeAll();
        setTitle("💼 Personal Finance Manager");
        setSize(500, 500);
        setLayout(new FlowLayout());

        add(new JLabel("Type (Income/Expense):"));
        typeField = new JTextField(10); add(typeField);

        add(new JLabel("Category:"));
        categoryField = new JTextField(10); add(categoryField);

        add(new JLabel("Amount:"));
        amountField = new JTextField(10); add(amountField);

        addBtn = new JButton("Add Transaction");
        balBtn = new JButton("Check Balance");
        logoutBtn = new JButton("Logout");
        add(addBtn); add(balBtn); add(logoutBtn);

        logArea = new JTextArea(18, 40);
        add(new JScrollPane(logArea));

        addBtn.addActionListener(e -> sendTransaction());
        balBtn.addActionListener(e -> requestBalance());
        logoutBtn.addActionListener(e -> logout());

        revalidate();
        repaint();
    }

    // === CONNECTION ===
    void connectToServer() {
        try {
            socket = new Socket("localhost", 5051);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("✅ Connected to server");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Failed to connect to server");
        }
    }

    // === LOGIN ===
    void login() {
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword()).trim();
        if (user.isEmpty() || pass.isEmpty()) {
            logArea.append("⚠️ Enter username and password\n");
            return;
        }
        out.println("LOGIN," + user + "," + pass);
        String resp = readServerReply();
        logArea.append(resp);
        if (resp.contains("Login Successful")) {
            showMainUI();
        }
    }

    // === SIGNUP ===
    void signup() {
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword()).trim();
        if (user.isEmpty() || pass.isEmpty()) {
            logArea.append("⚠️ Enter username and password\n");
            return;
        }
        out.println("SIGNUP," + user + "," + pass);
        logArea.append(readServerReply());
    }

    // === TRANSACTION ===
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
            logArea.append(readServerReply());
        } catch (Exception e) {
            logArea.append("❌ Error sending transaction\n");
        }
    }

    // === BALANCE ===
    void requestBalance() {
        out.println("BALANCE");
        logArea.append(readServerReply());
    }

    // === LOGOUT ===
    void logout() {
        out.println("LOGOUT");
        logArea.append(readServerReply());
        showLoginUI();
    }

    // === SERVER REPLY ===
    String readServerReply() {
        try {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                if (line.equals("END")) break;
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "⚠️ Error reading response\n";
        }
    }

    public static void main(String[] args) {
        new FinanceClient();
    }
}
