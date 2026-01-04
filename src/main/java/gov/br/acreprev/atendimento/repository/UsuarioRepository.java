package gov.br.acreprev.atendimento.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import gov.br.acreprev.atendimento.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
	
	@EntityGraph(attributePaths = "servico")
    Optional<Usuario> findByUsername(String username);

}
