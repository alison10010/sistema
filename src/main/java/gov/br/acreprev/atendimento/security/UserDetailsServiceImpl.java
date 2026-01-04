package gov.br.acreprev.atendimento.security;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import gov.br.acreprev.atendimento.model.Servico;
import gov.br.acreprev.atendimento.model.Usuario;
import gov.br.acreprev.atendimento.repository.UsuarioRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));

        List<Servico> servicos = usuario.getServico();
        if (servicos == null) servicos = Collections.emptyList();

        Set<GrantedAuthority> authorities = servicos.stream()
                .filter(s -> s != null)
                .map(Servico::getPrefixo) // pode ser null
                .filter(p -> p != null && !p.trim().isEmpty())
                .map(p -> p.trim().toUpperCase())
                .map(p -> p.startsWith("ROLE_") ? p : "ROLE_" + p)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        return User.withUsername(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities(authorities)
                .build();
    }
}