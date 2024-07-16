import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class CrptApi {

    private final Semaphore semaphore;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.semaphore = new Semaphore(requestLimit);
        initScheduler(timeUnit.toMillis(1), requestLimit);
    }

    private void initScheduler(long delay, int requestLimit) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() ->
                        semaphore.release(requestLimit - semaphore.availablePermits()),
                delay, delay, TimeUnit.MILLISECONDS);
    }

    public String createDocument(Document document, String signature) {
        String result = "";
        try {
            semaphore.acquire();
            String apiUrl = "https://ismp.crpt.ru/api/v3/lk/documents/create";
            String jsonDocument = new ObjectMapper().writeValueAsString(document);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Signature", signature)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonDocument))
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request,
                    HttpResponse.BodyHandlers.ofString());
            result = response.body();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static class Document {

        @JsonProperty("description")
        private final Description description;

        @JsonProperty("doc_id")
        private final String docId;

        @JsonProperty("doc_status")
        private final String docStatus;

        @JsonProperty("doc_type")
        private final String docType;

        @JsonProperty("importRequest")
        private final boolean importRequest;

        @JsonProperty("owner_inn")
        private final String ownerInn;

        @JsonProperty("participant_inn")
        private final String participantInn;

        @JsonProperty("producer_inn")
        private final String producerInn;

        @JsonProperty("production_date")
        private final String productionDate;

        @JsonProperty("production_type")
        private final String productionType;

        @JsonProperty("products")
        private final List<Product> products;

        @JsonProperty("reg_date")
        private final String regDate;

        @JsonProperty("reg_number")
        private final String regNumber;

        public Document(Description description,
                        String docId,
                        String docStatus,
                        String docType,
                        boolean importRequest,
                        String ownerInn,
                        String participantInn,
                        String producerInn,
                        String productionDate,
                        String productionType,
                        List<Product> products,
                        String regDate,
                        String regNumber) {
            this.description = description;
            this.docId = docId;
            this.docStatus = docStatus;
            this.docType = docType;
            this.importRequest = importRequest;
            this.ownerInn = ownerInn;
            this.participantInn = participantInn;
            this.producerInn = producerInn;
            this.productionDate = productionDate;
            this.productionType = productionType;
            this.products = products;
            this.regDate = regDate;
            this.regNumber = regNumber;
        }
    }

    public static class Description {

        @JsonProperty("participantInn")
        private final String participantInn;

        public Description(String participantInn) {
            this.participantInn = participantInn;
        }
    }

    public static class Product {

        @JsonProperty("certificate_document")
        private final String certificateDocument;

        @JsonProperty("certificate_document_date")
        private final String certificateDocumentDate;

        @JsonProperty("certificate_document_number")
        private final String certificateDocumentNumber;

        @JsonProperty("owner_inn")
        private final String ownerInn;

        @JsonProperty("producer_inn")
        private final String producerInn;

        @JsonProperty("production_date")
        private final String productionDate;

        @JsonProperty("tnved_code")
        private final String tnvedCode;

        @JsonProperty("uit_code")
        private final String uitCode;

        @JsonProperty("uitu_code")
        private final String uituCode;

        public Product(String certificateDocument,
                       String certificateDocumentDate,
                       String certificateDocumentNumber,
                       String ownerInn,
                       String producerInn,
                       String productionDate,
                       String tnvedCode,
                       String uitCode,
                       String uituCode) {
            this.certificateDocument = certificateDocument;
            this.certificateDocumentDate = certificateDocumentDate;
            this.certificateDocumentNumber = certificateDocumentNumber;
            this.ownerInn = ownerInn;
            this.producerInn = producerInn;
            this.productionDate = productionDate;
            this.tnvedCode = tnvedCode;
            this.uitCode = uitCode;
            this.uituCode = uituCode;
        }
    }

    public static void main(String[] args) {

        var crptApi = new CrptApi(TimeUnit.MINUTES, 1);

        CrptApi.Product product = new CrptApi.Product(
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "");

        CrptApi.Description description = new CrptApi.Description("");

        CrptApi.Document document = new CrptApi.Document(
                description,
                "",
                "",
                "",
                true,
                "",
                "",
                "",
                "",
                "",
                List.of(product),
                "",
                ""
        );

        System.out.println(crptApi.createDocument(document, "signature_1"));
        System.out.println(crptApi.createDocument(document, "signature_2"));
        System.out.println(crptApi.createDocument(document, "signature_3"));
    }
}