package com.api.library.book;

import javax.validation.Valid;

import com.api.library.exceptions.ApiErrors;
import com.api.library.exceptions.LibraryException;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
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

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiErrors handleValidationException(MethodArgumentNotValidException ex) {
    return new ApiErrors(ex.getBindingResult());
  }

  @ExceptionHandler(LibraryException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiErrors handleLibraryException(LibraryException ex) {
    return new ApiErrors(ex);
  }

}
