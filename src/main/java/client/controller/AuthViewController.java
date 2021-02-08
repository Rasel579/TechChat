package client.controller;

import client.NetworkChat;
import client.models.Network;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.io.IOException;

public class AuthViewController {
    long startTime = System.currentTimeMillis();
    @FXML
    private TextField loginField;

    @FXML
    private  TextField passwordField;
    private Network network;
    private NetworkChat networkChat;

    @FXML
    public  void  checkAuth(ActionEvent actionEvent) throws IOException {
        String login = loginField.getText();
        String password = passwordField.getText();
        if(!login.isBlank() && !password.isBlank()){
            String authErrorMsg = network.sendAuthCommand(login, password , startTime);
            if(authErrorMsg == null){
                // открыть чат
                networkChat.openChat();
            } else {
                System.out.println("!!!Ошибка аутенфикации");
            }
        }

    }

    public void setNetwork(Network network) {
        this.network = network;
        
    }

    public void setNetworkChat(NetworkChat networkChat) {
        this.networkChat = networkChat;
    }
}
