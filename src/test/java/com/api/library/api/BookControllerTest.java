package com.api.library.api;

import java.util.Arrays;
import java.util.Optional;

import com.api.library.book.Book;
import com.api.library.book.BookController;
import com.api.library.book.BookDTO;
import com.api.library.book.BookService;
import com.api.library.exceptions.LibraryException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = BookController.class)
@AutoConfigureMockMvc
public class BookControllerTest {

  static String BOOK_API = "/api/book";

  @Autowired
  MockMvc mvc;

  @MockBean
  BookService service;

  public BookDTO createBookDTO() {
    return BookDTO.builder().author("Teste").title("Teste").isbn("001").build();
  }

  public Book createBook() {
    return Book.builder().id(1).author("Teste").title("Teste").isbn("001").build();
  }

  @Test
  @DisplayName("Create a new book")
  public void createBookTest() throws Exception {

    BookDTO dto = createBookDTO();
    Book book = createBook();

    BDDMockito.given(service.save(Mockito.any(Book.class))).willReturn(book);

    String json = new ObjectMapper().writeValueAsString(dto);

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(json);

    mvc.perform(request).andExpect(MockMvcResultMatchers.status().isCreated())
        .andExpect(MockMvcResultMatchers.jsonPath("id").value(1))
        .andExpect(MockMvcResultMatchers.jsonPath("title").value(dto.getTitle()))
        .andExpect(MockMvcResultMatchers.jsonPath("author").value(dto.getAuthor()))
        .andExpect(MockMvcResultMatchers.jsonPath("isbn").value(dto.getIsbn()));
  }

  @Test
  @DisplayName("Should not create a new book with invalid data")
  public void createInvalidBookTest() throws Exception {

    String json = new ObjectMapper().writeValueAsString(new BookDTO());

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(json);

    mvc.perform(request).andExpect(MockMvcResultMatchers.status().isBadRequest())
        .andExpect(MockMvcResultMatchers.jsonPath("errors", Matchers.hasSize(3)));

  }

  @Test
  @DisplayName("Should not create a book with duplicated ISBN")
  public void createBookWithDuplicatedIsbn() throws Exception {

    BookDTO dto = createBookDTO();

    String json = new ObjectMapper().writeValueAsString(dto);

    String errorMessage = "ISBN já cadastrado!";

    BDDMockito.given(service.save(Mockito.any(Book.class))).willThrow(new LibraryException(errorMessage));

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(json);

    mvc.perform(request).andExpect(MockMvcResultMatchers.status().isBadRequest())
        .andExpect(MockMvcResultMatchers.jsonPath("errors", Matchers.hasSize(1)))
        .andExpect(MockMvcResultMatchers.jsonPath("errors[0]").value(errorMessage));

  }

  @Test
  @DisplayName("should return a book")
  public void getBookDetailsTest() throws Exception {

    Integer id = 1;

    Book book = createBook();

    book.setId(id);

    BDDMockito.given(service.findById(id)).willReturn(Optional.of(book));

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(BOOK_API.concat("/" + id))
        .accept(MediaType.APPLICATION_JSON);

    mvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("id").value(id))
        .andExpect(MockMvcResultMatchers.jsonPath("title").value(createBookDTO().getTitle()))
        .andExpect(MockMvcResultMatchers.jsonPath("author").value(createBookDTO().getAuthor()))
        .andExpect(MockMvcResultMatchers.jsonPath("isbn").value(createBookDTO().getIsbn()));
  }

  @Test
  @DisplayName("should return not found when the book does not exist")
  public void bookNotFoundTest() throws Exception {

    BDDMockito.given(service.findById(Mockito.anyInt())).willReturn(Optional.empty());

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(BOOK_API.concat("/" + 1))
        .accept(MediaType.APPLICATION_JSON);

    mvc.perform(request).andExpect(MockMvcResultMatchers.status().isNotFound());
  }

  @Test
  @DisplayName("should delete a book")
  public void deleteBookTest() throws Exception {

    BDDMockito.given(service.findById(Mockito.anyInt())).willReturn(Optional.of(Book.builder().id(1).build()));

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(BOOK_API.concat("/" + 1))
        .accept(MediaType.APPLICATION_JSON);

    mvc.perform(request).andExpect(MockMvcResultMatchers.status().isNoContent());
  }

  @Test
  @DisplayName("should return not found when there is not a book to delete")
  public void deleteInexistentBookTest() throws Exception {

    BDDMockito.given(service.findById(Mockito.anyInt())).willReturn(Optional.empty());

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(BOOK_API.concat("/" + 1))
        .accept(MediaType.APPLICATION_JSON);

    mvc.perform(request).andExpect(MockMvcResultMatchers.status().isNotFound());
  }

  @Test
  @DisplayName("should update a book")
  public void updateBook() throws Exception {
    Integer id = 1;

    String json = new ObjectMapper().writeValueAsString(createBook());

    Book book = Book.builder().id(1).title("title").author("author").isbn("333").build();

    Book updatedBook = Book.builder().id(id).author("Teste").title("Teste").isbn("333").build();

    BDDMockito.given(service.findById(id)).willReturn(Optional.of(book));

    BDDMockito.given(service.update(book)).willReturn(updatedBook);

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(BOOK_API.concat("/" + 1)).content(json)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);

    mvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("id").value(id))
        .andExpect(MockMvcResultMatchers.jsonPath("title").value(createBookDTO().getTitle()))
        .andExpect(MockMvcResultMatchers.jsonPath("author").value(createBookDTO().getAuthor()))
        .andExpect(MockMvcResultMatchers.jsonPath("isbn").value("333"));

  }

  @Test
  @DisplayName("should not update a inexistent book")
  public void updateInexistentBook() throws Exception {

    String json = new ObjectMapper().writeValueAsString(createBook());

    BDDMockito.given(service.findById(Mockito.anyInt())).willReturn(Optional.empty());

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(BOOK_API.concat("/" + 1)).content(json)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);

    mvc.perform(request).andExpect(MockMvcResultMatchers.status().isNotFound());
  }

  @Test
  @DisplayName("should get books by parameters")
  public void findBooksTest() throws Exception {

    Integer id = 1;

    Book book = createBook();
    book.setId(id);

    BDDMockito.given(service.find(Mockito.any(Book.class), Mockito.any(Pageable.class)))
        .willReturn(new PageImpl<Book>(Arrays.asList(book), PageRequest.of(0, 100), 1));

    String queryString = String.format("?title=%s&author=%s&page=0&size=100", book.getTitle(), book.getAuthor());

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(BOOK_API.concat(queryString))
        .accept(MediaType.APPLICATION_JSON);

    mvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("content", Matchers.hasSize(1)))
        .andExpect(MockMvcResultMatchers.jsonPath("totalElements").value(1))
        .andExpect((MockMvcResultMatchers.jsonPath("pageable.pageSize").value(100)))
        .andExpect((MockMvcResultMatchers.jsonPath("pageable.pageNumber")).value(0));

  }

}
