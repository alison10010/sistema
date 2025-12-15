package gov.br.acreprev.atendimento.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "atendimento")
@Getter
@Setter
public class Atendimento implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Data/hora em que a senha foi chamada no guichê
    @CreationTimestamp
    private LocalDateTime data;

    @Column(name = "guiche", nullable = false)
    private Integer guiche;

    // Número da senha (ex: 1, 2, 3...)
    @Column(name = "senha", nullable = false)
    private Integer senha;

    // Tipo da senha: 'N' (normal) ou 'P' (prioridade)
    @Column(name = "tipo", length = 1, nullable = false)
    private String tipo;

    // Opcional – caso você queira gravar o nome do paciente também
    @Column(name = "paciente")
    private String paciente;
    
    @Column(name = "senhaVisual")
    private String senhaVisual;
    
    @Column(name = "comparecer")
    private String comparecer;
    
    @Column(name = "servico")
    private String servico;
    
    @Column(name = "subServico")
    private String subservico;
    
    @Column(name = "tela")
    private String tela;
    
    //  Construtor padrão exigido pelo JPA/Hibernate
    protected Atendimento() {
    }
    
    public Atendimento(LocalDateTime data, Integer guiche, Integer senha, String tipo, String senhaVisual, String comparecer, String subservico, String servico, String tela) {
        this.data = data;
        this.guiche = guiche;
        this.senha = senha;
        this.tipo = tipo;
        this.senhaVisual = senhaVisual;
        this.comparecer = comparecer;
        this.subservico = subservico;
        this.servico = servico;
        this.tela = tela;
    }
}
