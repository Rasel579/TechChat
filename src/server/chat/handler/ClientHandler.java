package server.chat.handler;

import server.chat.MyServer;
import server.chat.auth.AuthService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {

    private final MyServer myServer;
    private final Socket clientSocket;
    private DataOutputStream out;
    private DataInputStream in;
    private String username;


    private static final String AUTH_CMD_PREFIX = "/auth"; // login + pass
    private static final String AUTHOK_CMD_PREFIX = "/authok"; // + username
    private static final String AUTHERR_CMD_PREFIX = "/autherr"; // + error
    private static final String CLIENT_MESSAGE_CMD_PREFIX = "/clientMsg"; // + message
    private static final String SERVER_MESSAGE_CMD_PREFIX = "/serverMsg"; // + message
    private static final String PRIVATE_MESSAGE_CMD_PREFIX = "/w"; // + sender + toUsername + msg
    private static final String END_CMD_PREFIX = "/end"; //

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
                readMessage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void authentication() throws IOException {
        while (true) {
//            String message = in.readUTF();
            String message = "/auth martin 1111";
            if (message.startsWith(AUTH_CMD_PREFIX)) {
                boolean isSuccessAuth = processAuthCommand(message);
                if (isSuccessAuth) {
                    break;
                }
            } else out.writeUTF(AUTHERR_CMD_PREFIX + " ошибка авторизации");
        }
    }

    private void readMessage() throws IOException {
        while (true) {
            String message = in.readUTF();
            System.out.println("message | " + username + " :" + message);
            if (message.startsWith(END_CMD_PREFIX)) {
                return;
            } else if (message.startsWith(PRIVATE_MESSAGE_CMD_PREFIX)) {
                  String[] parts = message.split("\\s+", 3);
                  String toUsername = parts[1];
                  String privateMessage = parts[2];
                  myServer.sendPrivateMsg(this, toUsername, privateMessage);
            } else myServer.broadCastMessage(message, this);
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
                return false;
            }
            out.writeUTF(AUTHOK_CMD_PREFIX + " " + username);
            myServer.subscribe(this);
            return true;
        } else {
            out.writeUTF(AUTHERR_CMD_PREFIX + "Логин или пароль не соответствуют");
            return false;
        }
    }

    public void sendMessage(String username, String message, String prefix) throws IOException {
        if(prefix.equals(PRIVATE_MESSAGE_CMD_PREFIX)){
            System.out.println(prefix + " Личное сообщение отправлено от " + username);
            out.writeUTF(String.format("%s %s %s", prefix,username, message));
        } else {
            String pref = prefix == CLIENT_MESSAGE_CMD_PREFIX ? CLIENT_MESSAGE_CMD_PREFIX : SERVER_MESSAGE_CMD_PREFIX;
            out.writeUTF(String.format("%s %s %s", pref, username, message));
        }
    }
}
