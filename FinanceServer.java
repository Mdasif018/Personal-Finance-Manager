import java.io.*;
import java.net.*;
import java.sql.*;

public class FinanceServer {
    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            ServerSocket server = new ServerSocket(5051);
            System.out.println("💼 Finance Server Running...");

            Socket socket = server.accept();
            System.out.println("✅ Client Connected");

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/finance_db?useSSL=false&serverTimezone=UTC",
                "root",
                "Asif@2007"
            );
            Statement stmt = conn.createStatement();

            int currentUserId = -1;
            String input;

            while ((input = in.readLine()) != null) {
                input = input.trim();

                // --- SIGNUP ---
                if (input.startsWith("SIGNUP")) {
                    String[] parts = input.split(",");
                    if (parts.length == 3) {
                        String user = parts[1];
                        String pass = parts[2];
                        try {
                            stmt.executeUpdate("INSERT INTO users(username, password) VALUES('" + user + "','" + pass + "')");
                            out.println("✅ Signup successful! You can now login.");
                        } catch (SQLException e) {
                            out.println("⚠️ Username already exists!");
                        }
                    } else {
                        out.println("⚠️ Invalid SIGNUP format.");
                    }
                    out.println("END");
                }

                // --- LOGIN ---
                else if (input.startsWith("LOGIN")) {
                    String[] parts = input.split(",");
                    if (parts.length == 3) {
                        String user = parts[1];
                        String pass = parts[2];
                        ResultSet rs = stmt.executeQuery("SELECT id FROM users WHERE username='" + user + "' AND password='" + pass + "'");
                        if (rs.next()) {
                            currentUserId = rs.getInt(1);
                            out.println("✅ Login Successful! Welcome, " + user);
                        } else {
                            out.println("❌ Invalid username or password");
                        }
                    } else {
                        out.println("⚠️ Invalid LOGIN format.");
                    }
                    out.println("END");
                }

                // --- BALANCE REQUEST ---
                else if (input.equalsIgnoreCase("BALANCE")) {
                    if (currentUserId == -1) {
                        out.println("⚠️ Please login first!");
                        out.println("END");
                    } else {
                        sendFullBalance(stmt, out, currentUserId);
                    }
                }

                // --- LOGOUT ---
                else if (input.equalsIgnoreCase("LOGOUT")) {
                    currentUserId = -1;
                    out.println("👋 Logged out successfully");
                    out.println("END");
                }

                // --- TRANSACTION ADD ---
                else {
                    if (currentUserId == -1) {
                        out.println("⚠️ Please login first!");
                        out.println("END");
                        continue;
                    }

                    String[] parts = input.split(",");
                    if (parts.length == 3) {
                        String type = parts[0].trim();
                        String category = parts[1].trim();
                        double amount = Double.parseDouble(parts[2]);

                        stmt.executeUpdate(
                            "INSERT INTO transactions(user_id, type, category, amount) VALUES (" +
                            currentUserId + ",'" + type + "','" + category + "'," + amount + ")"
                        );

                        out.println("✅ Transaction Added Successfully");
                        out.println("END");
                    } else {
                        out.println("⚠️ Invalid input format. Use: Type,Category,Amount");
                        out.println("END");
                    }
                }
            }

            conn.close();
            socket.close();
            server.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendFullBalance(Statement stmt, PrintWriter out, int userId) throws SQLException {
        out.println("\n📊 YOUR FINANCE RECORDS");
        out.println("-----------------------------");

        ResultSet rs = stmt.executeQuery("SELECT type, category, amount FROM transactions WHERE user_id=" + userId);

        while (rs.next()) {
            out.println(rs.getString(1) + " | " + rs.getString(2) + " | ₹" + rs.getDouble(3));
        }

        out.println("-----------------------------");

        double income = 0, expense = 0;

        rs = stmt.executeQuery("SELECT SUM(amount) FROM transactions WHERE type='Income' AND user_id=" + userId);
        if (rs.next()) income = rs.getDouble(1);

        rs = stmt.executeQuery("SELECT SUM(amount) FROM transactions WHERE type='Expense' AND user_id=" + userId);
        if (rs.next()) expense = rs.getDouble(1);

        double balance = income - expense;

        out.println("💰 Total Income: ₹" + income);
        out.println("💸 Total Expense: ₹" + expense);
        out.println("👛 Current Balance: ₹" + balance);
        out.println("-----------------------------");
        out.println("END");
    }
}
