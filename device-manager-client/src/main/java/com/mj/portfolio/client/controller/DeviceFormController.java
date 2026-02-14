package com.mj.portfolio.client.controller;

import com.mj.portfolio.client.model.Device;
import com.mj.portfolio.client.model.DeviceStatus;
import com.mj.portfolio.client.model.DeviceType;
import com.mj.portfolio.client.service.DeviceApiService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class DeviceFormController implements Initializable {

    @FXML private TextField nameField;
    @FXML private ComboBox<DeviceType> typeCombo;
    @FXML private ComboBox<DeviceStatus> statusCombo;
    @FXML private TextField ipField;
    @FXML private TextField locationField;
    @FXML private Label errorLabel;
    @FXML private Button saveButton;

    private Device device;           // null = create mode, non-null = edit mode
    private DeviceApiService apiService;
    private Runnable onSaved;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        typeCombo.getItems().addAll(DeviceType.values());
        statusCombo.getItems().addAll(DeviceStatus.values());
        statusCombo.setValue(DeviceStatus.ACTIVE);
        errorLabel.setVisible(false);
    }

    public void setDevice(Device device) {
        this.device = device;
        if (device != null) {
            nameField.setText(device.getName());
            typeCombo.setValue(device.getType());
            statusCombo.setValue(device.getStatus());
            ipField.setText(device.getIpAddress() != null ? device.getIpAddress() : "");
            locationField.setText(device.getLocation() != null ? device.getLocation() : "");
        }
    }

    public void setApiService(DeviceApiService apiService) { this.apiService = apiService; }
    public void setOnSaved(Runnable onSaved)               { this.onSaved = onSaved; }

    @FXML
    private void onSave() {
        errorLabel.setVisible(false);
        String name = nameField.getText().trim();
        if (name.isEmpty() || typeCombo.getValue() == null) {
            showError("Name and Type are required.");
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("type", typeCombo.getValue().name());
        body.put("status", statusCombo.getValue() != null ? statusCombo.getValue().name() : "ACTIVE");
        body.put("ipAddress", ipField.getText().trim());
        body.put("location", locationField.getText().trim());

        saveButton.setDisable(true);

        if (device == null) {
            apiService.create(body).whenComplete((d, ex) -> Platform.runLater(() -> {
                saveButton.setDisable(false);
                if (ex != null) {
                    showError("Error: " + ex.getMessage());
                } else {
                    if (onSaved != null) onSaved.run();
                    nameField.getScene().getWindow().hide();
                }
            }));
        } else {
            apiService.update(device.getId(), body).whenComplete((d, ex) -> Platform.runLater(() -> {
                saveButton.setDisable(false);
                if (ex != null) {
                    showError("Error: " + ex.getMessage());
                } else {
                    if (onSaved != null) onSaved.run();
                    nameField.getScene().getWindow().hide();
                }
            }));
        }
    }

    @FXML
    private void onCancel() {
        nameField.getScene().getWindow().hide();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}
