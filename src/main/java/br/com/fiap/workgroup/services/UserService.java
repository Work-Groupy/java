package br.com.fiap.workgroup.services;

import java.util.List;
import java.util.Base64;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import br.com.fiap.workgroup.dtos.UserResponseDTO;
import br.com.fiap.workgroup.models.User;
import br.com.fiap.workgroup.repositories.UserRepository;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // CRUD Operations
    // CREATE
    @SuppressWarnings("null")
    @CacheEvict(value = "users", allEntries = true)
    public User create(User user) {
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);
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

    // EMAIL EXISTS
    public boolean emailExists(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return userRepository.existsByEmailIgnoreCase(email.trim());
    }

    // UPDATE PROFILE IMAGE
    public void updateProfileImage(Long id, String base64) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (base64 == null || base64.trim().isEmpty()) {
            user.setProfile(null);
            userRepository.save(user);
            return;
        }

        byte[] imageBytes;
        try {
            imageBytes = Base64.getDecoder().decode(base64);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Invalid image data", ex);
        }

        user.setProfile(imageBytes);
        userRepository.save(user);
    }

    // Convert User to UserResponseDTO
    public UserResponseDTO toDTO(User user) {
        String imageBase64 = null;

        if (user.getProfile() != null) {
            imageBase64 = Base64.getEncoder().encodeToString(user.getProfile());
        }

        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setProfileBase64(imageBase64);

        return dto;
    }

}
