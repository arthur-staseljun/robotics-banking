package com.robotics.banking.persistence;

import com.robotics.banking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
