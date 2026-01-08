package gov.br.acreprev.atendimento.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.annotation.SessionScope;

import gov.br.acreprev.atendimento.model.Servico;
import gov.br.acreprev.atendimento.model.SubServico;
import gov.br.acreprev.atendimento.repository.ServicoRepository;
import lombok.Getter;
import lombok.Setter;

@Named
@Getter @Setter 
@SessionScope 
@Controller
public class DashboardController implements Serializable {
	
	@Autowired
    private SimpMessagingTemplate messagingTemplate;

    private static final long serialVersionUID = 1L;

    @Autowired
    private ServicoRepository servicoRepository;

    private List<Servico> servicos;            // lista de serviços principais
    private Servico servicoSelecionado;        // serviço que está sendo editado
    private boolean editando = false;          // se está em modo de edição/criação

    @PostConstruct
    public void init() {
        carregarServicos();
    }

    public void carregarServicos() {
        // Você pode usar findByAtivoTrueOrderByNomeAsc se quiser só ativos
        this.servicos = servicoRepository.findAll();
        if (this.servicos == null) {
            this.servicos = new ArrayList<>();
        }
    }

    // === Ações do CRUD ===

    public void novoServico() {
        this.servicoSelecionado = new Servico();
        this.servicoSelecionado.setAtivo(true);
        this.servicoSelecionado.setSubServicos(new ArrayList<>());
        this.editando = true;
    }

    public void editarServico(Servico servico) {
        if (servico == null) {
            this.servicoSelecionado = null;
            this.editando = false;
            return;
        }

        this.servicoSelecionado = servico;

        // Segurança extra: garante que nunca seja null
        if (this.servicoSelecionado.getSubServicos() == null) {
            this.servicoSelecionado.setSubServicos(new ArrayList<>());
        }
        
        this.editando = true;
        
        this.servicos = servicoRepository.findAll();
    }

    public void cancelarEdicao() {
        this.servicoSelecionado = null;
        this.editando = false;
    }

    public void salvarServico() {
        if (servicoSelecionado == null) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Nenhum serviço selecionado.", "");
            return;
        }

        String serv = servicoSelecionado.getNome();
        String prefixo = servicoSelecionado.getPrefixo();
        String comparecer = servicoSelecionado.getComparecer();
        
        if (prefixo == null || prefixo.trim().isEmpty()) {
            adicionarMensagem(FacesMessage.SEVERITY_WARN, "Prefixo não pode ser vazio.", "");
            return;
        }
        
        if (serv == null || serv.trim().isEmpty()) {
            adicionarMensagem(FacesMessage.SEVERITY_WARN, "Informe o nome do serviço.", "");
            return;
        }
        
        if (comparecer == null || comparecer.trim().isEmpty()) {
            adicionarMensagem(FacesMessage.SEVERITY_WARN, "Selecione o local 'Comparecer'.", "");
            return;
        }

        prefixo = prefixo.trim().toUpperCase();
        servicoSelecionado.setPrefixo(prefixo);

        // Valida unicidade do prefixo entre serviços diferentes
        Long id = servicoSelecionado.getId();
        boolean existe;

        if (id == null) {
            existe = servicoRepository.existsByPrefixoIgnoreCase(prefixo);
        } else {
            existe = servicoRepository.existsByPrefixoIgnoreCaseAndIdNot(prefixo, id);
        }

        if (existe) {
            adicionarMensagem(
                    FacesMessage.SEVERITY_WARN,
                    "Prefixo já utilizado",
                    "Já existe outro serviço com o prefixo: " + prefixo
            );
            return;
        }
        
        // Validar prefixo dos sub-serviços (não pode repetir)
        if (servicoSelecionado.getSubServicos() != null) {

            Set<String> subPrefixos = new HashSet<>();

            for (SubServico sub : servicoSelecionado.getSubServicos()) {
                if (sub == null) {
                    continue;
                }

                // 👉 Se o sub-serviço estiver inativo, IGNORA na validação
                // ajuste conforme seu atributo: isAtivo(), getStatus() == 1, etc.
                if (!sub.isAtivo()) { 
                    continue;
                }

                String subPrefixo = sub.getPrefixo();

                // === OBRIGATÓRIO (apenas para ativos) ===
                if (subPrefixo == null || subPrefixo.trim().isEmpty()) {
                    adicionarMensagem(
                            FacesMessage.SEVERITY_WARN,
                            "Informe o prefixo para todos os subserviços ativos.", "");
                    return;
                }

                subPrefixo = subPrefixo.trim().toUpperCase();

                // === DUPLICADO (apenas entre ativos) ===
                if (!subPrefixos.add(subPrefixo)) {
                    adicionarMensagem(
                            FacesMessage.SEVERITY_WARN,
                            "Prefixo duplicado",
                            "O prefixo \"" + subPrefixo + "\" já está em uso em outro sub-serviço ativo."
                    );
                    return;
                }
            }
        }


        
        // Ajusta relação bidirecional (garantia extra)
        if (servicoSelecionado.getSubServicos() != null) {
            for (SubServico sub : servicoSelecionado.getSubServicos()) {
                sub.setServico(servicoSelecionado);
            }
        }

        servicoSelecionado = servicoRepository.save(servicoSelecionado);
        
        messagingTemplate.convertAndSend("/topic/updateServico", "refresh");
        
        adicionarMensagem(FacesMessage.SEVERITY_INFO, "Serviço salvo com sucesso.", "");

        carregarServicos();

        this.editando = false;
    }

    public void excluirServico(Servico servico) {
        if (servico == null || servico.getId() == null) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Serviço inválido para exclusão.", "");
            return;
        }

        servicoRepository.delete(servico);
        adicionarMensagem(FacesMessage.SEVERITY_INFO, "Serviço excluído com sucesso.", "");
        carregarServicos();

        if (servicoSelecionado != null && servicoSelecionado.getId() != null
                && servicoSelecionado.getId().equals(servico.getId())) {
            servicoSelecionado = null;
            editando = false;
        }
    }

    // === Sub-serviços ===

    public void adicionarSubServico() {
        if (servicoSelecionado == null) {
            adicionarMensagem(FacesMessage.SEVERITY_WARN, "Selecione um serviço primeiro.", "");
            return;
        }

        if (servicoSelecionado.getSubServicos() == null) {
            servicoSelecionado.setSubServicos(new ArrayList<>());
        }

        SubServico sub = new SubServico();
        sub.setNome("Novo sub-serviço");
        sub.setSubSenhaNormal(true);
        sub.setSubSenhaPrioritaria(true);
        sub.setServico(servicoSelecionado);

        servicoSelecionado.getSubServicos().add(sub);
    }

    public void removerSubServico(SubServico sub) {
        final String cod = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        final java.security.SecureRandom random = new java.security.SecureRandom();

        if (servicoSelecionado == null || sub == null) {
            return;
        }

        // Gera um prefixo "aleatório" e praticamente impossível de repetir (4 chars + 2 do timestamp)
        String prefixo;
        {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 4; i++) {
                sb.append(cod.charAt(random.nextInt(cod.length())));
            }
            long t = System.currentTimeMillis() % 100; // 00..99
            sb.append(String.format("%02d", t));
            prefixo = sb.toString();
        }

        // Em vez de null, define um prefixo novo e desativa
        sub.setPrefixo(prefixo);
        sub.setAtivo(false);
    }


    // === Utilitário para mensagens JSF ===

    private void adicionarMensagem(FacesMessage.Severity severidade, String resumo, String detalhe) {
        FacesMessage msg = new FacesMessage(severidade, resumo, detalhe);
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
}
