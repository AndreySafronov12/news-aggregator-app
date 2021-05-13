package com.news.aggregatorapp.mapper;

public interface ModelMapper<Model, DTO> {
    DTO toDTO(Model model);
    Model toModel(DTO dto);
}
