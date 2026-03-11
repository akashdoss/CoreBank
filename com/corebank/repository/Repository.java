package com.corebank.repository;

import java.util.List;
import java.util.Optional;

/**
 * Generic Repository interface for CRUD operations.
 * Demonstrates Java Generics.
 */
public interface Repository<T, ID> {
    T save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    void deleteById(ID id);
}
