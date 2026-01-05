package gov.br.acreprev.atendimento.controller;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
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
    
    // Rotas
    
    @GetMapping("/usuario/cadastro")
	public String home() {		
    	listaServicos = servicoRepository.findByAtivoTrueOrderByNomeAsc();
        listaUsuario  = usuarioRepository.findAll();
		return "usuario/cadastro";
	}
}

