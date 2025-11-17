package br.com.fiap.workgroup.models;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Entity
@Table(name = "T_WG_USER")
@Data
public class User {

    @Id
    @Column(name = "ID_USER")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id_user;

    @Column(name = "NM_USER")
    @NotBlank(message = "O nome do usuário é obrigatório")
    private String name;

    @Column(name = "ID_EMAIL")
    @NotBlank(message = "O email do usuário é obrigatório")
    @Pattern(
        regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
        message = "O email deve estar em um formato válido"
    )
    private String email;

    @Column(name = "CD_SENHA")
    @NotBlank(message = "A senha do usuário é obrigatória")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}\\[\\]|:;\"'<>,.?/])[A-Za-z\\d!@#$%^&*()_+\\-={}\\[\\]|:;\"'<>,.?/]{8,}$",
        message = "A senha deve ter no mínimo 8 caracteres, incluindo uma letra maiúscula, uma minúscula, um número e um caractere especial"
    )
    private String password;

    @Column(name = "DT_CRIADA")
    @NotNull(message = "A data de criação não pode ser nula")
    private LocalDate created_at;

    @Lob
    @Column(name = "IMG_PERFIL")
    private byte[] profile;

    @Lob
    @Column(name = "IMG_CURRICULO")
    private byte[] resume;

    @Column(name = "DS_BIOGRAFIA", length = 100)
    private String bio;
}
