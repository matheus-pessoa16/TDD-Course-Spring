package com.api.library.book;

import javax.validation.constraints.NotEmpty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {

  @NotEmpty
  private String title;

  @NotEmpty
  private String author;

  @NotEmpty
  private String isbn;
  private Integer id;

}
