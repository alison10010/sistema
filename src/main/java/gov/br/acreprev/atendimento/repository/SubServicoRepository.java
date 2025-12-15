package gov.br.acreprev.atendimento.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import gov.br.acreprev.atendimento.model.SubServico;

public interface SubServicoRepository extends JpaRepository<SubServico, Long> {

    List<SubServico> findByServicoIdOrderByIdAsc(Long servicoId);
}
