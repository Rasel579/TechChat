    package client;

    import client.controller.AuthViewController;
    import client.controller.ChatController;
    import client.controller.ChangeController;
    import client.models.Network;
    import javafx.application.Application;
    import javafx.fxml.FXMLLoader;
    import javafx.scene.Parent;
    import javafx.scene.Scene;
    import javafx.scene.control.Alert;
    import javafx.stage.Modality;
    import javafx.stage.Stage;
    import java.io.IOException;
    import java.util.ArrayList;
    import java.util.List;

    public class NetworkChat extends Application {
        public static  List<String> USERS_TEST_DATA = new ArrayList<>();
        private Network network;
        private Stage primaryStage;
        private Stage authStage;
        private Stage changeLoaderStage;
        private ChatController chatController;
        private ChangeController changeController;

        @Override
        public void start(Stage primaryStage) throws Exception{
            this.primaryStage = primaryStage;
            network = new Network();
            network.connect();
            openAuthWindow();
            createClientWindow();
            createChangesUsername();
        }

        private void openAuthWindow() throws IOException {
            FXMLLoader authLoader = new FXMLLoader();
            authLoader.setLocation(NetworkChat.class.getResource("/fxml/auth-view.fxml"));
            Parent root = authLoader.load();
            authStage = new Stage();
            authStage.setTitle("Authentication");
            authStage.setScene(new Scene(root));
            authStage.initModality(Modality.WINDOW_MODAL);
            authStage.initOwner(primaryStage);
            authStage.setX(500);
            //authStage.setY(2200);
            authStage.show();
            AuthViewController authLoaderController = authLoader.getController();
            authLoaderController.setNetwork(network);
            authLoaderController.setNetworkChat(this);
        }

        private void createClientWindow() throws IOException {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(NetworkChat.class.getResource("/fxml/chat-view.fxml"));
            Parent root = loader.load();
            primaryStage.setTitle("TechChat");
            primaryStage.setScene(new Scene(root));
            primaryStage.setX(500);
            //primaryStage.setY(1400);
            chatController = loader.getController();
            chatController.setNetwork(network);
            chatController.setNetworkChat(this);
        }


        public static void main(String[] args) {
            launch(args);
        }

        public void openChat() {
            authStage.close();
            primaryStage.show();
            primaryStage.setTitle(network.getUsername());
            network.waitMessage(chatController);
            chatController.setUsernameTitle(network.getUsername());

        }

        public void createChangesUsername() throws IOException {
            FXMLLoader changeLoader = new FXMLLoader();
            changeLoader.setLocation(NetworkChat.class.getResource("/fxml/changer-view.fxml"));
            Parent root = changeLoader.load();
            changeLoaderStage = new Stage();
            changeLoaderStage.setTitle("Change Name");
            changeLoaderStage.setScene(new Scene(root));
            changeLoaderStage.setX(670);
            //authStage.setY(2200);
            changeController = changeLoader.getController();
            changeController.setNetworkChat(this);
            changeController.setNetwork(network);
        }

        public void openChangesUsername() {
            changeLoaderStage.show();
            changeController.setCurrentUsername(network.getUsername());
        }

        public void closeScene(String newName) {
            changeLoaderStage.close();
            chatController.setUsernameTitle(network.getUsername());
            primaryStage.setTitle(newName);
        }
    }
