package gov.br.acreprev.atendimento.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import gov.br.acreprev.atendimento.model.Senha;
import gov.br.acreprev.atendimento.model.Servico;

public interface SenhaRepository extends JpaRepository<Senha, Long> {

	// SENHA PARA O TOTEM
    @Query("select coalesce(max(s.senha), 0) + 1 from Senha s where date(s.data) = :data and s.subServico.id = :idSubServico and s.tipo = :tipo ")
    Long obterProximoNumero(@Param("data") LocalDate data,
                            @Param("idSubServico") Long idSubServico,
                            @Param("tipo") String tipo);
    
    Optional<Senha> findFirstByTipoAndStatusAtendimentoAndDataBetweenOrderByDataAsc(
            String tipo,
            int statusAtendimento,
            LocalDateTime dataInicio,
            LocalDateTime dataFim
    );
    
    List<Senha> findByStatusAtendimentoAndDataBetweenOrderByDataAsc(
    		int statusAtendimento,
            LocalDateTime dataInicio,
            LocalDateTime dataFim
    );

    // Próxima senha aguardando PARA UM SERVIÇO específico
    Optional<Senha> findFirstBySubServicoServicoAndTipoAndStatusAtendimentoAndDataBetweenOrderByDataAsc(
            Servico servico,
            String tipo,
            int statusAtendimento,
            LocalDateTime dataInicio,
            LocalDateTime dataFim
    );

    // Fila de espera para um serviço específico
    List<Senha> findBySubServicoServicoAndStatusAtendimentoAndDataBetweenOrderByDataAsc(
            Servico servico,
            int statusAtendimento,
            LocalDateTime dataInicio,
            LocalDateTime dataFim
    );
    
    // ---------------------------------------
    @Query(value = "SELECT "
            + "            COUNT(*) FILTER (WHERE CAST(created_at AS DATE) = CURRENT_DATE)                                      AS senhas_hoje, "
            + "            COUNT(*) FILTER (WHERE date_trunc('month', created_at) = date_trunc('month', CURRENT_DATE))          AS senhas_mes, "
            + "            COUNT(*) FILTER (WHERE CAST(created_at AS DATE) = CURRENT_DATE AND status_atendimento = 1)           AS concluidos_hoje, "
            + "            COUNT(*) FILTER (WHERE CAST(created_at AS DATE) = CURRENT_DATE AND status_atendimento = 0)           AS aguardando_hoje "
            + "       FROM senha",
           nativeQuery = true)
    List<Object[]> resumoRelatorio();
    
    
    @Query(value = "SELECT "
            + "    s.nome AS nome_servico, "
            + "    COUNT(se.id) AS qtd_total, " // índice [1]
            + "    SUM(CASE WHEN se.tipo = 'N' THEN 1 ELSE 0 END) AS qtd_normal, "     // índice [2]
            + "    SUM(CASE WHEN se.tipo = 'P' THEN 1 ELSE 0 END) AS qtd_prioridade "  // índice [3]
            + "    FROM senha se JOIN servico s ON s.id = se.id_servico "
            + "    WHERE CAST(se.created_at AS DATE) = CURRENT_DATE "
            + "    GROUP BY s.nome ORDER BY qtd_total DESC; ", nativeQuery = true)
	List<Object[]> servicosNoDia();
	
	@Query(value = "SELECT "
			+ "    s.nome  AS nome_servico, "
			+ "    ss.nome AS nome_sub_servico, "
			+ "    COUNT(se.id) AS qtd_total "
			+ "	   FROM senha se "
			+ "	   JOIN servico s      ON s.id = se.id_servico "
			+ "	   JOIN sub_servico ss ON ss.id = se.id_sub_servico "
			+ "	   WHERE CAST(se.created_at AS DATE) = CURRENT_DATE "
			+ "	   GROUP BY s.nome, ss.nome "
			+ "	   ORDER BY qtd_total DESC; "
			+ "", nativeQuery = true)
	List<Object[]> subServicosNoDia();

}




