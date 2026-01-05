package gov.br.acreprev.atendimento.controller;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.context.annotation.SessionScope;

import gov.br.acreprev.atendimento.model.Servico;
import gov.br.acreprev.atendimento.model.Usuario;
import gov.br.acreprev.atendimento.repository.ServicoRepository;
import gov.br.acreprev.atendimento.repository.UsuarioRepository;
import gov.br.acreprev.atendimento.util.EmailSenha;
import gov.br.acreprev.atendimento.util.Ferramentas;
import gov.br.acreprev.atendimento.util.Mensagens;
import lombok.Getter;
import lombok.Setter;

@Named
@Getter
@Setter
@Controller
@SessionScope
public class UsuarioController implements Serializable {

    private static final long serialVersionUID = 1L;

    private Usuario usuario = new Usuario();
    
    private Usuario usuarioSelecionado;
    private String novaSenha;

    private List<Usuario> listaUsuario;
    private List<Servico> listaServicos;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ServicoRepository servicoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        listaServicos = servicoRepository.findByAtivoTrueOrderByNomeAsc();
        listaUsuario  = usuarioRepository.findAll();
    }

    /****** Salva Usuario ******/
    public void insertUser() {
        try {

            // Verifica se já existe usuário com o mesmo username
            Optional<Usuario> existente = usuarioRepository.findByUsername(usuario.getEmail().toLowerCase());
            if (existente.isPresent()) {
                Mensagens.aviso("Usuário já existe!", "");
                return;
            }

            // Garante que tenha pelo menos 1 serviço
            if (usuario.getServico() == null || usuario.getServico().isEmpty()) {
                Mensagens.aviso("Selecione ao menos um serviço para o usuário.", "");
                return;
            }            
            
            usuario.setAcesso("NORMAL");
            usuario.setEmaiConfirmado(true);
            
            int senhaAleatoria = Ferramentas.randomDeDataHora();
			String senhaEmString = String.valueOf(senhaAleatoria);
			
            EmailSenha emailSenha = new EmailSenha();
			String mail = usuario.getEmail();
			
            // Codifica a senha antes de salvar
			usuario.setPassword(passwordEncoder.encode(senhaEmString));
			
			 // envia Mensagem Assincrona sem esperar pela confirmação do envio do e-mail
	        new Thread(() -> {
	        	emailSenha.enviaMensagem(mail, senhaEmString);
	        }).start();
            
            usuario.setUsername(usuario.getEmail());

            usuarioRepository.save(usuario);

            usuario = new Usuario(); // limpa o form

            Mensagens.info("Cadastro realizado!", "");

        } catch (Exception e) {
            e.printStackTrace();
            Mensagens.erro("Erro ao realizar o cadastro!", "");
        }
    }    
    
    public void recarregar() {
        listaUsuario = usuarioRepository.findAll();
    }

    public void abrirEditar(Usuario u) {
        if (u == null || u.getId() == null) {
            this.usuarioSelecionado = null;
            return;
        }

        // Recarrega do banco garantindo estado atual
        this.usuarioSelecionado = usuarioRepository.findById(u.getId()).orElse(null);
        this.novaSenha = null;
    }


    public void salvarEdicao() {
        if (usuarioSelecionado == null) return;

        // Se informou nova senha, criptografa
        if (novaSenha != null && !novaSenha.trim().isEmpty()) {
            usuarioSelecionado.setPassword(passwordEncoder.encode(novaSenha.trim()));
        }

        usuarioRepository.save(usuarioSelecionado);
        recarregar();

        Mensagens.info("Usuário atualizado.", ""); 
    }

    public void abrirRemover(Usuario u) {
        this.usuarioSelecionado = usuarioRepository.findById(u.getId())
            .orElseThrow(() ->
                new IllegalArgumentException("Usuário não encontrado: " + u.getId())
            );
    }
    
    public void confirmarRemover() {
        if (usuarioSelecionado == null) return;

        try {
            UUID id = usuarioSelecionado.getId();

            Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Id inválido: " + id));

            usuarioRepository.delete(usuario);

            usuarioSelecionado = null;
            recarregar();
            Mensagens.info("Usuário removido com sucesso.", "");

        } catch (Exception e) {
            Mensagens.erro("Não foi possível remover. Tente novamente", "");
        }
    }

    // Rotas    
    @GetMapping("/usuario/cadastro")
	public String cadastro() {		
    	listaServicos = servicoRepository.findByAtivoTrueOrderByNomeAsc();
        listaUsuario  = usuarioRepository.findAll();
       this.usuario = new Usuario();
		return "usuario/cadastro";
	}
    
    @GetMapping("/usuario/usuarios")
   	public String lista() {		
       	listaServicos = servicoRepository.findByAtivoTrueOrderByNomeAsc();
           listaUsuario  = usuarioRepository.findAll();
   		return "usuario/lista-user";
   	}

}

