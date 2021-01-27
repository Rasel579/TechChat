package client.models;

import client.controller.ViewController;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Network {
    private static  final int DEFAULT_SERVER_PORT = 8888;
    private  static final String DEFAULT_SERVER_HOST = "localhost";
    private  final int port;
    private  final String host;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

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

    public void  waitMessage(ViewController viewController){
        Thread thread = new Thread(()->{
            try {
                String message = in.readUTF();
                viewController.appendMessage("Я: " + message);
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
        thread.setDaemon(true);
        thread.start();
    }


}
