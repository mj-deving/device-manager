package com.mj.portfolio.client.controller;

import com.mj.portfolio.client.AppContext;
import com.mj.portfolio.client.service.ApiClient;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.ConnectException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField urlField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button connectBtn;

    private ApiClient apiClient;

    public void setApiClient(ApiClient client) {
        this.apiClient = client;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    @FXML
    private void onConnect() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isBlank()) {
            showError("Username is required.");
            return;
        }

        connectBtn.setDisable(true);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // Run the network test off the FX thread to keep the UI responsive
        Thread thread = new Thread(() -> {
            try {
                apiClient.setCredentials(username, password);
                var response = apiClient.get("/api/v1/devices?size=1");

                javafx.application.Platform.runLater(() -> {
                    connectBtn.setDisable(false);
                    if (response.statusCode() == 200) {
                        AppContext.setApiClient(apiClient);
                        closeStage();
                    } else if (response.statusCode() == 401) {
                        showError("Invalid credentials. Please try again.");
                    } else {
                        showError("Server returned HTTP " + response.statusCode());
                    }
                });
            } catch (ConnectException e) {
                javafx.application.Platform.runLater(() -> {
                    connectBtn.setDisable(false);
                    showError("Cannot reach server: " + apiClient.getBaseUrl());
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    connectBtn.setDisable(false);
                    showError("Connection error: " + e.getMessage());
                });
            }
        }, "login-check");
        thread.setDaemon(true);
        thread.start();
    }

    private void closeStage() {
        Stage stage = (Stage) connectBtn.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
