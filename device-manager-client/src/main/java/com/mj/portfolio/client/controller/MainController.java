package com.mj.portfolio.client.controller;

import com.mj.portfolio.client.model.Device;
import com.mj.portfolio.client.model.DeviceStatus;
import com.mj.portfolio.client.model.DeviceType;
import com.mj.portfolio.client.service.ApiClient;
import com.mj.portfolio.client.service.DeviceApiService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private TableView<Device> deviceTable;
    @FXML private TableColumn<Device, String> colName;
    @FXML private TableColumn<Device, DeviceType> colType;
    @FXML private TableColumn<Device, DeviceStatus> colStatus;
    @FXML private TableColumn<Device, String> colIp;
    @FXML private TableColumn<Device, String> colLocation;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Label statusBar;
    @FXML private Label connectionLabel;

    private final DeviceApiService apiService = new DeviceApiService(new ApiClient());
    private final ObservableList<Device> devices = FXCollections.observableArrayList();
    private FilteredList<Device> filteredDevices;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colIp.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));

        filteredDevices = new FilteredList<>(devices, p -> true);
        deviceTable.setItems(filteredDevices);

        // Status filter ComboBox
        statusFilter.setItems(FXCollections.observableArrayList(
                "ALL", "ACTIVE", "INACTIVE", "MAINTENANCE", "DECOMMISSIONED"));
        statusFilter.setValue("ALL");
        statusFilter.setOnAction(e -> applyFilters());

        // Search
        searchField.textProperty().addListener((obs, old, val) -> applyFilters());

        // Double-click opens detail
        deviceTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && deviceTable.getSelectionModel().getSelectedItem() != null) {
                openDetail(deviceTable.getSelectionModel().getSelectedItem());
            }
        });

        loadDevices();
    }

    private void applyFilters() {
        String query = searchField.getText().toLowerCase();
        String statusVal = statusFilter.getValue();
        filteredDevices.setPredicate(d -> {
            boolean matchesSearch = query.isBlank()
                    || d.getName().toLowerCase().contains(query)
                    || (d.getIpAddress() != null && d.getIpAddress().contains(query))
                    || (d.getLocation() != null && d.getLocation().toLowerCase().contains(query));
            boolean matchesStatus = "ALL".equals(statusVal)
                    || (d.getStatus() != null && d.getStatus().name().equals(statusVal));
            return matchesSearch && matchesStatus;
        });
        updateStatusBar();
    }

    @FXML
    private void onRefresh() {
        loadDevices();
    }

    @FXML
    private void onNewDevice() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/device-form.fxml"));
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("New Device");
        dialog.setScene(new Scene(loader.load(), 400, 320));
        DeviceFormController ctrl = loader.getController();
        ctrl.setApiService(apiService);
        ctrl.setOnSaved(this::loadDevices);
        dialog.showAndWait();
    }

    private void openDetail(Device device) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/device-detail.fxml"));
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Device: " + device.getName());
            dialog.setScene(new Scene(loader.load(), 480, 380));
            DeviceDetailController ctrl = loader.getController();
            ctrl.setDevice(device);
            ctrl.setApiService(apiService);
            ctrl.setOnChanged(this::loadDevices);
            dialog.showAndWait();
        } catch (IOException e) {
            showError("Failed to open device detail", e.getMessage());
        }
    }

    private void loadDevices() {
        connectionLabel.setText("Connecting...");
        connectionLabel.setStyle("-fx-text-fill: orange;");

        Task<List<Device>> task = new Task<>() {
            @Override
            protected List<Device> call() throws Exception {
                return apiService.getAll().get();
            }
        };

        task.setOnSucceeded(e -> {
            devices.setAll(task.getValue());
            applyFilters();
            connectionLabel.setText("Connected");
            connectionLabel.setStyle("-fx-text-fill: #4caf50;");
            updateStatusBar();
        });

        task.setOnFailed(e -> {
            connectionLabel.setText("Disconnected");
            connectionLabel.setStyle("-fx-text-fill: #f44336;");
            statusBar.setText("Error loading devices: " + task.getException().getMessage());
        });

        new Thread(task, "device-loader").start();
    }

    private void updateStatusBar() {
        statusBar.setText("Showing " + filteredDevices.size() + " of " + devices.size() + " devices");
    }

    private void showError(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }
}
