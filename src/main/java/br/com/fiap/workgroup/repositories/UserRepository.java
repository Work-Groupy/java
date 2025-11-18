package br.com.fiap.workgroup.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.fiap.workgroup.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
