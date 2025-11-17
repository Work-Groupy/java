package br.com.fiap.workgroup.services;

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

}
