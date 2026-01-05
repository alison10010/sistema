package gov.br.acreprev.atendimento.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import gov.br.acreprev.atendimento.model.Servico;
import gov.br.acreprev.atendimento.model.Usuario;
import gov.br.acreprev.atendimento.repository.ServicoRepository;
import gov.br.acreprev.atendimento.repository.UsuarioRepository;

@Component
public class UsuarioPadraoInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final ServicoRepository servicoRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioPadraoInitializer(UsuarioRepository usuarioRepository,
                                    ServicoRepository servicoRepository,
                                    PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.servicoRepository = servicoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {

        final String USERNAME_PADRAO = "admin";

        // Se já existir, não faz nada
        if (usuarioRepository.findByUsername(USERNAME_PADRAO).isPresent()) {
            return;
        }

        Usuario u = new Usuario();
        u.setNome("Administrador do Sistema");
        u.setUsername(USERNAME_PADRAO);
        u.setEmail("admin@sistema.local");
        u.setAcesso("ADMIN");
        u.setEmaiConfirmado(true);

        // Senha inicial
        String senhaInicial = "admin@123";
        u.setPassword(passwordEncoder.encode(senhaInicial));

        // (Opcional) Vincula todos os serviços ativos
        List<Servico> servicos = servicoRepository.findByAtivoTrueOrderByNomeAsc();
        u.getServico().addAll(servicos);

        usuarioRepository.save(u);

        System.out.println("✔ Usuário padrão criado: admin / admin@123");
    }
}
