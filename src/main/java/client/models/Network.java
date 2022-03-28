package client.models;

import client.NetworkChat;
import client.controller.ChatController;
import javafx.application.Platform;
import javafx.collections.FXCollections;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Network {
    private static  final int DEFAULT_SERVER_PORT = 8887;
    private  static final String DEFAULT_SERVER_HOST = "localhost";
    private  final int port;
    private  final String host;
    private final int limitMsg = 200;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private ChatController chatController;
    private FileReader logger;
    private BufferedReader reader;
    private BufferedWriter writer;
    private List<String> messages = new ArrayList<>();

    private String username;


    public String getUsername() {
        return username;
    }

    private  static final String AUTH_CMD_PREFIX = "/auth";

    private  static final String AUTHOK_CMD_PREFIX = "/authok";
    private  static final String AUTHERR_CMD_PREFIX = "/autherr";
    private  static final String CLIENT_MESSAGE_CMD_PREFIX = "/clientMsg";
    private  static final String SERVER_MESSAGE_CMD_PREFIX = "/serverMsg";
    private  static final String PRIVATE_MESSAGE_CMD_PREFIX = "/w";
    private  static final String USERLISTS_CMD_PREFIX = "/userlists";
    private  static final String END_CMD_PREFIX = "/end";
    private  static final String CHANGE_CMD_PREFIX = "/change";
    private  static final String UPDATEUSERLIST_CMD_PREFIX = "/updateUserList";
    public Network(int port, String host) {
        this.port = port;
        this.host = host;
    }

    public Network(){
        this.port =DEFAULT_SERVER_PORT;
        this.host = DEFAULT_SERVER_HOST;
    }

    public void connect(){
        try {
            socket = new Socket(host, port);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        }catch (IOException e){
            System.out.println("Ошибка подключения");
        }
    }

    public DataOutputStream getOut() {
        return out;
    }

    public void  waitMessage(ChatController chatController){
        long startTime = System.currentTimeMillis();
        Thread thread = new Thread(()->{
            while (true) {
                try {
                    String message = in.readUTF();
                    System.out.println(message);

                    if (message.startsWith(CLIENT_MESSAGE_CMD_PREFIX)) {
                        String[] parts = message.split("\\s+", 3);
                        String sender = parts[1];
                        String messageFromUser = parts[2];
                        Platform.runLater(() -> {
                            try {
                                chatController.appendMessage(String.format("%s:  %s", sender, messageFromUser));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });

                    } else if (message.startsWith(SERVER_MESSAGE_CMD_PREFIX)) {
                        String[] parts = message.split("\\s+", 3);
                        String username = parts[1];
                        String messageFromUser = parts[2];
                        Platform.runLater(() -> {
                            try {
                                chatController.appendMessage(messageFromUser + ": " + username);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    } else if(message.startsWith(PRIVATE_MESSAGE_CMD_PREFIX)){
                        String[] parts = message.split("\\s+", 3);
                        String recipient = parts[1];
                        String privateMessage = parts[2];
                        Platform.runLater(() -> {
                            try {
                                chatController.appendMessage(String.format("%s:  %s", recipient, privateMessage));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });

                    } else if (message.startsWith(USERLISTS_CMD_PREFIX)){
                          String[] parts = message.split("\\s+", 2);
                          String user = parts[1];
                          Platform.runLater(() -> {
                              if (!NetworkChat.USERS_TEST_DATA.contains(user)) {
                                  NetworkChat.USERS_TEST_DATA.add(user);
                                  chatController.updateUsersList(NetworkChat.USERS_TEST_DATA);
                              }
                          });
                    } else if(message.startsWith(UPDATEUSERLIST_CMD_PREFIX)){
                        String[] parts = message.split("\\s+", 3);
                        String oldUsername = parts[1];
                        String newUsername = parts[2];
                        Platform.runLater(() -> {
                            for (String usersTestData : NetworkChat.USERS_TEST_DATA) {
                                if(usersTestData.equals(oldUsername)){
                                    NetworkChat.USERS_TEST_DATA.remove(oldUsername);
                                    NetworkChat.USERS_TEST_DATA.add(newUsername);
                                }
                            }
                        });
                        Platform.runLater(() -> {
                            chatController.updateUsersList(NetworkChat.USERS_TEST_DATA);
                        });
                    }  else {
                        Platform.runLater(() -> {
                            System.out.println(message);
                            try {
                                chatController.appendMessage("Неизвестная ошибка");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }});
        thread.setDaemon(true);
        thread.start();
        System.out.println(System.currentTimeMillis() - startTime);
    }

    private void timeOut(Thread thread) throws IOException {
        long startTime = System.currentTimeMillis();
        System.out.println(System.currentTimeMillis() - startTime);
        if(System.currentTimeMillis() - startTime > 20000){
            this.sendMessage(String.format("%s %s", END_CMD_PREFIX, username));
            socket.close();
        }
    }


    public String sendAuthCommand(String login, String password, long startTime) {
        try {

            out.writeUTF(String.format("%s %s %s", AUTH_CMD_PREFIX, login, password));
            String response = in.readUTF();
            System.out.println(response);
            if(response.startsWith(AUTHOK_CMD_PREFIX)){
                 this.username = response.split("\\s+", 2)[1];
                 System.out.println(username);
                 writer = new BufferedWriter(new FileWriter(String.format("src/main/resources/logger/history_[%s].txt", username), true));
                 logger = new FileReader(String.format("src/main/resources/logger/history_[%s].txt", username));
                 reader = new BufferedReader(logger);
                 String str;
                 int count = 0;
                 while ((str = reader.readLine()) != null){
                     if(messages.size() < limitMsg || count > (messages.size() - limitMsg)){
                         messages.add(str);
                     }
                    count++;
                    System.out.println(str);
                 }
                 return null;
            }else {
                System.out.println(System.currentTimeMillis() - startTime);
                if (System.currentTimeMillis() - startTime > 120000){
                    socket.close();
                }

                 return  response.split("\\s+", 2)[1];
            }
        } catch (IOException e) {
            e.printStackTrace();
            return  e.getMessage();
        }
    }

    public void sendMessage(String message) throws IOException {
        out.writeUTF(message);
    }

    public void sendPrivateMessage(String message, String recipient) throws IOException {
        String command = String.format("%s %s %s", PRIVATE_MESSAGE_CMD_PREFIX, recipient, message);
        sendMessage(command);
    }
    public void sendChangeUserName(String username, String newName) throws IOException {
        String change = String.format("%s %s %s", CHANGE_CMD_PREFIX, username, newName);
        sendMessage(change);
        this.username = newName;
        writer = new BufferedWriter(new FileWriter(String.format("src/main/resources/logger/history_[%s].txt", newName), true));
        String str;
        while ((str = reader.readLine() )!= null){
            writer.flush();
            writer.write(String.format("\n %s", str));
        }
    }

    public void writeLog(String timeStamp, String s) throws IOException {
        System.out.println(writer);
        writer.flush();
        writer.write(String.format("\n %s \n %s", timeStamp, s));
    }

    public List<String> getMessages() {
        return messages;
    }
}
