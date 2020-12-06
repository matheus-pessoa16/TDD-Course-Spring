package com.api.library.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApplicationControllerAdvice {

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

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ApiErrors> handleResponseEntity(ResponseStatusException ex) {
    return new ResponseEntity<ApiErrors>(new ApiErrors(ex), ex.getStatus());
  }
}
