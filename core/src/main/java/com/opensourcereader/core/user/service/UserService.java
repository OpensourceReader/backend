package com.opensourcereader.core.user.service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.opensourcereader.core.user.entity.User;

@Service
public interface UserService {

  Optional<User> getUser(final Long userId);

  List<User> getUsers(final Collection<Long> userIds);

  Optional<User> findByEmail(final String email);
}
