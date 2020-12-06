package com.api.library.loan;

import java.time.LocalDate;

import com.api.library.book.Book;
import com.api.library.book.BookService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/api/loan")
@RequiredArgsConstructor
public class LoanController {

  private final LoanService loanService;
  private final BookService bookService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Integer create(@RequestBody LoanDTO dto) {
    Book book = bookService.findBookByIsnb(dto.getIsbn())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book not found"));
    Loan loan = Loan.builder().book(book).customer(dto.getCustomer()).loanDate(LocalDate.now()).build();

    return loanService.save(loan).getId();
  }

}
