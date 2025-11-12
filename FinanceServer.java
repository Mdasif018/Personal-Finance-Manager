
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

            String input;
            while ((input = in.readLine()) != null) {
                input = input.trim();

                if (input.equalsIgnoreCase("BALANCE")) {
                    sendFullBalance(stmt, out);   // ✅ now sends transaction list + summary
                } else {
                    // Adding a transaction
                    String[] parts = input.split(",");
                    if (parts.length == 3) {
                        String type = parts[0].trim();
                        String category = parts[1].trim();
                        double amount = Double.parseDouble(parts[2]);

                        stmt.executeUpdate(
                            "INSERT INTO transactions(type, category, amount) VALUES ('" +
                            type + "','" + category + "'," + amount + ")"
                        );

                        out.println("✅ Transaction Added Successfully");
                        out.println("END");  // ✅ prevents client from reading extra junk
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

    private static void sendFullBalance(Statement stmt, PrintWriter out) throws SQLException {
        out.println("\n📊 FINANCE RECORDS");
        out.println("-----------------------------");

        ResultSet rs = stmt.executeQuery("SELECT type, category, amount FROM transactions");

        while (rs.next()) {
            out.println(rs.getString(1) + " | " +
                        rs.getString(2) + " | ₹" +
                        rs.getDouble(3));
        }

        out.println("-----------------------------");

        double income = 0, expense = 0;

        rs = stmt.executeQuery("SELECT SUM(amount) FROM transactions WHERE type='Income'");
        if (rs.next()) income = rs.getDouble(1);

        rs = stmt.executeQuery("SELECT SUM(amount) FROM transactions WHERE type='Expense'");
        if (rs.next()) expense = rs.getDouble(1);

        double balance = income - expense;

        out.println("💰 Total Income: ₹" + income);
        out.println("💸 Total Expense: ₹" + expense);
        out.println("👛 Current Balance: ₹" + balance);
        out.println("-----------------------------");
        out.println("END");  // ✅ Client stops reading here
    }
}
