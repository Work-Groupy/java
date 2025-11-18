package br.com.fiap.workgroup.hateoas;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import br.com.fiap.workgroup.controllers.UserController;
import br.com.fiap.workgroup.dtos.IntroDTO;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class IntroAssembler implements RepresentationModelAssembler<IntroDTO, EntityModel<IntroDTO>> {

    @SuppressWarnings("null")
    @Override
    @NonNull
    public EntityModel<IntroDTO> toModel(@NonNull IntroDTO dto) {

        return EntityModel.of(dto)
            .add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("listar-usuarios"))
            .add(linkTo(methodOn(UserController.class).getById(null)).withRel("listar-usuarios-pelo-id"))
            .add(linkTo(methodOn(UserController.class).createUsers(null)).withRel("cadastrar-usuarios"))
            .add(linkTo(methodOn(UserController.class).updateUser(null, null)).withRel("atualizar-usuarios"))
            .add(linkTo(methodOn(UserController.class).deleteUser(null)).withRel("deletar-usuarios"));
    }
}
