package gov.br.acreprev.atendimento.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Table(name = "servico")
@Entity
@Getter @Setter
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 5)
    private String prefixo;

    @Column(nullable = false)
    private boolean ativo = true;
    
    @Column(nullable = false, length = 120)
    private String comparecer;

    @OneToMany(
            mappedBy = "servico",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    @OrderBy("id ASC")
    private List<SubServico> subServicos = new ArrayList<>();
    
    @Column(name = "tela_id")
    private Long tela;


    // Método helper para manter os dois lados da relação
    public void adicionarSubServico(SubServico sub) {
        sub.setServico(this);
        this.subServicos.add(sub);
    }

    public void removerSubServico(SubServico sub) {
        sub.setServico(null);
        this.subServicos.remove(sub);
    }
    
    // 🔹 JPA callback para forçar maiúsculo antes de salvar ou atualizar
    @PrePersist
    @PreUpdate
    private void ajustarMaiusculas() {
        if (nome != null) nome = nome.toUpperCase();
        if (prefixo != null) prefixo = prefixo.toUpperCase();
    }
}
