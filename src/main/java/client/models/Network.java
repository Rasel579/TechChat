package client.models;

import client.NetworkChat;
import client.controller.ChatController;
import javafx.application.Platform;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Network {
    private static  final int DEFAULT_SERVER_PORT = 8887;
    private  static final String DEFAULT_SERVER_HOST = "localhost";
    private  final int port;
    private  final String host;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private ChatController chatController;

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
                            chatController.appendMessage(String.format("%s:  %s", sender, messageFromUser));
                        });

                    } else if (message.startsWith(SERVER_MESSAGE_CMD_PREFIX)) {
                        String[] parts = message.split("\\s+", 3);
                        String username = parts[1];
                        String messageFromUser = parts[2];
                        Platform.runLater(() -> {
                            chatController.appendMessage(messageFromUser + ": " + username);
                        });
                    } else if(message.startsWith(PRIVATE_MESSAGE_CMD_PREFIX)){
                        String[] parts = message.split("\\s+", 3);
                        String recipient = parts[1];
                        String privateMessage = parts[2];
                        Platform.runLater(() -> {
                            chatController.appendMessage(String.format("%s:  %s", recipient, privateMessage));
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
                        for (String usersTestData : NetworkChat.USERS_TEST_DATA) {
                            if(usersTestData.equals(oldUsername)){
                                NetworkChat.USERS_TEST_DATA.remove(oldUsername);
                                NetworkChat.USERS_TEST_DATA.add(newUsername);
                            }
                        }
                        Platform.runLater(() -> {
                            chatController.updateUsersList(NetworkChat.USERS_TEST_DATA);
                        });
                    }  else {
                        Platform.runLater(() -> {
                            System.out.println(message);
                            chatController.appendMessage("Неизвестная ошибка");
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
    }
}
