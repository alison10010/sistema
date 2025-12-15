package gov.br.acreprev.atendimento.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sub_servico")
@Getter
@Setter
public class SubServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 255)
    private String descricao;

    @Column(nullable = false, length = 10)
    private String prefixo;

    @Column(nullable = false)
    private boolean ativo = true;    

    @Column(nullable = false)
    private boolean subSenhaNormal = true;

    @Column(nullable = false)
    private boolean subSenhaPrioritaria = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_servico", nullable = false)
    private Servico servico;
    
    // 🔹 JPA callback para forçar maiúsculo antes de salvar ou atualizar
    @PrePersist
    @PreUpdate
    private void ajustarMaiusculas() {
        if (nome != null) nome = nome.toUpperCase();
        if (prefixo != null) prefixo = prefixo.toUpperCase();
    }
}

