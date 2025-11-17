package br.com.fiap.workgroup.hateoas;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import br.com.fiap.workgroup.controllers.UserController;
import br.com.fiap.workgroup.dtos.IntroDTO;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class IntroAssembler implements RepresentationModelAssembler<IntroDTO, EntityModel<IntroDTO>> {

    @Override
    public EntityModel<IntroDTO> toModel(IntroDTO entity) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toModel'");
    }

    // @Override
    // public EntityModel<IntroDTO> toModel(IntroDTO dto) {

    //     return EntityModel.of(dto)
    //         .add(linkTo(methodOn(UserController.class).pegueTodos()).withRel("listar-usuarios"))
    //         .add(linkTo(methodOn(UserController.class).pegarPeloCpf(null)).withRel("listar-usuarios-pelo-cpf"))
    //         .add(linkTo(methodOn(UserController.class).cadastro(null)).withRel("cadastrar-usuarios"))
    //         .add(linkTo(methodOn(UserController.class).atualizar(null, null)).withRel("atualizar-usuarios"))
    //         .add(linkTo(methodOn(UserController.class).deletar(null)).withRel("deletar-usuarios"));
    // }
}
