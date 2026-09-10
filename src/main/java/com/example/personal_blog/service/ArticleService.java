package com.example.personal_blog.service;

import com.example.personal_blog.model.Article;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ArticleService {

    @Value("${blog.storage.path}")
    private String storagePath;

    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(ArticleService.class);

    public ArticleService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @PostConstruct
    private void init() {
        try {
            Files.createDirectories(Paths.get(storagePath));
            logger.info("Создана директория: {}", storagePath);
        } catch (IOException e) {
            logger.error("Не удалось создать папку: {}", storagePath, e);
        }
    }

    public List<Article> getAllArticles() {
        File folder = new File(storagePath);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));

        if (files == null || files.length == 0) {
            logger.info("Список пуст.");
            return new ArrayList<>();
        }

        return Arrays.stream(files)
                .map(this::readArticleFromFile)
                .filter(Objects::nonNull)
                .sorted((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public Article readArticleFromFile(File file) {
        try {
            Article article = objectMapper.readValue(file, Article.class);
            String filename = file.getName().replace(".json", "");
            article.setFilename(filename);
            logger.info("Статья {} прочитана из файла", article.getFilename());
            return article;
        } catch (IOException e) {
            logger.error("Ошибка чтения файла: {}", file.getName(), e);
            return null;
        }
    }

    public Article getArticle(String filename) {
        File file = new File(storagePath, filename + ".json");
        if (!file.exists()) {
            logger.info("Файла {} не существует", filename);
            return null;
        }
        return readArticleFromFile(file);
    }

    public void saveArticle(Article article) {
        try {
            String title = article.getTitle()
                    .toLowerCase()
                    .replace(" ", "-")
                    .replaceAll("[^a-z0-9-]", "");

            if (title.isBlank()) {
                title = "article";
            }

            String timestamp = LocalDateTime.now()
                    .toString()
                    .replace(":", "-")
                    .replace(".", "-");

            String filename = timestamp + "-" + title;
            article.setFilename(filename);

            File file = new File(storagePath, filename + ".json");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, article);

            logger.info("Статья сохранена в файл: {}", article.getFilename());
        } catch (IOException e) {
            logger.error("Ошибка сохранения файла: {}", e.getMessage(), e);
        }
    }

    public boolean deleteArticle(Article article) {
        File file = new File(storagePath, article.getFilename() + ".json");

        if (!file.exists()) {
            logger.warn("Файл не найден: {}", article.getFilename());
            return false;
        }

        boolean deleted = file.delete();
        if (deleted) {
            logger.info("Статья удалена: {}", article.getFilename());
        } else {
            logger.error("Не удалось удалить файл: {}", article.getFilename());
        }
        return deleted;
    }
}