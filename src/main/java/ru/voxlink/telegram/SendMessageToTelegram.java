package ru.voxlink.telegram;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

public class SendMessageToTelegram {
    private static final Logger LOGGER = LogManager.getLogger(SendMessageToTelegram.class);
    private final String URL = "https://api.telegram.org/";
    private final String PAGE_MAPPING = "/sendMessage?";
    private final String BOT_ID;
    private final String CHAT_ID;
    private String textMassage = "Напишите текст сообщения!";
    private String tags = "";
    private String hostName = "";

    public SendMessageToTelegram(String botId, String chatId, List<String> tags) {
        this.BOT_ID = botId;
        this.CHAT_ID = chatId;
        for (int i = 0; i < tags.size(); i++) {
            this.tags = this.tags + tags.get(i) + " ";
        }
        try {
            this.hostName = InetAddress.getLocalHost().getHostName() + " - ";
        } catch (UnknownHostException e) {
            LOGGER.info("Не удалось определить HostName сервера");
        }

    }

    public void send(String message) {

        String resultMessage = tags + message;
        String encodeMessage = URLEncoder.encode(resultMessage, StandardCharsets.UTF_8);
        String uriTelegram = URL + BOT_ID + PAGE_MAPPING + "chat_id=" + CHAT_ID + "&" + "text=" + encodeMessage;
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(uriTelegram))
                .build();
        try {
            HttpResponse<String> httpResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (httpResponse.body().contains("error_code")) {
                LOGGER.warn(hostName + "Ошибка отправки сообщения в телеграмм \"" + message + "\" ответ сервера " + httpResponse.body());
            }
        } catch (HttpConnectTimeoutException e) {
            LOGGER.warn(hostName + "Таймаут запроса при отправке ссобщения в телеграмм (возможно проблемы с интернетом)");
        } catch (IOException e) {
            LOGGER.warn(hostName + "Ошибка отправки сообщения \"" + resultMessage + "\" в телеграмм. " + e.getStackTrace().toString());

        } catch (InterruptedException e) {
            LOGGER.warn(hostName + "Ошибка отправки сообщения в телеграмм \"" + resultMessage + "\" " + e);
        } catch (RuntimeException e) {
            LOGGER.warn(hostName + "Ошибка отправки сообщения в телеграмм \"" + resultMessage + "\" " + e);
        }
    }


}
