package br.com.fiap.workgroup.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import br.com.fiap.workgroup.models.User;
import br.com.fiap.workgroup.repositories.UserRepository;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // CREATE
    @CacheEvict(value = {"users","users_page"}, allEntries = true)
    public User create(User user) {
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            String hashedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(hashedPassword);
        }
        return userRepository.save(user);
    }

    // READ all with pagination
    @SuppressWarnings("null")
    @Cacheable(value = "users_page")
    public Page<User> findAllPageable(Pageable pageable) {
        return userRepository.findAll(pageable);
    }   

    // READ all
    @Cacheable(value = "users")
    public List<User> findAll() {
        return userRepository.findAll();
    }

    // READ by ID
    @SuppressWarnings("null")
    @Cacheable(value = "user", key = "#id")
    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    // UPDATE seletivo
    @SuppressWarnings("null")
    @CacheEvict(value = {"users", "user", "users_page"}, key = "#id", allEntries = true)
    public User update(Long id, User newUser) {
        User user = findById(id);

        if (newUser.getName() != null) user.setName(newUser.getName());
        if (newUser.getEmail() != null) user.setEmail(newUser.getEmail());
        if (newUser.getBio() != null) user.setBio(newUser.getBio());

        if (newUser.getPassword() != null && !newUser.getPassword().isBlank()) {
            String hashedPassword = passwordEncoder.encode(newUser.getPassword());
            user.setPassword(hashedPassword);
        }

        if (newUser.getProfile() != null && newUser.getProfile().length > 0) {
            user.setProfile(newUser.getProfile());
        }
        if (newUser.getResume() != null && newUser.getResume().length > 0) {
            user.setResume(newUser.getResume());
        }

        return userRepository.save(user);
    }

    // DELETE
    @CacheEvict(value = {"users", "user", "users_page"}, key = "#id", allEntries = true)
    @SuppressWarnings("null")
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    // LOGIN
    public User login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Email not found"));

        boolean passwordMatch = passwordEncoder.matches(rawPassword, user.getPassword());

        if (!passwordMatch) {
            throw new RuntimeException("Invalid password");
        }

        return user;
    }

    // CHECK EMAIL EXISTS
    public boolean emailExists(String email) {
        return userRepository.existsByEmailIgnoreCase(email);
    }


}