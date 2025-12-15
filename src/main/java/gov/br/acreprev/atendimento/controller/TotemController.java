package gov.br.acreprev.atendimento.controller;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.annotation.SessionScope;

import gov.br.acreprev.atendimento.model.Senha;
import gov.br.acreprev.atendimento.model.Servico;
import gov.br.acreprev.atendimento.model.SubServico;
import gov.br.acreprev.atendimento.repository.SenhaRepository;
import gov.br.acreprev.atendimento.repository.ServicoRepository;
import lombok.Getter;
import lombok.Setter;

@Named
@Getter @Setter 
@SessionScope 
@Controller
public class TotemController implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Autowired
    private SimpMessagingTemplate messagingTemplate;
	
	@Autowired
    private SenhaRepository senhaRepository;

    @Autowired
    private ServicoRepository servicoRepository;

    private List<Servico> servicos;          // serviços principais ativos
    private Servico servicoSelecionado;      // serviço principal selecionado
    private String mensagemSenhaGerada;      // texto mostrado depois de gerar a senha

    @PostConstruct
    public void init() {
        this.servicos = servicoRepository.findByAtivoTrueOrderByNomeAsc();        
        atualizaDadosServico();
    }

    public List<Servico> listServicos() {
        return servicoRepository.findByAtivoTrueOrderByNomeAsc();
    }
    
    public void atualizaDadosServico() {    	
    	if (servicoSelecionado != null && servicoSelecionado.getId() != null) {
            servicoSelecionado = servicoRepository.buscarPorIdComSubServicos(servicoSelecionado.getId());
        }
    }
    
    public void selecionarServico(Servico servico) {
        this.servicoSelecionado = servico;
        this.mensagemSenhaGerada = null;
    }

    public void gerarSenha(SubServico subServico, String tipo) {
        if (servicoSelecionado == null || subServico == null) {
            addMensagem(FacesMessage.SEVERITY_WARN,
                    "Seleção inválida",
                    "Selecione um atendimento e um sub-serviço.");
            return;
        }

        if (tipo == null || (!"N".equalsIgnoreCase(tipo) && !"P".equalsIgnoreCase(tipo))) {
            addMensagem(FacesMessage.SEVERITY_WARN,
                    "Tipo inválido",
                    "Tipo de senha deve ser N (Normal) ou P (Prioritária).");
            return;
        }

        LocalDate hoje = LocalDate.now();

        // pega o próximo número sequencial para hoje + sub-serviço + tipo
        Long proximoNumero = senhaRepository.obterProximoNumero(
                hoje,
                subServico.getId(),
                tipo.toUpperCase()
        );
        if (proximoNumero == null || proximoNumero <= 0) {
            proximoNumero = 1L;
        }

        // monta entidade Senha
        Senha senha = new Senha();
        
        senha.setSenha(proximoNumero.intValue());
        senha.setTipo(tipo.toUpperCase());   // 'N' ou 'P'
        senha.setStatusAtendimento(0);     // aguardando 
        senha.setServico(servicoSelecionado);
        senha.setSubServico(subServico);
        // se tiver "paciente" no totem, pode preencher aqui ou deixar null
        senha.setPaciente(null);

        // prefixo visual da senha
        String prefixoBase;
        if (subServico.getPrefixo() != null && !subServico.getPrefixo().isBlank()) {
            prefixoBase = subServico.getPrefixo().toUpperCase();
        } else if (servicoSelecionado.getPrefixo() != null) {
            prefixoBase = servicoSelecionado.getPrefixo().toUpperCase();
        } else {
            prefixoBase = "";
        }

        String numeroFormatado = String.format("%02d", proximoNumero);
        String prefixoTipo = "N".equalsIgnoreCase(tipo) ? "N" : "P";
        
        // Prefixo do serviço 
        String prefixoServico = servicoSelecionado.getPrefixo().toUpperCase();
        
        // Prefixo do sub-serviço
        String prefixoSubServico = subServico.getPrefixo().toUpperCase();

        // EXEMPLO: "N AGC001"  (N = tipo, A = servico, GC = sub-servico, 001 = número)
        String senhaVisual = prefixoTipo + " " + prefixoServico + prefixoSubServico + numeroFormatado;

        senha.setSenhaVisual(senhaVisual);
        
        senhaRepository.save(senha);

        this.mensagemSenhaGerada = senhaVisual
                + " - "
                + servicoSelecionado.getNome()
                + " / "
                + subServico.getNome();
        
        messagingTemplate.convertAndSend("/topic/listEsperaPainel", "refresh");
        messagingTemplate.convertAndSend("/topic/senhaGeradaInfo", "refresh");
        
    }
    
    private void addMensagem(FacesMessage.Severity severidade, String resumo, String detalhe) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(severidade, resumo, detalhe));
    }
    
    public String getDataHoraAtualFormatada() {
        return java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
    
    
}
