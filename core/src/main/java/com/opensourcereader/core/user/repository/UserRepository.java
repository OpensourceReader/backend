package com.opensourcereader.core.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.opensourcereader.core.user.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {}
