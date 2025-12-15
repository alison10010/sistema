package gov.br.acreprev.atendimento.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tela")
@Getter
@Setter
public class Tela {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;        // Ex: "Recepção", "Consultório", "Laboratório"
    
    private String codigo;      // Ex: TELA_1, TELA_2

    private Boolean ativa = true;
    
    private String videoUrl;
    
    private int layout;     // 0 -> senha , 1 -> video
    
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Column(name = "data_update")
    @UpdateTimestamp
    private LocalDateTime dataUpdate;
    
    @PrePersist
    @PreUpdate
    private void ajustarMaiusculas() {
        if (nome != null) nome = nome.toUpperCase();
        if (codigo != null) codigo = codigo.toUpperCase();
    }
}
