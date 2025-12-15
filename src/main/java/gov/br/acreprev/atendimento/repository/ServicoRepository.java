package gov.br.acreprev.atendimento.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import gov.br.acreprev.atendimento.model.Servico;

public interface ServicoRepository extends JpaRepository<Servico, Long> {

    // Para listar apenas serviços ativos
    List<Servico> findByAtivoTrueOrderByNomeAsc();

    // Verifica se existe outro serviço com o mesmo prefixo
    boolean existsByPrefixoIgnoreCase(String prefixo);

    // Útil para edição (ignora o próprio ID)
    boolean existsByPrefixoIgnoreCaseAndIdNot(String prefixo, Long id);
    
    @Query("SELECT s FROM Servico s LEFT JOIN FETCH s.subServicos WHERE s.id = :id")
    Servico buscarPorIdComSubServicos(@Param("id") Long id);

}

