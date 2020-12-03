package com.api.library.book;

import java.util.Optional;

import com.api.library.exceptions.LibraryException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookService {

  @Autowired
  private BookRepository bookRepository;

  public Book save(Book book) {
    validate(book);
    return bookRepository.save(book);
  }

  public void validate(Book entity) {
    if (bookRepository.existsByIsbn(entity.getIsbn())) {
      throw new LibraryException("ISBN já cadastrado!");
    }
  }

  public Optional<Book> findById(Integer id) {
    return bookRepository.findById(id);
  }

  public void delete(Book book) {
    if (book == null || book.getId() == null) {
      throw new LibraryException("Id do livro não pode ser nulo");
    }
    bookRepository.delete(book);
  }

  public Book update(Book book) {
    if (book == null || book.getId() == null) {
      throw new LibraryException("Id do livro não pode ser nulo");
    }
    return bookRepository.save(book);
  }

}
