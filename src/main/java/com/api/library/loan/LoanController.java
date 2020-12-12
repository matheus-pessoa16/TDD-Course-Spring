package com.api.library.loan;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.api.library.book.Book;
import com.api.library.book.BookDTO;
import com.api.library.book.BookService;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
  private final ModelMapper modelMapper;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Integer create(@RequestBody LoanDTO dto) {
    Book book = bookService.findBookByIsbn(dto.getIsbn())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book not found"));
    Loan loan = Loan.builder().book(book).customer(dto.getCustomer()).loanDate(LocalDate.now()).build();

    return loanService.save(loan).getId();
  }

  @PatchMapping("/{id}")
  public void returnBook(@PathVariable("id") Integer id, @RequestBody ReturnedLoanDTO dto) {
    Loan loan = loanService.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    loan.setReturned(dto.getReturned());

    loanService.update(loan);
  }

  @GetMapping
  public Page<LoanDTO> find(LoanFilterDTO dto, Pageable pageRequest) {
    Page<Loan> result = loanService.find(dto, pageRequest);
    List<LoanDTO> loans = result.getContent().stream().map(entity -> {
      Book book = entity.getBook();
      BookDTO bookDTO = modelMapper.map(book, BookDTO.class);
      LoanDTO loanDTO = modelMapper.map(entity, LoanDTO.class);
      loanDTO.setBook(bookDTO);
      return loanDTO;
    }).collect(Collectors.toList());
    return new PageImpl<LoanDTO>(loans, pageRequest, result.getTotalElements());
  }
}
