package com.cosmosdb.demo.cosmosreactive.user;

import org.junit.jupiter.api.Test;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryContractTest {

    @Test
    void shouldExtendReactiveCrudRepository() {
        assertThat(ReactiveCrudRepository.class.isAssignableFrom(UserRepository.class)).isTrue();
    }

    @Test
    void shouldDeclareFindByEmailMethod() throws NoSuchMethodException {
        Method method = UserRepository.class.getMethod("findByEmail", String.class);

        assertThat(method.getReturnType()).isEqualTo(Mono.class);
    }
}
