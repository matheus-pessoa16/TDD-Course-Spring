package com.api.library.api;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import com.api.library.book.Book;
import com.api.library.book.BookService;
import com.api.library.exceptions.LibraryException;
import com.api.library.loan.Loan;
import com.api.library.loan.LoanController;
import com.api.library.loan.LoanDTO;
import com.api.library.loan.LoanFilterDTO;
import com.api.library.loan.LoanService;
import com.api.library.loan.ReturnedLoanDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.MediaType;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = LoanController.class)
@AutoConfigureMockMvc
public class LoanControllerTest {

  static final String LOAN_API = "/api/loan";

  @Autowired
  MockMvc mvc;

  @MockBean
  private BookService bookService;

  @MockBean
  private LoanService loanService;

  public LoanDTO newLoanDTO() {
    return LoanDTO.builder().isbn("123").customer("Fulano").email("customer@mail.com").build();
  }

  public Loan newLoan(Book book) {
    return Loan.builder().customer("Fulano").book(book).loanDate(LocalDate.now()).build();
  }

  @Test
  @DisplayName("should create a book")
  public void createLoanTest() throws Exception {
    LoanDTO dto = newLoanDTO();

    String isbn = "123";

    String json = new ObjectMapper().writeValueAsString(dto);

    Book book = Book.builder().id(1).isbn(isbn).build();

    Loan loan = newLoan(book);
    loan.setId(1);

    BDDMockito.given(bookService.findBookByIsbn(isbn)).willReturn(Optional.of(book));

    BDDMockito.given(loanService.save(Mockito.any(Loan.class))).willReturn(loan);

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API).accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON).content(json);

    mvc.perform(request).andExpect(MockMvcResultMatchers.status().isCreated())
        .andExpect(MockMvcResultMatchers.content().string("1"));
  }

  @Test
  @DisplayName("should return an error when the book does not exist")
  public void invalidIsbnCreateLoanTest() throws Exception {
    LoanDTO dto = newLoanDTO();

    String json = new ObjectMapper().writeValueAsString(dto);

    BDDMockito.given(bookService.findBookByIsbn("123")).willReturn(Optional.empty());

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API).accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON).content(json);

    mvc.perform(request).andExpect(MockMvcResultMatchers.status().isBadRequest())
        .andExpect(MockMvcResultMatchers.jsonPath("errors", Matchers.hasSize(1)))
        .andExpect(MockMvcResultMatchers.jsonPath("errors[0]").value("Book not found"));

  }

  @Test
  @DisplayName("should return an error when the book is already loaned")
  public void borrowedBookCreateLoanTest() throws Exception {
    LoanDTO dto = newLoanDTO();

    String json = new ObjectMapper().writeValueAsString(dto);

    String isbn = "123";
    Book book = Book.builder().id(1).isbn(isbn).build();

    BDDMockito.given(bookService.findBookByIsbn(isbn)).willReturn(Optional.of(book));

    BDDMockito.given(loanService.save(Mockito.any(Loan.class))).willThrow(new LibraryException("Book already loaned"));

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API).accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON).content(json);

    mvc.perform(request).andExpect(MockMvcResultMatchers.status().isBadRequest())
        .andExpect(MockMvcResultMatchers.jsonPath("errors", Matchers.hasSize(1)))
        .andExpect(MockMvcResultMatchers.jsonPath("errors[0]").value("Book already loaned"));

  }

  @Test
  @DisplayName("should return a book")
  public void returnBookTest() throws Exception {

    ReturnedLoanDTO dto = ReturnedLoanDTO.builder().returned(true).build();

    Loan loan = newLoan(Book.builder().id(1).build());

    BDDMockito.given(loanService.findById(Mockito.anyInt())).willReturn(Optional.of(loan));

    String json = new ObjectMapper().writeValueAsString(dto);

    mvc.perform(MockMvcRequestBuilders.patch(LOAN_API.concat("/1")).accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(MockMvcResultMatchers.status().isOk());

    Mockito.verify(loanService, Mockito.times(1)).update(loan);
  }

  @Test
  @DisplayName("should return an error when there is not a book")
  public void returnInexistentBookTest() throws Exception {

    ReturnedLoanDTO dto = ReturnedLoanDTO.builder().returned(true).build();
    String json = new ObjectMapper().writeValueAsString(dto);

    BDDMockito.given(loanService.findById(Mockito.anyInt())).willReturn(Optional.empty());

    mvc.perform(MockMvcRequestBuilders.patch(LOAN_API.concat("/1")).accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(MockMvcResultMatchers.status().isNotFound());

  }

  @Test
  @DisplayName("should return a loan by parameters")
  public void findLoansTest() throws Exception {

    Integer id = 1;

    Loan loan = newLoan(Book.builder().id(1).isbn("123").build());
    loan.setId(id);

    BDDMockito.given(loanService.find(Mockito.any(LoanFilterDTO.class), Mockito.any(Pageable.class)))
        .willReturn(new PageImpl<Loan>(Arrays.asList(loan), PageRequest.of(0, 10), 1));

    String queryString = String.format("?isbn=%s&customer=%s&page=0&size=10", loan.getCustomer(),
        loan.getBook().getIsbn());

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(LOAN_API.concat(queryString))
        .accept(MediaType.APPLICATION_JSON);

    mvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("content", Matchers.hasSize(1)))
        .andExpect(MockMvcResultMatchers.jsonPath("totalElements").value(1))
        .andExpect((MockMvcResultMatchers.jsonPath("pageable.pageSize").value(10)))
        .andExpect((MockMvcResultMatchers.jsonPath("pageable.pageNumber")).value(0));
  }

}
