package com.mj.portfolio.client;

import com.mj.portfolio.client.controller.LoginController;
import com.mj.portfolio.client.service.ApiClient;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DeviceManagerClientApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        ApiClient client = new ApiClient();

        // Show login dialog first; blocks until closed
        FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Stage loginStage = new Stage();
        loginStage.setTitle("Connect to Device Manager");
        loginStage.setScene(new Scene(loginLoader.load()));
        loginStage.setResizable(false);
        LoginController loginCtrl = loginLoader.getController();
        loginCtrl.setApiClient(client);
        loginStage.showAndWait();

        // If login was cancelled / window closed without connecting, exit
        if (AppContext.getApiClient() == null) {
            javafx.application.Platform.exit();
            return;
        }

        // Login succeeded — show main window
        FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Scene scene = new Scene(mainLoader.load(), 900, 600);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        primaryStage.setTitle("Device Manager");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(700);
        primaryStage.setMinHeight(400);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
