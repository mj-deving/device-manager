package com.mj.portfolio.client.controller;

import com.mj.portfolio.client.AppContext;
import com.mj.portfolio.client.model.Device;
import com.mj.portfolio.client.model.DeviceStatus;
import com.mj.portfolio.client.model.DeviceType;
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
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
    @FXML private HBox offlineBanner;

    private final DeviceApiService apiService = new DeviceApiService(AppContext.getApiClient());
    private final ObservableList<Device> devices = FXCollections.observableArrayList();
    private FilteredList<Device> filteredDevices;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(DeviceStatus status, boolean empty) {
                super.updateItem(status, empty);
                getStyleClass().removeAll(
                        "status-active", "status-inactive", "status-maintenance", "status-other");
                if (empty || status == null) { setText(null); return; }
                setText(status.name());
                switch (status) {
                    case ACTIVE        -> getStyleClass().add("status-active");
                    case INACTIVE      -> getStyleClass().add("status-inactive");
                    case MAINTENANCE   -> getStyleClass().add("status-maintenance");
                    default            -> getStyleClass().add("status-other");
                }
            }
        });
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
            offlineBanner.setVisible(false);
            offlineBanner.setManaged(false);
            updateStatusBar();
        });

        task.setOnFailed(e -> {
            connectionLabel.setText("Disconnected");
            connectionLabel.setStyle("-fx-text-fill: #f44336;");
            offlineBanner.setVisible(true);
            offlineBanner.setManaged(true);
            statusBar.setText("Error loading devices: " + task.getException().getMessage());
        });

        new Thread(task, "device-loader").start();
    }

    @FXML
    private void onExportCsv() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Export Devices as CSV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
        fc.setInitialFileName("devices.csv");
        File file = fc.showSaveDialog(deviceTable.getScene().getWindow());
        if (file == null) return;
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("Name,Type,Status,IP Address,Location");
            for (Device d : filteredDevices) {
                pw.printf("%s,%s,%s,%s,%s%n",
                        csvEscape(d.getName()), d.getType(), d.getStatus(),
                        csvEscape(d.getIpAddress()), csvEscape(d.getLocation()));
            }
            statusBar.setText("Exported " + filteredDevices.size() + " devices to " + file.getName());
        } catch (IOException e) {
            showError("Export failed", e.getMessage());
        }
    }

    private String csvEscape(String s) {
        if (s == null) return "";
        return s.contains(",") || s.contains("\"")
                ? "\"" + s.replace("\"", "\"\"") + "\""
                : s;
    }

    @FXML
    private void onAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Device Manager");
        alert.setHeaderText("Device Manager Client v1.0.0");
        alert.setContentText(
                "Portfolio Project 2 — JavaFX Desktop Client\n" +
                "API: " + AppContext.getApiClient().getBaseUrl() + "\n\n" +
                "Built with JavaFX 21 + Java 17\n" +
                "© 2026 mj-deving"
        );
        alert.showAndWait();
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
