package com.mj.portfolio.client.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;

/**
 * Thin wrapper around Java 17's built-in java.net.http.HttpClient.
 * Base URL defaults to http://localhost:8080 but can be overridden
 * via the system property: -Dapi.baseUrl=http://...
 */
public class ApiClient {

    private static final String DEFAULT_BASE_URL = "http://localhost:8080";

    private final String baseUrl;
    private final HttpClient httpClient;
    final Gson gson;
    private String authHeader;

    public ApiClient() {
        this.baseUrl = System.getProperty("api.baseUrl", DEFAULT_BASE_URL);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.gson = new GsonBuilder().create();
    }

    public String getBaseUrl() { return baseUrl; }

    public void setCredentials(String username, String password) {
        String encoded = Base64.getEncoder().encodeToString(
                (username + ":" + password).getBytes());
        this.authHeader = "Basic " + encoded;
    }

    public boolean hasCredentials() { return authHeader != null; }

    public HttpResponse<String> get(String path) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(10))
                .GET();
        if (authHeader != null) builder.header("Authorization", authHeader);
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> post(String path, Object body) throws IOException, InterruptedException {
        String json = gson.toJson(body);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(json));
        if (authHeader != null) builder.header("Authorization", authHeader);
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> put(String path, Object body) throws IOException, InterruptedException {
        String json = gson.toJson(body);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(10))
                .PUT(HttpRequest.BodyPublishers.ofString(json));
        if (authHeader != null) builder.header("Authorization", authHeader);
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> delete(String path) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(10))
                .DELETE();
        if (authHeader != null) builder.header("Authorization", authHeader);
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}
