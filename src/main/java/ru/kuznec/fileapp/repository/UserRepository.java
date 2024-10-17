package ru.kuznec.fileapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kuznec.fileapp.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
}
