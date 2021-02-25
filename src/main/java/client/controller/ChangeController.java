package client.controller;

import client.NetworkChat;
import client.models.Network;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.awt.event.ActionEvent;
import java.io.IOException;

public class ChangeController {
    private NetworkChat networkChat;
    private Network network;
    @FXML
    private Label curUsername;

    @FXML
    private  TextField exchangeUsername;


    @FXML
    void sendNewName() throws IOException {
         String newName = exchangeUsername.getText();
         if(!newName.isBlank()){
             network.sendChangeUserName(network.getUsername(), newName);
             networkChat.closeScene(newName);
             Alert alert = new Alert(Alert.AlertType.INFORMATION);
             alert.setTitle("Изменение Ника");
             alert.setHeaderText("Изменение Имени Успешно");
             alert.setContentText("Ваше новое имя в чате " + newName);
             alert.show();
         } else {
            Alert errAlert = new Alert(Alert.AlertType.ERROR);
            errAlert.setTitle("Ошибка");
            errAlert.setHeaderText("Отправлено пустое значение");
            errAlert.setContentText("Введите новое имя повторно");
            errAlert.show();

         }

    }

    public void setNetworkChat(NetworkChat networkChat) {
        this.networkChat = networkChat;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public void setCurrentUsername(String username){
        curUsername.setText(username);
    }
}
