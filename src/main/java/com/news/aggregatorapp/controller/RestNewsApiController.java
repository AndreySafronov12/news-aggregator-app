package com.news.aggregatorapp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.news.aggregatorapp.mapper.ContentMapper;
import com.news.aggregatorapp.model.Content;
import com.news.aggregatorapp.model.ContentList;
import com.news.aggregatorapp.model.MeduzaNews;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@RestController
public class RestNewsApiController {
    private final String url = "https://meduza.io/api/v3/search?chrono=news&locale=ru&page=0&per_page=24";

    @Value("${meduza.api.url.part-one}")
    private String urlPartOne;

    @Value("${meduza.api.url.part-two}")
    private String urlPartTwo;

    private final ContentMapper contentMapper;

    public RestNewsApiController(ContentMapper contentMapper) {
        this.contentMapper = contentMapper;
    }

    @GetMapping(value = "/news", produces = MediaType.APPLICATION_JSON_VALUE)
    public ContentList news(@RequestParam(value = "page", required = false, defaultValue = "0") String page) throws JsonProcessingException {
        String url = createUrl(urlPartOne, urlPartTwo, page);
        String responseJson = getContentFromMeduzaApi(url);
        MeduzaNews meduzaNews = stringToJsonMeduza(responseJson);
        List<Content> content = contentMapper.toDTO(meduzaNews);
        return new ContentList(content);
    }

    private MeduzaNews stringToJsonMeduza(String text) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JSR310Module());
        return objectMapper.readValue(text, MeduzaNews.class);
    }

    private String createUrl(String urlPartOne, String urlPartTwo, String page) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(urlPartOne)
                .append(page)
                .append(urlPartTwo);

        return stringBuilder.toString();
    }

    private String getContentFromMeduzaApi(String url) {
        return WebClient.create().get()
                .uri(url)
                .exchange()
                .block()
                .bodyToMono(String.class)
                .block();
    }
}
