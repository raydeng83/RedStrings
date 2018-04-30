package com.redstrings.backend.repository;

import com.redstrings.backend.model.Role;
import org.springframework.data.repository.CrudRepository;

public interface RoleRepository extends CrudRepository<Role, Long> {
    Role findByName(String roleName);
}
