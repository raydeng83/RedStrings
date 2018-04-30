package com.redstrings.backend.repository;

import com.redstrings.backend.model.Session;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;

@Transactional
public interface SessionRepository extends CrudRepository<Session, Long> {
    Session findByJsessionId(String jsessionId);
}
