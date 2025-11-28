package com.opensourcereader.core.user.service;

import com.opensourcereader.core.user.entity.User;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface UserService {

  User getUser(final Long userId);

  List<User> getUsers(final Collection<Long> userIds);
}
