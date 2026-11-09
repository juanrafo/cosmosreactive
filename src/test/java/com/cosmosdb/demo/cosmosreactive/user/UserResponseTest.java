package com.cosmosdb.demo.cosmosreactive.user;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserResponseTest {

    @Test
    void shouldMapFromUser() {
        User user = new User("1", "a@test.com", "Alice");

        UserResponse response = UserResponse.from(user);

        assertThat(response.id()).isEqualTo("1");
        assertThat(response.email()).isEqualTo("a@test.com");
        assertThat(response.name()).isEqualTo("Alice");
    }
}
