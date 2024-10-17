package ru.kuznec.fileapp.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.kuznec.fileapp.dto.UserRegistrationDTO;
import ru.kuznec.fileapp.model.User;
import ru.kuznec.fileapp.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public void blockUser(UUID userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setBlocked(true);
            userRepository.save(user);
        }
    }

    public void unblockUser(UUID userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setBlocked(false);
            userRepository.save(user);
        }
    }

    public Optional<User> findById(UUID userId) {
        return userRepository.findById(userId);
    }

    public void registerNewUser(UserRegistrationDTO registrationDto) throws Exception {
        if (userRepository.findByUsername(registrationDto.getUsername()).isPresent()) {
            throw new Exception("Пользователь с таким именем уже существует");
        }

        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setRole(User.Role.USER);

        userRepository.save(user);
    }
}
