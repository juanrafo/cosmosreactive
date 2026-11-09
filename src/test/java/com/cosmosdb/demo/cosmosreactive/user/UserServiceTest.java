package com.cosmosdb.demo.cosmosreactive.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    @Test
    void shouldFindAllUsers() {
        when(userRepository.findAll()).thenReturn(Flux.just(
                new User("1", "a@test.com", "Alice"),
                new User("2", "b@test.com", "Bob")
        ));

        StepVerifier.create(userService.findAll())
                .assertNext(user -> {
                    assertThat(user.id()).isEqualTo("1");
                    assertThat(user.email()).isEqualTo("a@test.com");
                    assertThat(user.name()).isEqualTo("Alice");
                })
                .assertNext(user -> {
                    assertThat(user.id()).isEqualTo("2");
                    assertThat(user.email()).isEqualTo("b@test.com");
                    assertThat(user.name()).isEqualTo("Bob");
                })
                .verifyComplete();
    }

    @Test
    void shouldFindUserById() {
        when(userRepository.findById("1")).thenReturn(Mono.just(new User("1", "a@test.com", "Alice")));

        StepVerifier.create(userService.findById("1"))
                .assertNext(user -> {
                    assertThat(user.id()).isEqualTo("1");
                    assertThat(user.email()).isEqualTo("a@test.com");
                    assertThat(user.name()).isEqualTo("Alice");
                })
                .verifyComplete();
    }

    @Test
    void shouldCreateUserWhenEmailDoesNotExist() {
        CreateUserRequest request = new CreateUserRequest("1", "a@test.com", "Alice");
        User savedUser = new User("1", "a@test.com", "Alice");
        when(userRepository.findByEmail("a@test.com")).thenReturn(Mono.empty());
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(savedUser));

        StepVerifier.create(userService.create(request))
                .assertNext(user -> {
                    assertThat(user.id()).isEqualTo("1");
                    assertThat(user.email()).isEqualTo("a@test.com");
                    assertThat(user.name()).isEqualTo("Alice");
                })
                .verifyComplete();
    }

    @Test
    void shouldFailCreateWhenEmailAlreadyExists() {
        CreateUserRequest request = new CreateUserRequest("1", "a@test.com", "Alice");
        when(userRepository.findByEmail("a@test.com")).thenReturn(Mono.just(new User("2", "a@test.com", "Other")));

        StepVerifier.create(userService.create(request))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(UserAlreadyExistsException.class);
                    assertThat(error).hasMessage("A user with the email 'a@test.com' already exists.");
                })
                .verify();

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldFailUpdateWhenUserDoesNotExist() {
        UpdateUserRequest request = new UpdateUserRequest("a@test.com", "Alice");
        when(userRepository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(userService.update("missing", request))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(UserNotFoundException.class);
                    assertThat(error).hasMessage("User with id 'missing' was not found.");
                })
                .verify();
    }

    @Test
    void shouldFailUpdateWhenEmailBelongsToAnotherUser() {
        User existing = new User("1", "old@test.com", "Old");
        UpdateUserRequest request = new UpdateUserRequest("taken@test.com", "Alice");
        when(userRepository.findById("1")).thenReturn(Mono.just(existing));
        when(userRepository.findByEmail("taken@test.com")).thenReturn(Mono.just(new User("2", "taken@test.com", "Other")));

        StepVerifier.create(userService.update("1", request))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(UserAlreadyExistsException.class);
                    assertThat(error).hasMessage("A user with the email 'taken@test.com' already exists.");
                })
                .verify();

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldUpdateUserWhenEmailBelongsToSameUser() {
        User existing = new User("1", "old@test.com", "Old");
        UpdateUserRequest request = new UpdateUserRequest("same@test.com", "Updated");
        when(userRepository.findById("1")).thenReturn(Mono.just(existing));
        when(userRepository.findByEmail("same@test.com")).thenReturn(Mono.just(new User("1", "same@test.com", "Old")));
        when(userRepository.save(existing)).thenReturn(Mono.just(existing));

        StepVerifier.create(userService.update("1", request))
                .assertNext(user -> {
                    assertThat(user.id()).isEqualTo("1");
                    assertThat(user.email()).isEqualTo("same@test.com");
                    assertThat(user.name()).isEqualTo("Updated");
                })
                .verifyComplete();
    }

    @Test
    void shouldUpdateUserWhenEmailIsNew() {
        User existing = new User("1", "old@test.com", "Old");
        UpdateUserRequest request = new UpdateUserRequest("new@test.com", "Updated");
        when(userRepository.findById("1")).thenReturn(Mono.just(existing));
        when(userRepository.findByEmail("new@test.com")).thenReturn(Mono.empty());
        when(userRepository.save(existing)).thenReturn(Mono.just(existing));

        StepVerifier.create(userService.update("1", request))
                .assertNext(user -> {
                    assertThat(user.id()).isEqualTo("1");
                    assertThat(user.email()).isEqualTo("new@test.com");
                    assertThat(user.name()).isEqualTo("Updated");
                })
                .verifyComplete();
    }

    @Test
    void shouldDeleteUserWhenExists() {
        User existing = new User("1", "a@test.com", "Alice");
        when(userRepository.findById("1")).thenReturn(Mono.just(existing));
        when(userRepository.delete(existing)).thenReturn(Mono.empty());

        StepVerifier.create(userService.delete("1"))
                .verifyComplete();

        verify(userRepository).delete(existing);
    }

    @Test
    void shouldFailDeleteWhenUserDoesNotExist() {
        when(userRepository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(userService.delete("missing"))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(UserNotFoundException.class);
                    assertThat(error).hasMessage("User with id 'missing' was not found.");
                })
                .verify();

        verify(userRepository, never()).delete(any(User.class));
    }
}
