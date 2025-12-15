package gov.br.acreprev.atendimento.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DashboardResumoDTO {

    private Long senhasHoje;
    private Long senhasMes;
    private Long concluidosHoje;
    private Long aguardandoHoje;

    public DashboardResumoDTO() {
    }

    public DashboardResumoDTO(Long senhasHoje, Long senhasMes,
                              Long concluidosHoje, Long aguardandoHoje) {
        this.senhasHoje = senhasHoje;
        this.senhasMes = senhasMes;
        this.concluidosHoje = concluidosHoje;
        this.aguardandoHoje = aguardandoHoje;
    }
}

