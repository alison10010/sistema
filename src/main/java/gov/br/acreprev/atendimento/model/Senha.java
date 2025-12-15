package gov.br.acreprev.atendimento.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "senha")
@Getter
@Setter
public class Senha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nome do paciente (se houver no fluxo)
    @Column(name = "paciente")
    private String paciente;

    // Número sequencial da senha (001, 002...)
    @Column(name = "senha", nullable = false)
    private Integer senha;

    // Status: 0 = Aguardando, 1 = Atendido
    @Column(name = "status_atendimento", length = 1, nullable = false)
    private Integer statusAtendimento;

    // Tipo de senha: N = Normal, P = Prioritária
    @Column(name = "tipo", length = 1, nullable = false)
    private String tipo;

    // Relacionamento com Serviço principal
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_servico", nullable = false)
    private Servico servico;

    // Relacionamento com Sub-serviço
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_sub_servico", nullable = false)
    private SubServico subServico;
    
    @Column(name = "senhaVisual")
    private String senhaVisual;
    
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime data;
    
    @Column(name = "data_update")
    @UpdateTimestamp
    private LocalDateTime dataUpdate;
}
