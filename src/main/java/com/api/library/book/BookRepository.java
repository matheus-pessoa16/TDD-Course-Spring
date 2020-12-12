package com.api.library.book;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {

  boolean existsByIsbn(String isbn);

  Optional<Book> findByIsbn(String isbn);

}
