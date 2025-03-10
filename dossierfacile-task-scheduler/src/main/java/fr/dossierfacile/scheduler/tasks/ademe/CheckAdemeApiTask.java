package fr.dossierfacile.scheduler.tasks.ademe;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.dossierfacile.common.model.AdemeApiResultModel;
import fr.dossierfacile.common.utils.MapperUtil;
import fr.dossierfacile.scheduler.LoggingContext;
import fr.dossierfacile.scheduler.tasks.AbstractTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static fr.dossierfacile.scheduler.tasks.TaskName.CHECK_API_ADEME;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckAdemeApiTask extends AbstractTask {
    private final ObjectMapper objectMapper = MapperUtil.newObjectMapper();

    // Used to check if the API is down and log details
    //@Scheduled(fixedDelayString = "${scheduled.process.check.api.ademe:10}", initialDelayString = "${scheduled.process.check.api.ademe:10}", timeUnit = TimeUnit.MINUTES)
    public void checkAdemeApi() {
        super.startTask(CHECK_API_ADEME);
        URI uri;
        HttpResponse<String> response;
        try {
            uri = new URI("https://observatoire-dpe-audit.ademe.fr/pub/dpe/2392E2001612S");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();
            try (HttpClient client = HttpClient.newHttpClient()) {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
                String json = response.body();
                if (response.statusCode() == 404) {
                    log.error("ADEME API ERROR 404 : DPE NOT FOUND");
                }
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                AdemeApiResultModel ademeApiResultModel = objectMapper.readValue(json, AdemeApiResultModel.class);

                if(!ademeApiResultModel.getNumero().equals("2392E2001612S")) {
                    log.error("ADEME API ERROR : Error with number : {}", ademeApiResultModel.getNumero());
                }
                if (!ademeApiResultModel.getDateRealisation().equals("2023-06-14T22:00:00Z")) {
                    log.error("ADEME API ERROR : Error with date : {}", ademeApiResultModel.getDateRealisation());
                }
                log.info("ADEME API CHECK : OK");
            } catch (Exception e) {
                log.error("ADEME API ERROR : An error occurred while processing the request", e);
            }
        } catch (URISyntaxException e) {
            log.error("ADEME API ERROR : An URISyntaxException occured", e);
        }
        super.endTask();
    }
}
