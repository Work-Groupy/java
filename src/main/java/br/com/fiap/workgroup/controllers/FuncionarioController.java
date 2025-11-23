package br.com.fiap.workgroup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import br.com.fiap.workgroup.models.Funcionario;
import br.com.fiap.workgroup.repositories.FuncionarioRepository;

import java.util.List;

@RestController
@RequestMapping("/funcionarios")
public class FuncionarioController {

    @Autowired
    private FuncionarioRepository repository;

    @GetMapping
    public List<Funcionario> listar() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Funcionario buscar(@PathVariable Long id) {
        return repository.findById(id)
                         .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));
    }

    @PostMapping
    public Funcionario criar(@RequestBody Funcionario funcionario) {
        return repository.save(funcionario);
    }

    @PutMapping("/{id}")
    public Funcionario atualizar(@PathVariable Long id, @RequestBody Funcionario f) {
        return repository.findById(id).map(funcionario -> {
            funcionario.setNome(f.getNome());
            funcionario.setCargo(f.getCargo());
            funcionario.setSalario(f.getSalario());
            return repository.save(funcionario);
        }).orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
