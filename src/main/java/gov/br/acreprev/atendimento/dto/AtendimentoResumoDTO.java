package gov.br.acreprev.atendimento.dto;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AtendimentoResumoDTO implements Serializable {

    private String servico;
    private String subservico;

    private Long totalN;
    private Long totalP;
    private Long total;

    public AtendimentoResumoDTO(String servico, String subservico, Long totalN, Long totalP, Long total) {
        this.servico = servico;
        this.subservico = subservico;
        this.totalN = totalN;
        this.totalP = totalP;
        this.total = total;
    }
}


