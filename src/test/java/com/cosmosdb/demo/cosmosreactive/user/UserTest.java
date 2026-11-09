package com.cosmosdb.demo.cosmosreactive.user;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void shouldCreateUserWithConstructor() {
        User user = new User("1", "a@test.com", "Alice");

        assertThat(user.getId()).isEqualTo("1");
        assertThat(user.getEmail()).isEqualTo("a@test.com");
        assertThat(user.getName()).isEqualTo("Alice");
    }

    @Test
    void shouldSetUserFields() {
        User user = new User();
        user.setId("2");
        user.setEmail("b@test.com");
        user.setName("Bob");

        assertThat(user.getId()).isEqualTo("2");
        assertThat(user.getEmail()).isEqualTo("b@test.com");
        assertThat(user.getName()).isEqualTo("Bob");
    }
}
