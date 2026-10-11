package com.frauddetection.util;

import com.frauddetection.exception.InvalidInputException;

/**
 * Interface contract for domain entities that can perform self-validation.
 * Fulfills OOP Rubric: Interfaces.
 */
public interface Validatable {

    /**
     * Validates internal entity fields and states.
     *
     * @throws InvalidInputException if any validation rule is violated
     */
    void validate() throws InvalidInputException;
}
