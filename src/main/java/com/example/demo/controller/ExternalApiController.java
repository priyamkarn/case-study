package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ExternalDataDto;
import com.example.demo.service.ExternalApiService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/external")
public class ExternalApiController {

    @Autowired
    private ExternalApiService externalApiService;

    // ============================================
    // RestTemplate Endpoints (Synchronous)
    // ============================================

    @GetMapping("/posts/resttemplate")
    public ResponseEntity<List<ExternalDataDto>> getAllPostsRestTemplate() {
        List<ExternalDataDto> posts = externalApiService.getAllPostsRestTemplate();
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/posts/resttemplate/{id}")
    public ResponseEntity<ExternalDataDto> getPostByIdRestTemplate(@PathVariable Long id) {
        ExternalDataDto post = externalApiService.getPostByIdRestTemplate(id);
        return ResponseEntity.ok(post);
    }

    @GetMapping("/posts/resttemplate/user/{userId}")
    public ResponseEntity<List<ExternalDataDto>> getPostsByUserIdRestTemplate(@PathVariable Long userId) {
        List<ExternalDataDto> posts = externalApiService.getPostsByUserIdRestTemplate(userId);
        return ResponseEntity.ok(posts);
    }

    // ============================================
    // WebClient Endpoints (Reactive)
    // ============================================

    @GetMapping("/posts/webclient")
    public Flux<ExternalDataDto> getAllPostsWebClient() {
        return externalApiService.getAllPostsWebClient();
    }

    @GetMapping("/posts/webclient/{id}")
    public Mono<ExternalDataDto> getPostByIdWebClient(@PathVariable Long id) {
        return externalApiService.getPostByIdWebClient(id);
    }

    @GetMapping("/posts/webclient/user/{userId}")
    public Flux<ExternalDataDto> getPostsByUserIdWebClient(@PathVariable Long userId) {
        return externalApiService.getPostsByUserIdWebClient(userId);
    }

    @GetMapping("/posts/webclient/blocking")
    public ResponseEntity<List<ExternalDataDto>> getAllPostsWebClientBlocking() {
        List<ExternalDataDto> posts = externalApiService.getAllPostsWebClientBlocking();
        return ResponseEntity.ok(posts);
    }
}
