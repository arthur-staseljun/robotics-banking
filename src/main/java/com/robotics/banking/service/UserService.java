package com.robotics.banking.service;

import com.robotics.banking.dto.request.CreateUserRequest;
import com.robotics.banking.dto.response.UserResponse;
import com.robotics.banking.model.User;
import com.robotics.banking.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        User user = new User(request.name(), request.email());
        return toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse get(Long id) {
        return toResponse(getEntity(id));
    }

    User getEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User " + id + " not found"));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }
}
