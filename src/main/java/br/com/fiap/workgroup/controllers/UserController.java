package br.com.fiap.workgroup.controllers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import br.com.fiap.workgroup.dtos.UserResponseDTO;
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

    // INTRO
    @GetMapping
    public ResponseEntity<EntityModel<IntroDTO>> getIntro() {
        IntroDTO introDTO = new IntroDTO("Workgroup API", "Welcome to the Workgroup API! Use the provided links to navigate through the available resources.");
        return ResponseEntity.ok(introAssembler.toModel(introDTO));
    }

    // CREATE
    @PostMapping("/create")
    public ResponseEntity<User> createUsers(@RequestBody @Valid User user) {
        User createdUser = userService.create(user);
        return ResponseEntity.ok(createdUser);
    }

    // READ - paginated
    @GetMapping("/page")
    public ResponseEntity<Page<UserResponseDTO>> getUsersPage(Pageable pageable) {
        Page<User> pageUsers = userService.findAllPageable(pageable);
        Page<UserResponseDTO> dtoPage = pageUsers.map(userService::toDTO);
        return ResponseEntity.ok(dtoPage);
    }

    // READ
    @GetMapping("/all")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<User> users = userService.findAll();
        List<UserResponseDTO> dtos = users.stream()
                .map(userService::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // READ - BY ID
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getById(@PathVariable Long id) {
        User user = userService.findById(id);
        UserResponseDTO dto = userService.toDTO(user);
        return ResponseEntity.ok(dto);
    }

    // UPDATE
    @PutMapping("/update/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody @Valid User user) {
        return ResponseEntity.ok(userService.update(id, user));
    }

    // UPDATE PROFILE IMAGE
    @PostMapping("/user/{id}/profile-image")
    public ResponseEntity<?> updateProfileImage(@PathVariable Long id,
                                                @RequestBody Map<String, String> body) {

        String base64 = body.get("imageBase64");
        if (base64 == null || base64.isBlank()) {
            return ResponseEntity.badRequest().body("Base64 da imagem ausente");
        }

        userService.updateProfileImage(id, base64);
        return ResponseEntity.ok().body("Imagem atualizada");
    }

    // DELETE
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok("User deleted successfully!");
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginDTO login) {
        User user = userService.login(login.getEmail(), login.getPassword());

        LoginResponseDTO response = new LoginResponseDTO(
            user.getId(),
            user.getName(),
            user.getEmail()
        );
        
        return ResponseEntity.ok(response);
    }

    // EMAIL EXISTS
    @GetMapping("/exists")
    public ResponseEntity<Map<String, Boolean>> emailExists(@RequestParam String email) {
        boolean exists = userService.emailExists(email);
        return ResponseEntity.ok(Map.of("exists", exists));
    }


}
