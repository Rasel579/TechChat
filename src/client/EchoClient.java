    package client;

    import client.controller.ViewController;
    import client.models.Network;
    import javafx.application.Application;
    import javafx.fxml.FXMLLoader;
    import javafx.scene.Parent;
    import javafx.scene.Scene;
    import javafx.stage.Stage;

    public class EchoClient extends Application {

        @Override
        public void start(Stage primaryStage) throws Exception{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(EchoClient.class.getResource("view/chat-view.fxml"));
            Parent root = loader.load();
            primaryStage.setTitle("TechChat");
            primaryStage.setScene(new Scene(root));
            primaryStage.show();

            Network network = new Network();
            network.connect();
            ViewController viewController = loader.getController();
            viewController.setNetwork(network);
            network.waitMessage(viewController);
        }



        public static void main(String[] args) {
            launch(args);
        }
    }
