package server.chat.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.ServerApp;
import server.chat.MyServer;
import server.chat.auth.AuthService;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;

public class ClientHandler {

    private final MyServer myServer;
    private final Socket clientSocket;

    private DataOutputStream out;

    private DataInputStream in;
    public String username;



    private static final String AUTH_CMD_PREFIX = "/auth"; // login + pass

    private static final String AUTHOK_CMD_PREFIX = "/authok"; // + username
    private static final String AUTHERR_CMD_PREFIX = "/autherr"; // + error
    private static final String CLIENT_MESSAGE_CMD_PREFIX = "/clientMsg"; // + message
    private static final String SERVER_MESSAGE_CMD_PREFIX = "/serverMsg"; // + message
    private static final String PRIVATE_MESSAGE_CMD_PREFIX = "/w"; // + sender + toUsername + msg
    private  static final String USERLISTS_CMD_PREFIX = "/userlists";
    private static final String END_CMD_PREFIX = "/end"; //
    private  static final String CHANGE_CMD_PREFIX = "/change";
    private  static final String UPDATEUSERLIST_CMD_PREFIX = "/updateUserList";

    private final static Logger LOGGER = LogManager.getLogger(ClientHandler.class);

    public ClientHandler(MyServer myServer, Socket socket) {
        this.myServer = myServer;
        this.clientSocket = socket;
    }

    public String getUsername() {
        return username;
    }

    public void handle() throws IOException {
        out = new DataOutputStream(clientSocket.getOutputStream());
        in = new DataInputStream(clientSocket.getInputStream());
        new Thread(() -> {
            try {
                authentication();
                myServer.checkUserList();
                readMessage();
            } catch (IOException | SQLException e) {
                LOGGER.error(e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void authentication() throws IOException {
        while (true) {
            String message = in.readUTF();
            if (message.startsWith(AUTH_CMD_PREFIX)) {
                boolean isSuccessAuth = processAuthCommand(message);
                if (isSuccessAuth) {
                    break;
                }
            } else {
                out.writeUTF(AUTHERR_CMD_PREFIX + " ошибка авторизации");
            }
        }
    }

    private void readMessage() throws IOException, SQLException {
        while (true) {
            String message = in.readUTF();
            System.out.println("message | " + username + " :" + message);
            if (message.startsWith(END_CMD_PREFIX)) {
                String parts[] = message.split("\\s+", 2);
                String user = parts[1];
                myServer.unSubscribe(this);
                myServer.checkUserList();
                LOGGER.warn("отключение сервера");
            } else if (message.startsWith(PRIVATE_MESSAGE_CMD_PREFIX)) {
                  String[] parts = message.split("\\s+", 3);
                  String toUsername = parts[1];
                  String privateMessage = parts[2];
                  myServer.sendPrivateMsg(this, toUsername, privateMessage);
            } else if(message.startsWith(CHANGE_CMD_PREFIX)){
                String[] parts = message.split("\\s+");
                String username = parts[1];
                String newUsername = parts[2];
                myServer.changeUsername(this, username, newUsername);
                LOGGER.info("Пользователь" + username + "поменял имя " + newUsername);
            }  else myServer.broadCastMessage(message, this);
        }
    }

    private boolean processAuthCommand(String message) throws IOException {
        String[] parts = message.split("\\s+", 3); // разделяет слообщение по пробелам с табуляцией
        String login = parts[1];
        String password = parts[2];
        System.out.println(login + " " + password);
        AuthService authService = myServer.getAuthservice();
        username = authService.getUsernameByLoginAndPassword(login, password);
        if (username != null) {
            if (myServer.isUsernameBusy(username)) {
                out.writeUTF(AUTHERR_CMD_PREFIX + " этот никнейм занят");
                LOGGER.info(username + " попытка входа");
                return false;
            }
            out.writeUTF(AUTHOK_CMD_PREFIX + " " + username);
            myServer.subscribe(this);
            LOGGER.info("успешная авторизация " + username);
            return true;
        } else {
            out.writeUTF(AUTHERR_CMD_PREFIX + "Логин или пароль не соответствуют");
            LOGGER.info(AUTHERR_CMD_PREFIX + " Логин или пароль не соответствуют");
            return false;
        }
    }

    public void sendMessage(String username, String message, String prefix) throws IOException {
        if(prefix.equals(PRIVATE_MESSAGE_CMD_PREFIX)){
            System.out.println(prefix + " Личное сообщение отправлено от " + username);
            out.writeUTF(String.format("%s %s %s", prefix, username, message));
            LOGGER.info(username + " выслал личное сообщение");
        } else {
            String pref = prefix.equals(CLIENT_MESSAGE_CMD_PREFIX)  ? CLIENT_MESSAGE_CMD_PREFIX : SERVER_MESSAGE_CMD_PREFIX;
            out.writeUTF(String.format("%s %s %s", pref, username, message));
            LOGGER.info(username + " выслал сообщение в чат");
        }
    }


    public void sendUser(String username) throws IOException {
        out.writeUTF(String.format("%s %s", USERLISTS_CMD_PREFIX, username));
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void sendUpdateList(String username, String newUsername) throws IOException {
        out.writeUTF(String.format("%s %s %s",UPDATEUSERLIST_CMD_PREFIX, username, newUsername));
    }
}
