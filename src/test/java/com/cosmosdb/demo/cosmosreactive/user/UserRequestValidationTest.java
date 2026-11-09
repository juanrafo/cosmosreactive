package com.cosmosdb.demo.cosmosreactive.user;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldValidateCreateUserRequestWhenValid() {
        CreateUserRequest request = new CreateUserRequest("1", "a@test.com", "Alice");

        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldValidateCreateUserRequestWhenInvalid() {
        CreateUserRequest request = new CreateUserRequest("", "invalid-email", "");

        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder(
                        "Id is required.",
                        "Email must be valid.",
                        "Name is required."
                );
    }

    @Test
    void shouldValidateUpdateUserRequestWhenValid() {
        UpdateUserRequest request = new UpdateUserRequest("a@test.com", "Alice");

        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldValidateUpdateUserRequestWhenInvalid() {
        UpdateUserRequest request = new UpdateUserRequest("invalid-email", "");

        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder(
                        "Email must be valid.",
                        "Name is required."
                );
    }
}
