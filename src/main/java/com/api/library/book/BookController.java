package com.api.library.book;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/book")
public class BookController {

  @Autowired
  private BookService bookService;

  @Autowired
  private ModelMapper modelMapper;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public BookDTO create(@RequestBody @Valid BookDTO dto) {
    Book book = modelMapper.map(dto, Book.class);
    book = bookService.save(book);
    return modelMapper.map(book, BookDTO.class);
  }

  @GetMapping("{id}")
  @ResponseStatus(HttpStatus.OK)
  public BookDTO findById(@PathVariable("id") Integer id) {
    return bookService.findById(id).map(book -> modelMapper.map(book, BookDTO.class))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }

  @DeleteMapping("{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable("id") Integer id) {
    Book book = bookService.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    bookService.delete(book);
  }

  @PutMapping("{id}")
  public BookDTO update(@PathVariable Integer id, BookDTO dto) {

    return bookService.findById(id).map(book -> {

      book.setAuthor(dto.getAuthor());
      book.setTitle(dto.getTitle());
      book = bookService.update(book);
      return modelMapper.map(book, BookDTO.class);

    }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

  }

  @GetMapping
  public Page<BookDTO> findBooksByQuery(BookDTO dto, Pageable pageRequest) {
    Book book = modelMapper.map(dto, Book.class);
    Page<Book> result = bookService.find(book, pageRequest);

    List<BookDTO> response = result.getContent().stream().map(b -> modelMapper.map(b, BookDTO.class))
        .collect(Collectors.toList());

    return new PageImpl<BookDTO>(response, pageRequest, result.getTotalElements());
  }

}
