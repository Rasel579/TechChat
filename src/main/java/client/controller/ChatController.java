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

import java.io.*;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ChatController {
    private Network network;
    private FileReader logger;
    private BufferedReader reader;
    private FileWriter writer;

    @FXML
    private TextField userMessage;
    private  NetworkChat networkChat;

    @FXML
    private Button sendButton;

    @FXML
    private ListView<String> messagesListView = new ListView<String>();

    @FXML
    private ListView<String> userListView = new ListView<>();

    @FXML
    private Label usernameTitle;

    private String selectedRecipient;
    public ChatController() {
    }


    public void setNetwork(Network network){
        this.network = network;
    }


    @FXML
    void sendMessage() throws IOException {
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
    void onChange(ActionEvent actionEvent) throws IOException {
        this.networkChat.openChangesUsername();
    }

    @FXML
    public  void  initialize() throws IOException {
        userListView.setItems(FXCollections.observableArrayList(NetworkChat.USERS_TEST_DATA));
        sendButton.setOnAction(event -> {
            try {
                ChatController.this.sendMessage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        userMessage.setOnAction(event -> {
            try {
                ChatController.this.sendMessage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
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




    public void appendMessage(String s) throws IOException {
        String timeStamp = DateFormat.getTimeInstance().format(new Date());
        messagesListView.getItems().add(timeStamp);
        messagesListView.getItems().add(s);
        network.writeLog(timeStamp, s);
    }

    public void setUsernameTitle(String username) {
        this.usernameTitle.setText(username);
    }

    public void updateUsersList(List<String> username) {
        userListView.setItems(FXCollections.observableList(username));
    }

    public void setNetworkChat(NetworkChat networkChat) {
        this.networkChat = networkChat;
    }

    public ListView<String> getMessagesListView() {
        return messagesListView;
    }
}
