package org.bootstrapbugz.api.shared.generic.crud;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

public abstract class CrudController<T, U> {
  private final CrudService<T, U> service;

  protected CrudController(CrudService<T, U> service) {
    this.service = service;
  }

  @PostMapping
  public ResponseEntity<T> create(@Valid @RequestBody U saveRequest) {
    return new ResponseEntity<>(service.create(saveRequest), HttpStatus.CREATED);
  }

  @GetMapping
  public ResponseEntity<List<T>> findAll() {
    return ResponseEntity.ok(service.findAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<T> findById(@PathVariable("id") Long id) {
    return ResponseEntity.ok(service.findById(id));
  }

  @PutMapping("/{id}")
  public ResponseEntity<T> update(@PathVariable("id") Long id, @Valid @RequestBody U saveRequest) {
    return ResponseEntity.ok(service.update(id, saveRequest));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }
}
