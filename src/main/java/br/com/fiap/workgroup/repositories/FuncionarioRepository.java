package br.com.fiap.workgroup.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.fiap.workgroup.models.Funcionario;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long>  {
}
