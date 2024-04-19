package ru.gb.springbootlesson3.controllers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.gb.springbootlesson3.entity.Book;
import ru.gb.springbootlesson3.repository.BookRepository;

import java.util.List;
import java.util.Objects;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class BookControllerTest {

    @Autowired
    WebTestClient webTestClient;
    @Autowired
    BookRepository bookRepository;
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void testGetAllBooks() {
        List<Book> books = bookRepository.findAll();

        List<Book> responseBody = webTestClient.get()
                .uri("books")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<Book>>() {
                })
                .returnResult()
                .getResponseBody();

        Assertions.assertEquals(books.size(), responseBody.size());
        for (Book book:responseBody) {
            boolean found = books.stream()
                    .filter(it -> Objects.equals(book.getId(), it.getId()))
                    .anyMatch(it -> Objects.equals(book.getName(), it.getName()));
            Assertions.assertTrue(found);
        }
    }

    @Test
    void testGetByIdSuccess() {
        Book savedBook = bookRepository.save(new Book("Спартак"));

        Book responseBody = webTestClient.get()
                .uri("books/" + savedBook.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Book.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertNotNull(responseBody);
        Assertions.assertEquals(responseBody.getId(), savedBook.getId());
        Assertions.assertEquals(responseBody.getName(), savedBook.getName());
    }

    @Test
    void testGetByIdNotFound() {
//        long size = bookRepository.findAll().size();
//        long unexpectedId = size + 1;
        Long maxId = jdbcTemplate.queryForObject("select max(id) from books", Long.class);
        long unexpectedId;
        if (maxId == null) {
            unexpectedId = 1;
        } else {
            unexpectedId = maxId + 1;
        }

        webTestClient.get()
                .uri("books/" + unexpectedId)
                .exchange()
                .expectStatus().isNotFound();
    }
}