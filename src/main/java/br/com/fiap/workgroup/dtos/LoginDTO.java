package br.com.fiap.workgroup.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginDTO {

    @NotBlank(message = "O email do usuário é obrigatório")
    @Pattern(
        regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
        message = "O email deve estar em um formato válido"
    )
    private String email;

    @NotBlank(message = "A senha do usuário é obrigatória")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}\\[\\]|:;\"'<>,.?/])[A-Za-z\\d!@#$%^&*()_+\\-={}\\[\\]|:;\"'<>,.?/]{8,}$",
        message = "A senha deve ter no mínimo 8 caracteres, incluindo uma letra maiúscula, uma minúscula, um número e um caractere especial"
    )
    private String password;

}
