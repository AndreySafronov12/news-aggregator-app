package com.news.aggregatorapp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.news.aggregatorapp.mapper.ContentMapper;
import com.news.aggregatorapp.model.Content;
import com.news.aggregatorapp.model.ContentList;
import com.news.aggregatorapp.model.MeduzaNews;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@RestController
public class RestNewApiController {
    private final String url = "https://meduza.io/api/v3/search?chrono=news&locale=ru&page=0&per_page=24";
    private final ContentMapper contentMapper;

    public RestNewApiController(ContentMapper contentMapper) {
        this.contentMapper = contentMapper;
    }

    @GetMapping(value = "/news", produces = MediaType.APPLICATION_JSON_VALUE)
    public ContentList news(@RequestParam(value = "page", required = false, defaultValue = "0") String page) throws JsonProcessingException {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("https://meduza.io/api/v3/search?chrono=news&locale=ru&page=")
                .append(page)
                .append("&per_page=24");

        String url = stringBuilder.toString();

        WebClient webClient = WebClient.create();
        String responseJson = webClient.get()
                .uri(url)
                .exchange()
                .block()
                .bodyToMono(String.class)
                .block();

        MeduzaNews meduzaNews = stringToJsonMeduza(responseJson);
        List<Content> content = contentMapper.toDTO(meduzaNews);
        return new ContentList(content);
    }

    private MeduzaNews stringToJsonMeduza(String text) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JSR310Module());
        return objectMapper.readValue(text, MeduzaNews.class);
    }


}
