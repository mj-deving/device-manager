package com.mj.portfolio.client.controller;

import com.mj.portfolio.client.model.Device;
import com.mj.portfolio.client.service.DeviceApiService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class DeviceDetailController {

    @FXML private Label lblName;
    @FXML private Label lblType;
    @FXML private Label lblStatus;
    @FXML private Label lblIp;
    @FXML private Label lblLocation;
    @FXML private Label lblCreatedAt;
    @FXML private Label lblUpdatedAt;

    private Device device;
    private DeviceApiService apiService;
    private Runnable onChanged;

    public void setDevice(Device device) {
        this.device = device;
        lblName.setText(device.getName());
        lblType.setText(device.getType() != null ? device.getType().name() : "-");
        lblStatus.setText(device.getStatus() != null ? device.getStatus().name() : "-");
        lblIp.setText(device.getIpAddress() != null ? device.getIpAddress() : "-");
        lblLocation.setText(device.getLocation() != null ? device.getLocation() : "-");
        lblCreatedAt.setText(device.getCreatedAt() != null ? device.getCreatedAt() : "-");
        lblUpdatedAt.setText(device.getUpdatedAt() != null ? device.getUpdatedAt() : "-");
    }

    public void setApiService(DeviceApiService apiService) { this.apiService = apiService; }
    public void setOnChanged(Runnable onChanged)           { this.onChanged = onChanged; }

    @FXML
    private void onEdit() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/device-form.fxml"));
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Edit Device");
        dialog.setScene(new Scene(loader.load(), 400, 320));
        DeviceFormController ctrl = loader.getController();
        ctrl.setDevice(device);
        ctrl.setApiService(apiService);
        ctrl.setOnSaved(() -> {
            if (onChanged != null) onChanged.run();
            // Close detail dialog
            lblName.getScene().getWindow().hide();
        });
        dialog.showAndWait();
    }

    @FXML
    private void onDelete() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete device '" + device.getName() + "'?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                apiService.delete(device.getId()).thenRun(() -> {
                    javafx.application.Platform.runLater(() -> {
                        if (onChanged != null) onChanged.run();
                        lblName.getScene().getWindow().hide();
                    });
                });
            }
        });
    }
}
