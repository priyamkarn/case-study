package com.example.demo.service;

import com.example.demo.dto.ExternalDataDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class ExternalApiService {

    private static final Logger logger = LoggerFactory.getLogger(ExternalApiService.class);
    private static final String BASE_URL = "https://jsonplaceholder.typicode.com";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WebClient webClient;

    // ============================================
    // RestTemplate Methods (Synchronous)
    // ============================================

    /**
     * Fetch all posts using RestTemplate
     */
    public List<ExternalDataDto> getAllPostsRestTemplate() {
        logger.info("Fetching all posts using RestTemplate");
        String url = BASE_URL + "/posts";
        
        try {
            ResponseEntity<List<ExternalDataDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ExternalDataDto>>() {}
            );
            
            logger.info("Successfully fetched {} posts", response.getBody().size());
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error fetching posts with RestTemplate: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch posts", e);
        }
    }

    /**
     * Fetch single post by ID using RestTemplate
     */
    public ExternalDataDto getPostByIdRestTemplate(Long id) {
        logger.info("Fetching post with ID: {} using RestTemplate", id);
        String url = BASE_URL + "/posts/" + id;
        
        try {
            ExternalDataDto post = restTemplate.getForObject(url, ExternalDataDto.class);
            logger.info("Successfully fetched post: {}", post.getTitle());
            return post;
        } catch (Exception e) {
            logger.error("Error fetching post {} with RestTemplate: {}", id, e.getMessage());
            throw new RuntimeException("Failed to fetch post with ID: " + id, e);
        }
    }

    /**
     * Fetch posts by user ID using RestTemplate
     */
    public List<ExternalDataDto> getPostsByUserIdRestTemplate(Long userId) {
        logger.info("Fetching posts for user ID: {} using RestTemplate", userId);
        String url = BASE_URL + "/posts?userId=" + userId;
        
        try {
            ResponseEntity<List<ExternalDataDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ExternalDataDto>>() {}
            );
            
            logger.info("Successfully fetched {} posts for user {}", response.getBody().size(), userId);
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error fetching posts for user {} with RestTemplate: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to fetch posts for user: " + userId, e);
        }
    }

    // ============================================
    // WebClient Methods (Asynchronous/Reactive)
    // ============================================

    /**
     * Fetch all posts using WebClient (Reactive)
     */
    public Flux<ExternalDataDto> getAllPostsWebClient() {
        logger.info("Fetching all posts using WebClient");
        
        return webClient.get()
                .uri("/posts")
                .retrieve()
                .bodyToFlux(ExternalDataDto.class)
                .doOnComplete(() -> logger.info("Successfully fetched all posts"))
                .doOnError(e -> logger.error("Error fetching posts with WebClient: {}", e.getMessage()));
    }

    /**
     * Fetch single post by ID using WebClient (Reactive)
     */
    public Mono<ExternalDataDto> getPostByIdWebClient(Long id) {
        logger.info("Fetching post with ID: {} using WebClient", id);
        
        return webClient.get()
                .uri("/posts/{id}", id)
                .retrieve()
                .bodyToMono(ExternalDataDto.class)
                .doOnSuccess(post -> logger.info("Successfully fetched post: {}", post.getTitle()))
                .doOnError(e -> logger.error("Error fetching post {} with WebClient: {}", id, e.getMessage()));
    }

    /**
     * Fetch posts by user ID using WebClient (Reactive)
     */
    public Flux<ExternalDataDto> getPostsByUserIdWebClient(Long userId) {
        logger.info("Fetching posts for user ID: {} using WebClient", userId);
        
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/posts")
                    .queryParam("userId", userId)
                    .build())
                .retrieve()
                .bodyToFlux(ExternalDataDto.class)
                .doOnComplete(() -> logger.info("Successfully fetched posts for user {}", userId))
                .doOnError(e -> logger.error("Error fetching posts for user {} with WebClient: {}", userId, e.getMessage()));
    }

    /**
     * Fetch posts synchronously from WebClient (blocking)
     */
    public List<ExternalDataDto> getAllPostsWebClientBlocking() {
        logger.info("Fetching all posts using WebClient (blocking)");
        
        return webClient.get()
                .uri("/posts")
                .retrieve()
                .bodyToFlux(ExternalDataDto.class)
                .collectList()
                .block(); // Converts reactive to blocking
    }
}