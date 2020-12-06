package com.api.library.loan;

import java.time.LocalDate;

import com.api.library.book.Book;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Loan {

  private Integer id;
  private String customer;
  private Book book;
  private LocalDate loanDate;
  private Boolean returned;
}
