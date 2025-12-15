package gov.br.acreprev.atendimento.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import gov.br.acreprev.atendimento.model.Atendimento;

public interface AtendimentoRepository extends JpaRepository<Atendimento, Long> {

    // últimas senhas chamadas, ordenadas da mais recente para a mais antiga
   
    
    @Query("SELECT a FROM Atendimento a WHERE DATE(a.data) = CURRENT_DATE order by a.id desc")
    List<Atendimento> senhasHoje();
    
    @Query(value = "SELECT * FROM atendimento WHERE tela = :codigo ORDER BY id DESC LIMIT 6", nativeQuery = true)
    List<Atendimento> ultimos6Atendimento(@Param("codigo") String codigo);


}
