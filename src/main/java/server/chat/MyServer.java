package server.chat;

import server.chat.auth.BaseAuth;
import server.chat.handler.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MyServer {
    private final ServerSocket serverSocket;
    private final BaseAuth authservice;
    private final List<ClientHandler> clients = new ArrayList<>();

    public MyServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.authservice = new BaseAuth();
    }

    public BaseAuth getAuthservice() {
        return authservice;
    }

    public void start()  {
        System.out.println("Cервер запущен");
        try {
            while (true){
                waitAndProcessNewClientConnection();
            }
             } catch (IOException e) {
                e.printStackTrace();
            }
        }

    private void waitAndProcessNewClientConnection() throws IOException {
        System.out.println("Ожидание пользователя");
        Socket socket = serverSocket.accept();
        System.out.println("Клиент подключился");
        processClientConnection(socket);
    }

    private void processClientConnection(Socket socket) throws IOException {
        ClientHandler clientHandler = new ClientHandler(this, socket);
        clientHandler.handle();
    }

    public synchronized void subscribe(ClientHandler clientHandler) throws IOException {
        clients.add(clientHandler);
        for (ClientHandler client : clients) {
            if (client == clientHandler){
                continue;
            }
            String prefix = "/serverMsg";
            client.sendMessage(clientHandler.getUsername(), "Присоединился к чату", prefix);
        }
        System.out.println(clientHandler.getUsername());
    }


    public synchronized  void unSubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
    }

    public synchronized boolean isUsernameBusy(String username){
        for (ClientHandler client : clients) {
            if(client.getUsername().equals(username)){
                return true;
            }
        }
        return false;
    };

    public synchronized  void broadCastMessage(String message, ClientHandler clientHandler) throws IOException {
        for (ClientHandler client : clients) {
            if(client == clientHandler){
                continue;
            }
            String prefix = "/clientMsg";
           client.sendMessage(clientHandler.getUsername(), message, prefix);
        }
    }

    public synchronized void sendPrivateMsg(ClientHandler clientHandler, String toUsername, String privateMessage) throws IOException {
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(toUsername)){
                String prefix = "/w";
                client.sendMessage(clientHandler.getUsername(), privateMessage, prefix);
            }
        }
    }

    public void checkUserList() throws IOException {
        for (ClientHandler client : clients) {
            for (int i = 0; i < clients.size(); i++) {
                client.sendUser(clients.get(i).getUsername());
            }
            }
        }

    }


