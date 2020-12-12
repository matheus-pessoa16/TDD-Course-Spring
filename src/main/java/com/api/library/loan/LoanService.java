package com.api.library.loan;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.api.library.book.Book;
import com.api.library.exceptions.LibraryException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class LoanService {

  private LoanRepository loanRepository;

  public LoanService(LoanRepository loanRepository) {

    this.loanRepository = loanRepository;

  }

  public Loan save(Loan loan) {

    if (loanRepository.existsByBookAndNotReturned(loan.getBook())) {
      throw new LibraryException("Book already borrowed");
    }

    return loanRepository.save(loan);
  }

  public Optional<Loan> findById(Integer id) {
    return loanRepository.findById(id);
  }

  public Loan update(Loan loan) {
    return loanRepository.save(loan);
  }

  public Page<Loan> find(LoanFilterDTO loanFilter, Pageable pageRequest) {
    return loanRepository.findByBookIsbnOrCustomer(loanFilter.getIsbn(), loanFilter.getCustomer(), pageRequest);
  }

  public Page<Loan> findLoansByBook(Book book, Pageable pageable) {
    return loanRepository.findByBook(book, pageable);
  }

  public List<Loan> findAllLateLoands() {
    final int loanDays = 4;
    LocalDate threeDaysAgo = LocalDate.now().minusDays(loanDays);
    return loanRepository.findByLoanDateLessThanAndNotReturned(threeDaysAgo);
  }

}
