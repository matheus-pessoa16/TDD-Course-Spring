package com.api.library.loan;

import javax.validation.constraints.NotEmpty;

import com.api.library.book.BookDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanDTO {
  private Integer id;

  @NotEmpty
  private String isbn;

  @NotEmpty
  private String customer;

  @NotEmpty
  private String email;
  private BookDTO book;

}
