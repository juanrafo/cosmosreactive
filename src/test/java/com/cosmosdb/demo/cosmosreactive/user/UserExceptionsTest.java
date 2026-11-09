package com.cosmosdb.demo.cosmosreactive.user;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserExceptionsTest {

    @Test
    void shouldCreateUserAlreadyExistsExceptionWithMessage() {
        UserAlreadyExistsException exception = new UserAlreadyExistsException("already exists");

        assertThat(exception).hasMessage("already exists");
    }

    @Test
    void shouldCreateUserNotFoundExceptionWithMessage() {
        UserNotFoundException exception = new UserNotFoundException("not found");

        assertThat(exception).hasMessage("not found");
    }
}
