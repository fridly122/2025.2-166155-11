package itss.group11.controller.uc6;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import itss.group11.controller.chung.ApiConfig;
import itss.group11.entity.uc6.PurchaseOrderResponseDTO;
import itss.group11.entity.uc6.ReconciliationDetailDTO;
import itss.group11.entity.uc6.ReconciliationResultDTO;
import itss.group11.entity.uc6.ReconciliationSubmitDTO;

public class HttpReconciliationApiClient implements ReconciliationApiClient {

    private static final String BASE_URL = ApiConfig.baseUrl("/api/v1/purchase-orders");

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public HttpReconciliationApiClient() {
        this(HttpClient.newHttpClient(), new ObjectMapper());
    }

    HttpReconciliationApiClient(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<PurchaseOrderResponseDTO> getInTransitOrders() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/in-transit"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        requireSuccess(response);

        PurchaseOrderResponseDTO[] rows = objectMapper.readValue(response.body(), PurchaseOrderResponseDTO[].class);
        return Arrays.asList(rows);
    }

    @Override
    public ReconciliationDetailDTO getReconciliationDetail(String orderId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + encode(orderId) + "/reconciliation"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        requireSuccess(response);

        return objectMapper.readValue(response.body(), ReconciliationDetailDTO.class);
    }

    @Override
    public ReconciliationResultDTO reconcile(String orderId, ReconciliationSubmitDTO dto)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + encode(orderId) + "/reconcile"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(dto)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        requireSuccess(response);

        return objectMapper.readValue(response.body(), ReconciliationResultDTO.class);
    }

    private void requireSuccess(HttpResponse<String> response) throws IOException {
        if (response.statusCode() != 200) {
            throw new IOException(response.body());
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
