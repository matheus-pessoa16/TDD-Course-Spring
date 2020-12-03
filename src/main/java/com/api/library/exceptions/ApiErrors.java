package com.api.library.exceptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.validation.BindingResult;

public class ApiErrors {

  private List<String> errors;

  public ApiErrors(BindingResult bindingResult) {
    this.errors = new ArrayList<>();

    bindingResult.getAllErrors().forEach(e -> this.errors.add(e.getDefaultMessage()));
  }

  public ApiErrors(LibraryException ex) {
    errors = Arrays.asList(ex.getMessage());
  }

  public List<String> getErrors() {
    return errors;
  }

  public void setErrors(List<String> errors) {
    this.errors = errors;
  }

}
