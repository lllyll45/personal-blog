package com.example.personal_blog.controller;

import com.example.personal_blog.model.Article;
import com.example.personal_blog.service.ArticleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ArticleService articleService;

    public AdminController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping("/dashboard")
    public String dashboard(ModelMap model) {
        List<Article> articles = articleService.getAllArticles();
        model.addAttribute("articles", articles);
        return "admin/dashboard";
    }

    @GetMapping("/add")
    public String addForm() {
        return "admin/add";
    }

    @PostMapping("/add")
    public String addArticle(@ModelAttribute Article article) {
        articleService.saveArticle(article);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/edit/{filename}")
    public String editForm(@PathVariable String filename, ModelMap model) {
        Article article = articleService.getArticle(filename);
        if (article == null) {
            return "redirect:/admin/dashboard";
        }
        model.addAttribute("article", article);
        return "admin/edit";
    }

    @PostMapping("/edit/{filename}")
    public String editArticle(@PathVariable String filename, @ModelAttribute Article updatedArticle) {
        Article existingArticle = articleService.getArticle(filename);
        if (existingArticle == null) {
            return "redirect:/admin/dashboard";
        }
        articleService.deleteArticle(existingArticle);
        articleService.saveArticle(updatedArticle);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/delete/{filename}")
    public String deleteArticle(@PathVariable String filename) {
        Article article = articleService.getArticle(filename);
        if (article != null) {
            articleService.deleteArticle(article);
        }
        return "redirect:/admin/dashboard";
    }
}