package com.mj.portfolio.client;

import com.mj.portfolio.client.service.ApiClient;

/**
 * Simple static holder for the shared ApiClient instance.
 * Set once at login time; read by all controllers thereafter.
 */
public class AppContext {

    private static ApiClient apiClient;

    public static ApiClient getApiClient() { return apiClient; }

    public static void setApiClient(ApiClient c) { apiClient = c; }
}
