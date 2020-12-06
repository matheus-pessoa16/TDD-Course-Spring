package com.api.library.api;

import java.time.LocalDate;
import java.util.Optional;

import com.api.library.book.Book;
import com.api.library.book.BookService;
import com.api.library.exceptions.LibraryException;
import com.api.library.loan.Loan;
import com.api.library.loan.LoanController;
import com.api.library.loan.LoanDTO;
import com.api.library.loan.LoanService;
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
    return LoanDTO.builder().isbn("123").customer("Fulano").build();
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

    BDDMockito.given(bookService.findBookByIsnb(isbn)).willReturn(Optional.of(book));

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

    BDDMockito.given(bookService.findBookByIsnb("123")).willReturn(Optional.empty());

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API).accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON).content(json);

    mvc.perform(request).andExpect(MockMvcResultMatchers.status().isBadRequest())
        .andExpect(MockMvcResultMatchers.jsonPath("errors", Matchers.hasSize(1)))
        .andExpect(MockMvcResultMatchers.jsonPath("errors[0]").value("Book not found"));

  }

  @Test
  @DisplayName("should return an error when the book is already loaned")
  public void loanedBookCreateLoanTest() throws Exception {
    LoanDTO dto = newLoanDTO();

    String json = new ObjectMapper().writeValueAsString(dto);

    String isbn = "123";
    Book book = Book.builder().id(1).isbn(isbn).build();

    BDDMockito.given(bookService.findBookByIsnb(isbn)).willReturn(Optional.of(book));

    BDDMockito.given(loanService.save(Mockito.any(Loan.class))).willThrow(new LibraryException("Book already loaned"));

    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API).accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON).content(json);

    mvc.perform(request).andExpect(MockMvcResultMatchers.status().isBadRequest())
        .andExpect(MockMvcResultMatchers.jsonPath("errors", Matchers.hasSize(1)))
        .andExpect(MockMvcResultMatchers.jsonPath("errors[0]").value("Book already loaned"));

  }

}
