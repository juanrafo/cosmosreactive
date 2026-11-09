package com.cosmosdb.demo.cosmosreactive.user;

public record UserResponse(String id, String email, String name) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getName());
    }
}
