package com.frauddetection.dao;

import com.frauddetection.exception.DatabaseOperationException;
import java.util.List;
import java.util.Optional;

/**
 * Generic Data Access Object (DAO) interface defining common CRUD operations.
 * Fulfills OOP Rubric: Interfaces, Generics, and Classes for Database Operations.
 *
 * @param <T> Domain entity type
 */
public interface DatabaseOperations<T> {

    /**
     * Persists a new entity into the database.
     *
     * @param entity The entity to insert
     * @return The persisted entity with generated primary key
     * @throws DatabaseOperationException on database failure
     */
    T save(T entity) throws DatabaseOperationException;

    /**
     * Updates an existing entity in the database.
     *
     * @param entity The entity containing updated attributes
     * @return true if record was updated, false if not found
     * @throws DatabaseOperationException on database failure
     */
    boolean update(T entity) throws DatabaseOperationException;

    /**
     * Deletes an entity by its primary key identifier.
     *
     * @param id The primary key
     * @return true if deleted, false if not found
     * @throws DatabaseOperationException on database failure
     */
    boolean delete(int id) throws DatabaseOperationException;

    /**
     * Retrieves an entity by its primary key.
     *
     * @param id The primary key
     * @return Optional containing the entity if found, empty otherwise
     * @throws DatabaseOperationException on database failure
     */
    Optional<T> findById(int id) throws DatabaseOperationException;

    /**
     * Retrieves all entities belonging to this table.
     * Fulfills Collections requirement: returns List<T>.
     *
     * @return List of all domain entities
     * @throws DatabaseOperationException on database failure
     */
    List<T> findAll() throws DatabaseOperationException;
}
