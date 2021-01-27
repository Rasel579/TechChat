package client.controller;

import client.models.Network;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.*;

import java.io.IOException;


public class ViewController {
    private Network network;
    @FXML
    private TextField userMessage;

    @FXML
    private Button sendButton;

    @FXML
    private ListView<String> messagesListView = new ListView<String>();



    public void setNetwork(Network network){
        this.network = network;
    }


    @FXML
    void sendMessage(){
      String message = userMessage.getText();
      if(!message.isBlank()){
          try {
              network.getOut().writeUTF(message);
          } catch (IOException e) {
              System.out.println("Ошибка при отправке сообщения");
              e.printStackTrace();
          }
      }
      userMessage.clear();
    }

    @FXML
    void onAbout(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Lesson 4");
        alert.setContentText("My Chat");
        alert.show();

    }

    @FXML
    public  void  initialize(){
        sendButton.setOnAction(event -> ViewController.this.sendMessage());
        userMessage.setOnAction(event -> ViewController.this.sendMessage());
    }


    public void appendMessage(String s) {
        messagesListView.getItems().add(s);
    }
}
