package com.example.demo.service;

import com.example.demo.dto.NewsRequestDto;
import com.example.demo.dto.NewsResponseDto;
import com.example.demo.entity.News;
import com.example.demo.exception.NewsNotFoundException;
import com.example.demo.repository.NewsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NewsService {

    private final NewsRepository repository;

    public NewsService(NewsRepository repository) {
        this.repository = repository;
    }

    public NewsResponseDto create(NewsRequestDto dto) {
        News news = new News();
        news.setTitle(dto.getTitle());
        news.setContent(dto.getContent());
        news.setPublishedAt(dto.getPublishedAt());
        news.setAuthor(dto.getAuthor());
        News saved = repository.save(news);
        return toDto(saved);
    }

    public List<NewsResponseDto> getAll() {
        return repository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public NewsResponseDto getById(Long id) {
        News news = repository.findById(id)
                .orElseThrow(() -> new NewsNotFoundException(id));
        return toDto(news);
    }

    public NewsResponseDto update(Long id, NewsRequestDto dto) {
        News news = repository.findById(id)
                .orElseThrow(() -> new NewsNotFoundException(id));
        news.setTitle(dto.getTitle());
        news.setContent(dto.getContent());
        news.setPublishedAt(dto.getPublishedAt());
        news.setAuthor(dto.getAuthor());
        News updated = repository.save(news);
        return toDto(updated);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NewsNotFoundException(id);
        }
        repository.deleteById(id);
    }

    private NewsResponseDto toDto(News news) {
        NewsResponseDto dto = new NewsResponseDto();
        dto.setId(news.getId());
        dto.setTitle(news.getTitle());
        dto.setContent(news.getContent());
        dto.setPublishedAt(news.getPublishedAt());
        dto.setAuthor(news.getAuthor());
        return dto;
    }
}