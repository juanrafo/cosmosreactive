package com.cosmosdb.demo.cosmosreactive.config;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.ReactiveCosmosTemplate;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.core.mapping.CosmosMappingContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CosmosReactiveConfiguration {

    @Bean
    public CosmosAsyncClient cosmosAsyncClient(
            @Value("${azure.cosmos.uri}") String uri,
            @Value("${azure.cosmos.key}") String key) {
        return new CosmosClientBuilder()
                .endpoint(uri)
                .key(key)
                .buildAsyncClient();
    }

    @Bean
    public CosmosMappingContext cosmosMappingContext() {
        return new CosmosMappingContext();
    }

    @Bean
    public ObjectMapper cosmosObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return mapper;
    }

    @Bean(name = "reactiveCosmosTemplate")
    public ReactiveCosmosTemplate reactiveCosmosTemplate(
            CosmosAsyncClient cosmosAsyncClient,
            CosmosMappingContext cosmosMappingContext,
            ObjectMapper cosmosObjectMapper,
            @Value("${azure.cosmos.database}") String databaseName) {

        MappingCosmosConverter converter =
                new MappingCosmosConverter(cosmosMappingContext, cosmosObjectMapper);

        return new ReactiveCosmosTemplate(
                cosmosAsyncClient,
                databaseName,
                CosmosConfig.builder().build(),
                converter
        );
    }
}
