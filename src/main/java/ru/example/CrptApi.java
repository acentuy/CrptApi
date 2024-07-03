package ru.example;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrptApi {
    private final HttpClient httpClient;
    private final Semaphore semaphore;
    private final ScheduledExecutorService scheduler;
    private final Gson gson;
    private final Logger logger = LoggerFactory.getLogger(CrptApi.class);

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.httpClient = HttpClient.newBuilder().build();
        this.semaphore = new Semaphore(requestLimit);
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.gson = new Gson();
        limitReset(timeUnit);
    }

    public void createDocument(Document document) {
        try {
            semaphore.acquire();
            String json = gson.toJson(document);
            HttpResponse<String> response = httpClient.send(request(json), HttpResponse.BodyHandlers.ofString());
            logger.info("Request sent. Response status: " + response.statusCode());
        } catch (InterruptedException | IOException e) {
            Thread.currentThread().interrupt();
            logger.error("Error sending request", e);
        } finally {
            semaphore.release();
        }
    }

    private void limitReset(TimeUnit timeUnit) {
        scheduler.scheduleAtFixedRate(() -> {
            int permits = semaphore.drainPermits();
            semaphore.release(permits);
        }, 0, timeUnit.toMillis(1), TimeUnit.MILLISECONDS);
    }

    private HttpRequest request(String json) {
        return HttpRequest.newBuilder()
                .uri(URI.create("https://ismp.crpt.ru/api/v3/lk/documents/create"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();
    }

    public static class Document {
        private Description description;
        private String doc_id;
        private String doc_status;
        private String doc_type;
        private boolean importRequest;
        private String owner_inn;
        private String participant_inn;
        private String producer_inn;
        private String production_type;
        private List<Product> products;
        private Date reg_date;
        private String reg_number;

        public void setDescription(Description description) {
            this.description = description;
        }

        public void setDoc_id(String doc_id) {
            this.doc_id = doc_id;
        }

        public void setDoc_status(String doc_status) {
            this.doc_status = doc_status;
        }

        public void setDoc_type(String doc_type) {
            this.doc_type = doc_type;
        }

        public void setImportRequest(boolean importRequest) {
            this.importRequest = importRequest;
        }

        public void setOwner_inn(String owner_inn) {
            this.owner_inn = owner_inn;
        }

        public void setParticipant_inn(String participant_inn) {
            this.participant_inn = participant_inn;
        }

        public void setProducer_inn(String producer_inn) {
            this.producer_inn = producer_inn;
        }

        public void setProduction_type(String production_type) {
            this.production_type = production_type;
        }

        public void setProducts(List<Product> products) {
            this.products = products;
        }

        public void setReg_date(Date reg_date) {
            this.reg_date = reg_date;
        }

        public void setReg_number(String reg_number) {
            this.reg_number = reg_number;
        }
    }

    public static class Description {
        private String participantInn;

        public void setParticipantInn(String participantInn) {
            this.participantInn = participantInn;
        }
    }

    public static class Product {
        private String certificate_document;
        private Date certificate_document_date;
        private String certificate_document_number;
        private String owner_inn;
        private String producer_inn;
        private Date production_date;
        private String tnved_code;
        private String uit_code;
        private String uitu_code;

        public void setCertificate_document(String certificate_document) {
            this.certificate_document = certificate_document;
        }

        public void setCertificate_document_date(Date certificate_document_date) {
            this.certificate_document_date = certificate_document_date;
        }

        public void setCertificate_document_number(String certificate_document_number) {
            this.certificate_document_number = certificate_document_number;
        }

        public void setOwner_inn(String owner_inn) {
            this.owner_inn = owner_inn;
        }

        public void setProducer_inn(String producer_inn) {
            this.producer_inn = producer_inn;
        }

        public void setProduction_date(Date production_date) {
            this.production_date = production_date;
        }

        public void setTnved_code(String tnved_code) {
            this.tnved_code = tnved_code;
        }

        public void setUit_code(String uit_code) {
            this.uit_code = uit_code;
        }

        public void setUitu_code(String uitu_code) {
            this.uitu_code = uitu_code;
        }
    }
}
