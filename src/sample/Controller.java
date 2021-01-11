package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.*;


public class Controller {
    @FXML
    private TextField userMessage;

    @FXML
    private Button sendButton;

    @FXML
    private ListView<String> messagesListView = new ListView<String>();



    @FXML
    void sendMessage(){
      String message = userMessage.getText();
      if(!message.isBlank()){
          messagesListView.getItems().add(message);
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


}
