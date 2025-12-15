package gov.br.acreprev.atendimento.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import gov.br.acreprev.atendimento.model.Tela;

public interface TelaRepository extends JpaRepository<Tela, Long> {

    // Ordenadas da mais recente para a mais antiga
	List<Tela> findAllByOrderByCreatedAtDesc();
	
	@Query("SELECT t FROM Tela t WHERE t.id = :id ")
	Tela codTela(Long id);
	
	@Query("SELECT t.layout FROM Tela t WHERE t.codigo = :codigo")
	int layout(@Param("codigo") String codigo);
	
}
