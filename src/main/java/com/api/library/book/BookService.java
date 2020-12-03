package com.api.library.book;

import java.util.Optional;

import com.api.library.exceptions.LibraryException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

  public Page<Book> find(Book filter, Pageable pageRequest) {
    Example<Book> example = Example.of(filter, ExampleMatcher.matching().withIgnoreCase().withIgnoreNullValues()
        .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING));
    return bookRepository.findAll(example, pageRequest);
  }

}
