package com.cookingapp.domain.repository;

import com.cookingapp.domain.model.User;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findByEmail(String email);

    Optional<User> findById(String userId);

    User save(User user);
}
