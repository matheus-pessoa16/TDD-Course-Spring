package com.api.library.api;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.api.library.book.Book;
import com.api.library.book.BookRepository;
import com.api.library.book.BookService;
import com.api.library.exceptions.LibraryException;

import org.assertj.core.api.Assertions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

  @InjectMocks
  BookService service;

  @Mock
  BookRepository repository;

  public Book createBook() {
    return Book.builder().isbn("123456").author("Autor Fulano").title("As aventuras").build();
  }

  @Test
  @DisplayName("Should save a book")
  public void saveBookTest() {

    Book book = createBook();

    Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(false);

    Mockito.when(repository.save(book))
        .thenReturn(Book.builder().id(1).isbn("123456").author("Autor Fulano").title("As aventuras").build());

    Book entity = service.save(book);

    Assertions.assertThat(entity.getId()).isNotNull();
    Assertions.assertThat(entity.getIsbn()).isEqualTo("123456");
    Assertions.assertThat(entity.getAuthor()).isEqualTo("Autor Fulano");
    Assertions.assertThat(entity.getTitle()).isEqualTo("As aventuras");
  }

  @Test
  @DisplayName("Should not save a book with duplicated ISBN")
  public void shouldNotSaveDuplicatedIsbnTest() {
    Book book = createBook();

    Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(true);

    String errorMessage = "ISBN já cadastrado!";

    var exception = Assertions.catchThrowable(() -> service.save(book));

    // verificando a instância do erro e a mensagem
    Assertions.assertThat(exception).isInstanceOf(LibraryException.class).hasMessage(errorMessage);

    // verifica que o repository nunca vai executar o save
    Mockito.verify(repository, Mockito.never()).save(book);
  }

  @Test
  @DisplayName("should return a book by its id")
  public void getBookByIdTest() {

    Integer id = 1;
    Book book = createBook();
    book.setId(id);

    Mockito.when(repository.findById(id)).thenReturn(Optional.of(book));

    Optional<Book> foundBook = service.findById(id);

    Assertions.assertThat(foundBook.isPresent()).isTrue();
    Assertions.assertThat(foundBook.get().getId()).isEqualTo(book.getId());
    Assertions.assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
    Assertions.assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
    Assertions.assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
  }

  @Test
  @DisplayName("should return empty when the book does not exist")
  public void bookNotFoundByIdTest() {

    Integer id = 1;
    Mockito.when(repository.findById(id)).thenReturn(Optional.empty());
    Optional<Book> book = service.findById(id);
    Assertions.assertThat(book.isPresent()).isFalse();

  }

  @Test
  @DisplayName("should delete a book")
  public void deleteBookTest() {

    Book book = createBook();
    book.setId(1);
    assertDoesNotThrow(() -> service.delete(book));
    Mockito.verify(repository, Mockito.times(1)).delete(book);

  }

  @Test
  @DisplayName("should not delete an invalid book")
  public void deleteInvalidBookTest() {

    Book book = new Book();

    var exception = Assertions.catchThrowable(() -> service.delete(book));

    // verificando a instância do erro e a mensagem
    Assertions.assertThat(exception).isInstanceOf(LibraryException.class).hasMessage("Id do livro não pode ser nulo");

    // verifica que o repository nunca vai executar o delete
    Mockito.verify(repository, Mockito.never()).delete(book);

  }

  @Test
  @DisplayName("should update a book")
  public void updateBookTest() {
    Integer id = 1;
    Book book = createBook();
    book.setId(id);

    Book updatedBook = Book.builder().id(id).author("author").title("title").isbn("123456").build();

    Mockito.when(repository.save(book)).thenReturn(updatedBook);

    Book entity = service.save(book);

    Assertions.assertThat(entity.getId()).isEqualTo(updatedBook.getId());
    Assertions.assertThat(entity.getAuthor()).isEqualTo(updatedBook.getAuthor());
    Assertions.assertThat(entity.getTitle()).isEqualTo(updatedBook.getTitle());
    Assertions.assertThat(entity.getIsbn()).isEqualTo(updatedBook.getIsbn());

  }

  @Test
  @DisplayName("should not update an invalid book")
  public void updateInvalidBookTest() {
    Book book = new Book();

    var exception = Assertions.catchThrowable(() -> service.update(book));

    // verificando a instância do erro e a mensagem
    Assertions.assertThat(exception).isInstanceOf(LibraryException.class).hasMessage("Id do livro não pode ser nulo");

    // verifica que o repository nunca vai executar o delete
    Mockito.verify(repository, Mockito.never()).save(book);

  }

  @Test
  @DisplayName("should find a book by the parameters")
  public void findBookTest() {
    Book book = createBook();

    PageRequest pageRequest = PageRequest.of(0, 10);

    List<Book> list = Arrays.asList(book);

    Page<Book> page = new PageImpl<Book>(list, pageRequest, 1);

    Mockito.when(repository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class))).thenReturn(page);

    Page<Book> result = service.find(book, pageRequest);

    Assertions.assertThat(result.getTotalElements()).isEqualTo(1);
    Assertions.assertThat(result.getContent()).isEqualTo(list);
    Assertions.assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    Assertions.assertThat(result.getPageable().getPageNumber()).isEqualTo(0);

  }

}
