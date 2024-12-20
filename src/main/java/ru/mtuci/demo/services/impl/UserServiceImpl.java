package ru.mtuci.demo.services.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.mtuci.demo.configuration.JwtTokenProvider;
import ru.mtuci.demo.exception.UserAlreadyCreate;
import ru.mtuci.demo.exception.UserException;
import ru.mtuci.demo.model.ApplicationRole;
import ru.mtuci.demo.model.User;
import ru.mtuci.demo.repo.UserRepository;
import ru.mtuci.demo.services.UserService;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public void add(User user) {
        if (userRepository.findByName(user.getName()).isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
        }
    }

    @Override
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserException("User not found"));
    }

    @Override
    public User getByName(String name)  {
        return userRepository.findByName(name)
                .orElseThrow(() -> new UserException("User not found"));
    }

    @Override
    public User getByLogin(String login)  {
        return userRepository.findByLogin(login)
                .orElseThrow(() -> new UserException("User not found"));
    }

    @Override
    public User getUserByJwt(HttpServletRequest httpRequest) {
        String authorizationHeader = httpRequest.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new UserException("JWT токен отсутствует или некорректен");
        }

        String jwt = authorizationHeader.substring(7);
        String username = jwtTokenProvider.getUsername(jwt);
        return getByLogin(username);
    }
    @Override
    public void create(String login, String name, String password) throws UserAlreadyCreate {
        if (userRepository.findByLogin(login).isPresent()) throw new UserAlreadyCreate(login);
        var user = new User();
        user.setLogin(login);
        user.setName(name);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(ApplicationRole.USER);
        userRepository.save(user);
    }

}