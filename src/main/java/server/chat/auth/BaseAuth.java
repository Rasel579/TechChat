package server.chat.auth;
import server.chat.User;
import java.sql.*;
import java.util.*;

public class BaseAuth implements AuthService{
    private static Connection connection;
    private static Statement stmt;
    private static ResultSet rs;
    private static List<User> clients = new ArrayList<>();

    public void connection() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:src/main/resources/db/users.db");
        stmt = connection.createStatement();
    }

    public void selectAllUsers() throws SQLException {
        rs = stmt.executeQuery("SELECT * FROM users");
        while (rs.next()){
            clients.add(new User (rs.getString("login"), rs.getString("password"),rs.getString("username")));
        }
    }
    public static void updateAuth(String username, String newUsername) throws SQLException {
        stmt.executeUpdate(String.format("UPDATE users SET username = '%s' WHERE username = '%s'", newUsername, username));
        for (User client : clients) {
           if (client.getUsername().equals(username)){
               client.setUsername(newUsername);
           }
        }
    }


    @Override
    public String getUsernameByLoginAndPassword(String login, String password) {
        System.out.println("Старт аутенфикации");
        for (User client : clients) {
            if (client.getLogin().equals(login) && client.getPassword().equals(password)){
                return client.getUsername();
            }
        }
        return null;
    }

    @Override
    public void startAuthentication() {

    }

    @Override
    public void endAuthentication() {
        System.out.println("Окончание аутенфикации");
    }
}
