package gov.br.acreprev.atendimento.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import gov.br.acreprev.atendimento.dto.AtendimentoResumoDTO;
import gov.br.acreprev.atendimento.model.Atendimento;

public interface AtendimentoRepository extends JpaRepository<Atendimento, Long> {

    @Query("SELECT a FROM Atendimento a WHERE DATE(a.data) = CURRENT_DATE order by a.id desc")
    List<Atendimento> senhasHoje();

    @Query(value = "SELECT * FROM atendimento WHERE tela = :codigo ORDER BY id DESC LIMIT 6", nativeQuery = true)
    List<Atendimento> ultimos6Atendimento(@Param("codigo") String codigo);

    List<Atendimento> findByDataBetweenOrderByDataAsc(LocalDateTime inicio, LocalDateTime fim);

    @Query(
        "select new gov.br.acreprev.atendimento.dto.AtendimentoResumoDTO(" +
        "  coalesce(a.servico,'-'), " +
        "  coalesce(a.subservico,'-'), " +
        "  sum(case when a.tipo = 'N' then 1 else 0 end), " +
        "  sum(case when a.tipo = 'P' then 1 else 0 end), " +
        "  count(a.id) " +
        ") " +
        "from Atendimento a " +
        "where a.data between :ini and :fim " +
        "group by a.servico, a.subservico " +
        "order by a.servico asc, a.subservico asc"
    )
    List<AtendimentoResumoDTO> resumoPorServicoESubNP(
            @Param("ini") LocalDateTime ini,
            @Param("fim") LocalDateTime fim
    );
}