package com.cosmosdb.demo.cosmosreactive.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController(userService);
    }

    @Test
    void shouldReturnAllUsers() {
        when(userService.findAll()).thenReturn(Flux.just(
                new UserResponse("1", "a@test.com", "Alice"),
                new UserResponse("2", "b@test.com", "Bob")
        ));

        StepVerifier.create(userController.getAllUsers())
                .assertNext(user -> assertThat(user.id()).isEqualTo("1"))
                .assertNext(user -> assertThat(user.id()).isEqualTo("2"))
                .verifyComplete();
    }

    @Test
    void shouldReturnUserByIdWhenExists() {
        UserResponse response = new UserResponse("1", "a@test.com", "Alice");
        when(userService.findById("1")).thenReturn(Mono.just(response));

        StepVerifier.create(userController.getUserById("1"))
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(entity.getBody()).isEqualTo(response);
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnNotFoundWhenUserByIdDoesNotExist() {
        when(userService.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(userController.getUserById("missing"))
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(entity.getBody()).isNull();
                })
                .verifyComplete();
    }

    @Test
    void shouldCreateUser() {
        CreateUserRequest request = new CreateUserRequest("1", "a@test.com", "Alice");
        UserResponse response = new UserResponse("1", "a@test.com", "Alice");
        when(userService.create(request)).thenReturn(Mono.just(response));

        StepVerifier.create(userController.createUser(request))
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void shouldUpdateUser() {
        UpdateUserRequest request = new UpdateUserRequest("a@test.com", "Alice");
        UserResponse response = new UserResponse("1", "a@test.com", "Alice");
        when(userService.update("1", request)).thenReturn(Mono.just(response));

        StepVerifier.create(userController.updateUser("1", request))
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(entity.getBody()).isEqualTo(response);
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnNotFoundWhenUpdateUserDoesNotExist() {
        UpdateUserRequest request = new UpdateUserRequest("a@test.com", "Alice");
        when(userService.update("missing", request))
                .thenReturn(Mono.error(new UserNotFoundException("not found")));

        StepVerifier.create(userController.updateUser("missing", request))
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(entity.getBody()).isNull();
                })
                .verifyComplete();
    }

    @Test
    void shouldDeleteUser() {
        when(userService.delete("1")).thenReturn(Mono.empty());

        StepVerifier.create(userController.deleteUser("1"))
                .assertNext(entity -> assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT))
                .verifyComplete();
    }

    @Test
    void shouldReturnNotFoundWhenDeleteUserDoesNotExist() {
        when(userService.delete("missing"))
                .thenReturn(Mono.error(new UserNotFoundException("not found")));

        StepVerifier.create(userController.deleteUser("missing"))
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(entity.getBody()).isNull();
                })
                .verifyComplete();
    }
}
