package br.com.fiap.workgroup.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.fiap.workgroup.models.User;
import br.com.fiap.workgroup.repositories.UserRepository;
import br.com.fiap.workgroup.security.PasswordUtil;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    public User createUser(User user) {
        String hashed = PasswordUtil.hashPassword(user.getPassword());
        user.setPassword(hashed);

        return userRepository.save(user);
    }

     @SuppressWarnings("null")
    public User create(User user) {
        return userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    @SuppressWarnings("null")
    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    public User update(Long id, User newUser) {
        User user = findById(id);
        user.setName(newUser.getName());
        user.setEmail(newUser.getEmail());
        return userRepository.save(user);
    }

    @SuppressWarnings("null")
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

}
