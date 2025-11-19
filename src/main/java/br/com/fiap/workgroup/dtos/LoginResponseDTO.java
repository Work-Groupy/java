package br.com.fiap.workgroup.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDTO {

    private Long id;
    private String name;
    private String email;
    
}
