package com.cosmosdb.demo.cosmosreactive.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Flux<UserResponse> findAll() {
        return userRepository.findAll()
                .map(UserResponse::from);
    }

    public Mono<UserResponse> findById(String id) {
        return userRepository.findById(id)
                .map(UserResponse::from);
    }

    public Mono<UserResponse> create(CreateUserRequest request) {
        return findByEmail(request.email())
                .flatMap(existingUser -> Mono.<UserResponse>error(
                        new UserAlreadyExistsException("A user with the email '%s' already exists.".formatted(request.email()))))
                .switchIfEmpty(Mono.defer(() ->
                        userRepository.save(new User(request.id(), request.email(), request.name()))
                                .map(UserResponse::from)))
                .doOnError(error -> LOGGER.error(
                        "UserService#create - Failed to persist user. id={}, email={}",
                        request.id(),
                        request.email(),
                        error
                ));
    }

    public Mono<UserResponse> update(String id, UpdateUserRequest request) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User with id '%s' was not found.".formatted(id))))
                .flatMap(existingUser -> findByEmail(request.email())
                        .flatMap(foundUser -> {
                            if (!foundUser.getId().equals(id)) {
                                return Mono.<UserResponse>error(
                                        new UserAlreadyExistsException("A user with the email '%s' already exists.".formatted(request.email())));
                            }
                            return Mono.empty();
                        })
                        .then(Mono.defer(() -> {
                            existingUser.setEmail(request.email());
                            existingUser.setName(request.name());
                            return userRepository.save(existingUser)
                                    .map(UserResponse::from);
                        })))
                .doOnError(error -> LOGGER.error(
                        "UserService#update - Failed to update user. id={}, email={}",
                        id,
                        request.email(),
                        error
                ));
    }

    public Mono<Void> delete(String id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User with id '%s' was not found.".formatted(id))))
                .flatMap(userRepository::delete)
                .doOnError(error -> LOGGER.error(
                        "UserService#delete - Failed to delete user. id={}",
                        id,
                        error
                ));
    }

    private Mono<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
