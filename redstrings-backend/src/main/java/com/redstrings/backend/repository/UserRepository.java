package com.redstrings.backend.repository;

import com.redstrings.backend.model.User;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;

@Transactional
public interface UserRepository extends CrudRepository<User, Long> {
    User findByEmail(String email);

    User findByUsername(String username);

    User findByEmailAndAccountType(String email, String accountType);

}
