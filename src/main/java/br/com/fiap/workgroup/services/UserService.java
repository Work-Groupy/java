package br.com.fiap.workgroup.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.fiap.workgroup.models.User;
import br.com.fiap.workgroup.repositories.UserRepository;
import br.com.fiap.workgroup.security.PasswordUtil;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    // Create user with hashed password
    public User createUser(User user) {
        String hashed = PasswordUtil.hashPassword(user.getPassword());
        user.setPassword(hashed);

        return userRepository.save(user);
    }

    // CRUD Operations
    // CREATE
    @SuppressWarnings("null")
    @CacheEvict(value = "users", allEntries = true)
    public User create(User user) {
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

    // UPDATE
    @CacheEvict(value = {"users", "user", "users_page"}, key = "#id", allEntries = true)
    public User update(Long id, User newUser) {
        User user = findById(id);
        user.setName(newUser.getName());
        user.setEmail(newUser.getEmail());
        return userRepository.save(user);
    }

    // DELETE
    @CacheEvict(value = {"users", "user", "users_page"}, key = "#id", allEntries = true)
    @SuppressWarnings("null")
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

}
