package com.example.personal_blog.controller;

import com.example.personal_blog.model.Article;
import com.example.personal_blog.service.ArticleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class BlogController {

    private final ArticleService articleService;

    public BlogController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping("/")
    public String getArticles(ModelMap model){
        List<Article> articles= articleService.getAllArticles();
        model.addAttribute("articles", articles);
        return "index";
    }

    @GetMapping("/article/{filename}")
    public String getArticle(@PathVariable String filename, ModelMap model){
        Article article = articleService.getArticle(filename);
        if (article!= null){
            model.addAttribute("article", article);
            return "article";
        }else{
            return "redirect:/";
        }
    }


}