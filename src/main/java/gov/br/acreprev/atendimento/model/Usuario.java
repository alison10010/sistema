package gov.br.acreprev.atendimento.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@Entity
public class Usuario implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;
	
	@Column(name = "nome")
	private String nome;
	
	@Column(name = "username")
	private String username;

	@Column(name = "password")
	private String password;
	
	@Column(name = "email")
	private String email;
	
	@Column(name = "acesso")
	private String acesso; /* NIVEL DE ACESSO ('NORMAL','COLABORADOR','ADMIN') */
	
	@Column(name = "emai_confirmado")
	private boolean emaiConfirmado;
	
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
	    name="usuario_servico",
	    joinColumns = @JoinColumn(name = "usuario_id"),
	    inverseJoinColumns = @JoinColumn(name = "servico_id")
	)
	private List<Servico> servico = new ArrayList<>();

	
	@Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
	
	@Column(name = "data_update")
    @UpdateTimestamp
    private LocalDateTime dataUpdate;

}
