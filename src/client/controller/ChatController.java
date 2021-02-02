package client.controller;

import client.NetworkChat;
import client.models.Network;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ChatController {
    private Network network;
    @FXML
    private TextField userMessage;

    @FXML
    private Button sendButton;

    @FXML
    private ListView<String> messagesListView = new ListView<String>();

    @FXML
    private ListView<String> userListView = new ListView<>();
    @FXML
    private Label usernameTitle;
    private String selectedRecipient;


    public void setNetwork(Network network){
        this.network = network;
    }


    @FXML
    void sendMessage(){
      String message = userMessage.getText();
      if(!message.isBlank()){
          appendMessage("Я " + message);
          try {
              if(selectedRecipient != null){
                  network.sendPrivateMessage(message, selectedRecipient);
              }else {
                  network.sendMessage(message);
              }
          } catch (IOException e) {
              System.out.println("Ошибка при отправке сообщения");
              e.printStackTrace();
          }
          userMessage.clear();
      }
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
        userListView.setItems(FXCollections.observableArrayList(NetworkChat.USERS_TEST_DATA));
        sendButton.setOnAction(event -> ChatController.this.sendMessage());
        userMessage.setOnAction(event -> ChatController.this.sendMessage());
        userListView.setCellFactory(lv -> {
            MultipleSelectionModel <String> selectionModel = userListView.getSelectionModel();
            ListCell <String> listCell = new ListCell<>();
            listCell.textProperty().bind(listCell.itemProperty());
            listCell.addEventFilter(MouseEvent.MOUSE_PRESSED,
                    event -> {
                        userListView.requestFocus();
                        if(!listCell.isEmpty()){
                            int index = listCell.getIndex();
                            if(selectionModel.getSelectedIndices().contains(index)){
                                selectionModel.clearSelection(index);
                                selectedRecipient = null;
                            }else {
                                selectionModel.select(index);
                                selectedRecipient = listCell.getItem();
                            }
                            event.consume();
                        }
                    });
            return listCell;
        });
    }




    public void appendMessage(String s) {
        String timeStamp = DateFormat.getTimeInstance().format(new Date());
        messagesListView.getItems().add(timeStamp);
        messagesListView.getItems().add(s);
    }

    public void setUsernameTitle(String username) {
        this.usernameTitle.setText(username);
    }

    public void updateUsersList(List<String> username) {
        userListView.setItems(FXCollections.observableList(username));
    }
}
