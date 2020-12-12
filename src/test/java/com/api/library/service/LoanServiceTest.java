package com.api.library.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.api.library.book.Book;
import com.api.library.exceptions.LibraryException;
import com.api.library.loan.Loan;
import com.api.library.loan.LoanFilterDTO;
import com.api.library.loan.LoanRepository;
import com.api.library.loan.LoanService;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {

  LoanService service;

  @MockBean
  LoanRepository repository;

  @BeforeEach
  public void setup() {
    this.service = new LoanService(repository);
  }

  public Loan createLoan() {
    return Loan.builder().book(Book.builder().id(1).build()).customer("Customer").loanDate(LocalDate.now()).build();
  }

  @Test
  @DisplayName("should save a loan")
  public void saveLoanTest() {

    Loan loan = createLoan();

    Loan savedLoan = loan;
    savedLoan.setId(1);
    Mockito.when(repository.existsByBookAndNotReturned(loan.getBook())).thenReturn(false);
    Mockito.when(repository.save(loan)).thenReturn(savedLoan);

    Loan entity = service.save(loan);

    Assertions.assertThat(entity.getId()).isEqualTo(savedLoan.getId());
    Assertions.assertThat(entity.getBook()).isEqualTo(savedLoan.getBook());
    Assertions.assertThat(entity.getCustomer()).isEqualTo(savedLoan.getCustomer());
    Assertions.assertThat(entity.getLoanDate()).isEqualTo(savedLoan.getLoanDate());
  }

  @Test
  @DisplayName("should throw an error when a book has already been borrowed")
  public void borrowedBookSaveTest() {

    Loan savingLoan = createLoan();
    Mockito.when(repository.existsByBookAndNotReturned(savingLoan.getBook())).thenReturn(true);
    Throwable exception = Assertions.catchThrowable(() -> service.save(savingLoan));
    Assertions.assertThat(exception).isInstanceOf(LibraryException.class).hasMessage("Book already borrowed");
    Mockito.verify(repository, Mockito.never()).save(savingLoan);

  }

  @Test
  @DisplayName("should return a book by its id")
  public void findByIdTest() {
    Integer id = 1;
    Loan loan = createLoan();
    loan.setId(id);

    Mockito.when(repository.findById(id)).thenReturn(Optional.of(loan));

    Optional<Loan> returnedLoan = service.findById(id);

    Assertions.assertThat(returnedLoan.isPresent()).isTrue();
    Assertions.assertThat(returnedLoan.get().getCustomer()).isEqualTo(loan.getCustomer());
    Assertions.assertThat(returnedLoan.get().getLoanDate()).isEqualTo(loan.getLoanDate());
    Assertions.assertThat(returnedLoan.get().getBook()).isEqualTo(loan.getBook());
    Assertions.assertThat(returnedLoan.get().getId()).isEqualTo(loan.getId());

  }

  @Test
  @DisplayName("should return not found when there is not a loan")
  public void returnInexistentLoanTest() {
    Integer id = 1;
    Mockito.when(repository.findById(id)).thenReturn(Optional.empty());
    Optional<Loan> returnedLoan = service.findById(id);
    Assertions.assertThat(returnedLoan.isPresent()).isFalse();
  }

  @Test
  @DisplayName("should update a loan")
  public void updateLoanTest() {
    Loan loan = createLoan();
    loan.setId(1);
    loan.setReturned(true);

    Mockito.when(repository.save(loan)).thenReturn(loan);

    Loan updatedLoan = service.update(loan);

    Assertions.assertThat(updatedLoan.getReturned()).isTrue();
    Mockito.verify(repository).save(loan);
  }

  @Test
  @DisplayName("should find a loan by its properties")
  public void findLoanTest() {

    LoanFilterDTO loanFilterDTO = LoanFilterDTO.builder().customer("Customer").isbn("123").build();

    Loan loan = createLoan();

    PageRequest pageRequest = PageRequest.of(0, 10);

    List<Loan> list = Arrays.asList(loan);

    Page<Loan> page = new PageImpl<Loan>(list, pageRequest, list.size());

    Mockito.when(
        repository.findByBookIsbnOrCustomer(Mockito.anyString(), Mockito.anyString(), Mockito.any(PageRequest.class)))
        .thenReturn(page);

    Page<Loan> result = service.find(loanFilterDTO, pageRequest);

    Assertions.assertThat(result.getTotalElements()).isEqualTo(1);
    Assertions.assertThat(result.getContent()).isEqualTo(list);
    Assertions.assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    Assertions.assertThat(result.getPageable().getPageNumber()).isEqualTo(0);

  }
}
