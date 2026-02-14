package com.mj.portfolio.client.service;

import com.google.gson.reflect.TypeToken;
import com.mj.portfolio.client.model.Device;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Async wrapper around ApiClient for Device CRUD operations.
 * All methods return CompletableFuture so the JavaFX UI thread is never blocked.
 */
public class DeviceApiService {

    private static final String BASE_PATH = "/api/v1/devices";
    private static final Executor POOL = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "api-worker");
        t.setDaemon(true);
        return t;
    });

    private final ApiClient client;

    public DeviceApiService(ApiClient client) {
        this.client = client;
    }

    public CompletableFuture<List<Device>> getAll() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpResponse<String> resp = client.get(BASE_PATH + "?size=200&sort=name");
                if (resp.statusCode() == 200) {
                    // Spring Page JSON: { "content": [...] }
                    Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
                    Map<String, Object> page = client.gson.fromJson(resp.body(), mapType);
                    String contentJson = client.gson.toJson(page.get("content"));
                    Type listType = new TypeToken<List<Device>>(){}.getType();
                    return client.gson.fromJson(contentJson, listType);
                }
                return Collections.emptyList();
            } catch (Exception e) {
                throw new RuntimeException("Failed to fetch devices", e);
            }
        }, POOL);
    }

    public CompletableFuture<Device> getById(String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpResponse<String> resp = client.get(BASE_PATH + "/" + id);
                if (resp.statusCode() == 200) {
                    return client.gson.fromJson(resp.body(), Device.class);
                }
                throw new RuntimeException("Device not found: " + id);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException("Failed to fetch device", e);
            }
        }, POOL);
    }

    public CompletableFuture<Device> create(Map<String, Object> requestBody) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpResponse<String> resp = client.post(BASE_PATH, requestBody);
                if (resp.statusCode() == 201) {
                    return client.gson.fromJson(resp.body(), Device.class);
                }
                throw new RuntimeException("Create failed: HTTP " + resp.statusCode());
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException("Failed to create device", e);
            }
        }, POOL);
    }

    public CompletableFuture<Device> update(String id, Map<String, Object> requestBody) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpResponse<String> resp = client.put(BASE_PATH + "/" + id, requestBody);
                if (resp.statusCode() == 200) {
                    return client.gson.fromJson(resp.body(), Device.class);
                }
                throw new RuntimeException("Update failed: HTTP " + resp.statusCode());
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException("Failed to update device", e);
            }
        }, POOL);
    }

    public CompletableFuture<Void> delete(String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                client.delete(BASE_PATH + "/" + id);
                return null;
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException("Failed to delete device", e);
            }
        }, POOL);
    }
}
