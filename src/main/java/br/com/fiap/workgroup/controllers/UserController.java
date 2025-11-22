package br.com.fiap.workgroup.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.workgroup.dtos.IntroDTO;
import br.com.fiap.workgroup.dtos.LoginDTO;
import br.com.fiap.workgroup.dtos.LoginResponseDTO;
import br.com.fiap.workgroup.hateoas.IntroAssembler;
import br.com.fiap.workgroup.models.User;
import br.com.fiap.workgroup.services.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserService userService;

    @Autowired
    private IntroAssembler introAssembler;

    @GetMapping
    public ResponseEntity<EntityModel<IntroDTO>> getIntro() {
        IntroDTO introDTO = new IntroDTO("Workgroup API", "Welcome to the Workgroup API! Use the provided links to navigate through the available resources.");
        return ResponseEntity.ok(introAssembler.toModel(introDTO));
    }

    @PostMapping("/create")
    public ResponseEntity<User> createUsers(@RequestBody @Valid User user) {
        User createdUser = userService.create(user);
        return ResponseEntity.ok(createdUser);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<User>> getUsersPage(Pageable pageable) {
        Page<User> page = userService.findAllPageable(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody @Valid User user) {
        return ResponseEntity.ok(userService.update(id, user));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok("User deleted successfully!");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginDTO login) {
        User user = userService.login(login.getEmail(), login.getPassword());
        LoginResponseDTO response = new LoginResponseDTO(user.getId(), user.getName(), user.getEmail());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/exists")
    public ResponseEntity<Boolean> emailExists(@RequestParam String email) {
        boolean exists = userService.emailExists(email);
        return ResponseEntity.ok(exists);
    }


}