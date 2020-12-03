package com.api.library.repository;

import java.util.Optional;

import com.api.library.book.Book;
import com.api.library.book.BookRepository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest // cria uma instância do bd em memória apenas para executar testes. Depois o bd
             // é apagado
public class BookRepositoryTest {

  @Autowired
  TestEntityManager entityManager;

  @Autowired
  BookRepository repository;

  public Book createBook(String isbn) {
    return Book.builder().title("As aventuras").author("Autor").isbn(isbn).build();
  }

  @Test
  @DisplayName("should return a book with from an isbn if exists")
  public void returnTrueWhenIsbnExists() {
    String isbn = "123";

    Book book = createBook(isbn);
    entityManager.persist(book);

    boolean exists = repository.existsByIsbn(isbn);

    Assertions.assertThat(exists).isTrue();
  }

  @Test
  @DisplayName("should return false when isbn does not exists")
  public void returnFalseWhenIsbnDoesNotExist() {
    String isbn = "123";
    boolean exists = repository.existsByIsbn(isbn);

    Assertions.assertThat(exists).isFalse();
  }

  @Test
  @DisplayName("should return a book by id")
  public void getBookbyId() {
    Book book = createBook("123");

    entityManager.persist(book);
    Optional<Book> foundBook = repository.findById(book.getId());
    Assertions.assertThat(foundBook.isPresent()).isTrue();

  }

  @Test
  @DisplayName("should save a book")
  public void saveBookTest() {
    Book book = createBook("123");

    Book savedBook = repository.save(book);

    Assertions.assertThat(savedBook.getId()).isNotNull();
  }

  @Test
  @DisplayName("should delete a book")
  public void deleteBookTest() {

    Book book = createBook("123");
    entityManager.persist(book);

    Book foundBook = entityManager.find(Book.class, book.getId());

    repository.delete(foundBook);

    Book deletedBook = entityManager.find(Book.class, book.getId());

    Assertions.assertThat(deletedBook).isNull();
  }

}
