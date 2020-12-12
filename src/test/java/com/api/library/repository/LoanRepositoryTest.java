package com.api.library.repository;

import java.time.LocalDate;
import java.util.List;

import com.api.library.book.Book;
import com.api.library.loan.Loan;
import com.api.library.loan.LoanRepository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class LoanRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private LoanRepository repository;

  public Book createBook() {
    return Book.builder().isbn("123").author("Author").title("Title").build();
  }

  public Loan createLoan(Book book, LocalDate loanDate) {
    return Loan.builder().book(book).customer("Customer").loanDate(loanDate).build();
  }

  @Test
  @DisplayName("should verify if the book was not returned")
  public void existsByBookAndNotReturnedTest() {

    Loan loan = createAndPersistLoan(LocalDate.now());
    Book book = loan.getBook();

    entityManager.persist(book);
    entityManager.persist(loan);

    boolean exists = repository.existsByBookAndNotReturned(book);

    Assertions.assertThat(exists).isTrue();
  }

  @Test
  @DisplayName("should return a loan by isbn or customer")
  public void findByBookIsbnOrCustomerTest() {

    Loan loan = createAndPersistLoan(LocalDate.now());

    Page<Loan> result = repository.findByBookIsbnOrCustomer("123", "Customer", PageRequest.of(0, 10));
    Assertions.assertThat(result.getContent()).hasSize(1);
    Assertions.assertThat(result.getContent()).containsSequence(loan);
    Assertions.assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    Assertions.assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
    Assertions.assertThat(result.getTotalElements()).isEqualTo(1);
  }

  public Loan createAndPersistLoan(LocalDate loanDate) {
    Book book = createBook();
    Loan loan = createLoan(book, loanDate);

    entityManager.persist(book);
    entityManager.persist(loan);

    return loan;
  }

  @Test
  @DisplayName("should return a borrowed book lated")
  public void findLateBookTest() {

    // empréstimo atrasado persistido
    Loan loan = createAndPersistLoan(LocalDate.now().minusDays(5));

    // busca por empréstimos atrasados
    List<Loan> result = repository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));

    // deve existir um empréstimo atrasado que seja o loan definido anteriormente
    Assertions.assertThat(result).hasSize(1);
    Assertions.assertThat(result).contains(loan);
  }

  @Test
  @DisplayName("should return empty when a book is not late")
  public void findNotLateBookTest() {

    // busca por empréstimos atrasados
    List<Loan> result = repository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));

    // deve existir um empréstimo atrasado que seja o loan definido anteriormente
    Assertions.assertThat(result).isEmpty();
  }
}
