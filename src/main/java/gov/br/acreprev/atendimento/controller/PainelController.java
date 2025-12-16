package gov.br.acreprev.atendimento.controller;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.annotation.SessionScope;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.br.acreprev.atendimento.dto.DashboardResumoDTO;
import gov.br.acreprev.atendimento.model.Atendimento;
import gov.br.acreprev.atendimento.model.Senha;
import gov.br.acreprev.atendimento.model.Servico;
import gov.br.acreprev.atendimento.model.Tela;
import gov.br.acreprev.atendimento.repository.AtendimentoRepository;
import gov.br.acreprev.atendimento.repository.SenhaRepository;
import gov.br.acreprev.atendimento.repository.ServicoRepository;
import gov.br.acreprev.atendimento.repository.TelaRepository;
import gov.br.acreprev.atendimento.service.SequenciaChamadaService;
import lombok.Getter;
import lombok.Setter;

@Named
@Getter
@Setter
@SessionScope
@Controller
public class PainelController implements Serializable {

    private static final long serialVersionUID = 1L;
    
    // Controle da sequência de tipos: P, N, N
    @Autowired
    private SequenciaChamadaService sequenciaChamadaService;
    
    @Autowired
    private TelaRepository telaRepository;

    @Autowired
    private SenhaRepository senhaRepository;

    @Autowired
    private ServicoRepository servicoRepository;

    @Autowired
    private AtendimentoRepository atendimentoRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    private DashboardResumoDTO dashboardResumoDTO;

    // Serviço atualmente selecionado no painel
    private Servico servicoSelecionado;

    // Estado atual do guichê e da última chamada
    private Integer guicheSelecionado = 1;
    private String ultimaSenha = "---";
    private String ultimoTipoFicha = "---";
    private String ultimoGuiche = "---";

    // Para rechamar
    private Senha senhaAtual;
    private Atendimento atendimentoAtual;
    
    private List<Object[]> servicosNoDia;
    private List<Object[]> subServicosNoDia;
    

    // =========================================================
    // LISTAS / SELEÇÃO
    // =========================================================

    public List<Servico> getServicos() {
        return servicoRepository.findByAtivoTrueOrderByNomeAsc();
    }

    /**
     * Seleciona um serviço ao clicar no card do painel.
     */
    public void selecionarServico(Servico s) {
        this.servicoSelecionado = s;        
    }

    // =========================================================
    // AÇÕES DO PAINEL
    // =========================================================

    /**
     * Chama a próxima senha seguindo a sequência de tipos P, N, N
     * para o serviço selecionado.
     */
    public void chamarProximaSenha() {
        try {
            if (servicoSelecionado == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN,
                                "Selecione um serviço antes de chamar a senha.", ""));
                return;
            }

            LocalDate hoje = LocalDate.now();
            LocalDateTime inicio = hoje.atStartOfDay();
            LocalDateTime fim = hoje.plusDays(1).atStartOfDay();

            Long servicoId = servicoSelecionado.getId();
            
            synchronized (("SERVICO_" + servicoId).intern()) {
            	
	            String[] sequenciaTipos = sequenciaChamadaService.getSequenciaTipos();
	
	            // índice base da sequência GLOBAL para este serviço
	            int indiceBase = sequenciaChamadaService.getIndiceAtual(servicoId);
	
	            // Tenta no máximo 3 vezes (P, N, N), respeitando a sequência GLOBAL
	            for (int i = 0; i < sequenciaTipos.length; i++) {
	                int posicao = (indiceBase + i) % sequenciaTipos.length;
	                String tipoTentativa = sequenciaTipos[posicao];
	
	                Optional<Senha> optProxima = senhaRepository
	                        .findFirstBySubServicoServicoAndTipoAndStatusAtendimentoAndDataBetweenOrderByDataAsc(
	                                servicoSelecionado,
	                                tipoTentativa,
	                                0,
	                                inicio,
	                                fim
	                        );
	
	                if (optProxima.isPresent()) {
	                    // Atualiza sequência GLOBAL para o próximo tipo
	                    int novoIndice = (indiceBase + i + 1) % sequenciaTipos.length;
	                    sequenciaChamadaService.atualizarIndice(servicoId, novoIndice);
	
	                    processarChamada(optProxima.get());
	                    return;
	                }
	            }
	
	            // Se chegou aqui, não encontrou nenhuma senha para nenhum tipo da sequência
	            FacesContext.getCurrentInstance().addMessage(null,
	                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Não há senhas aguardando para o serviço selecionado.", ""));	            
            }            

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Erro ao chamar senha",
                            e.getMessage()));
        }
    }


    /**
     * Processa a chamada da senha: atualiza status, registra Atendimento,
     * atualiza dados exibidos no painel e envia para a tela (TV).
     */
    private void processarChamada(Senha senha) {
        this.senhaAtual = senha;

        senhaAtual.setStatusAtendimento(1);
        senhaAtual.setDataUpdate(LocalDateTime.now());
        senhaRepository.save(senhaAtual);
        
        Tela t = telaRepository.codTela(senhaAtual.getServico().getTela()); 
        
        atendimentoAtual = new Atendimento(
                LocalDateTime.now(),
                guicheSelecionado,
                senhaAtual.getSenha(),
                senhaAtual.getTipo(),
                senhaAtual.getSenhaVisual(),
                senhaAtual.getServico().getComparecer(),
                senhaAtual.getServico().getNome(),
                senhaAtual.getSubServico().getNome(),
                t.getCodigo()
        );
        atendimentoRepository.save(atendimentoAtual);

        ultimaSenha = senhaAtual.getSenhaVisual();
        ultimoTipoFicha = "P".equalsIgnoreCase(senhaAtual.getTipo()) ? "Prioritária" : "Normal";
        ultimoGuiche = senhaAtual.getServico().getComparecer() +" "+ guicheSelecionado;
        
        enviarParaTela(senhaAtual, ultimoGuiche);
        
        messagingTemplate.convertAndSend("/topic/listEsperaPainel", "refresh");
        messagingTemplate.convertAndSend("/topic/senhaGeradaInfo", "refresh");
                
        messagingTemplate.convertAndSend("/topic/updateHistorico/"+ t.getCodigo(), "refresh");
        notificarPainelFila();        
        
        notificarSistema("Senha " + ultimaSenha + " - Guichê " + guicheSelecionado);
    }

    /**
     * Fila de espera APENAS do serviço selecionado.
     */
    public List<Senha> getSenhasAguardandoPorServico() {
        if (servicoSelecionado == null) {
            return Collections.emptyList();
        }

        LocalDate hoje = LocalDate.now();
        LocalDateTime inicio = hoje.atStartOfDay();
        LocalDateTime fim = hoje.plusDays(1).atStartOfDay();

        return senhaRepository
                .findBySubServicoServicoAndStatusAtendimentoAndDataBetweenOrderByDataAsc(
                        servicoSelecionado,
                        0,
                        inicio,
                        fim
                );
    }

    /**
     * Fila de espera geral (se ainda for usada em algum lugar).
     */
    public List<Senha> getSenhasAguardando() {
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicio = hoje.atStartOfDay();
        LocalDateTime fim = hoje.plusDays(1).atStartOfDay();

        return senhaRepository
                .findByStatusAtendimentoAndDataBetweenOrderByDataAsc(0, inicio, fim);
    }

    /**
     * Apenas reenvia para a TV a última senha chamada.
     */
    public void rechamarUltimaSenha() {
        if (senhaAtual == null || ultimoGuiche == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Nenhuma senha foi chamada ainda.", ""));
            return;
        }

        enviarParaTela(senhaAtual, ultimoGuiche);
        notificarSistema("Rechamada: Senha " + ultimaSenha + " - Guichê " + ultimoGuiche);
    }

    /**
     * Últimas senhas atendidas exibidas na TV (tela.xhtml).
     * (ex.: as últimas 6 senhas do dia)
     */
    public List<Atendimento> senhasAnteriores(String cod) {
        return atendimentoRepository.ultimos6Atendimento(cod);
    }
    
    public List<Atendimento> senhasHoje() {
        return atendimentoRepository.senhasHoje();
    }
    
    public void carregarResumo() {
        List<Object[]> lista = senhaRepository.resumoRelatorio();

        // Se não veio nada, zera o DTO
        if (lista == null || lista.isEmpty()) {
            dashboardResumoDTO = new DashboardResumoDTO(0L, 0L, 0L, 0L);
            return;
        }

        Object[] resultado = lista.get(0); //  [8, 8, 7, 1]

        Long senhasHoje     = ((Number) resultado[0]).longValue();
        Long senhasMes      = ((Number) resultado[1]).longValue();
        Long concluidosHoje = ((Number) resultado[2]).longValue();
        Long aguardandoHoje = ((Number) resultado[3]).longValue();

        dashboardResumoDTO = new DashboardResumoDTO(
                senhasHoje,
                senhasMes,
                concluidosHoje,
                aguardandoHoje
        );
        
        carregarServicosNoDia();
    }
    
    public void carregarServicosNoDia() {
        // 1. Carrega serviços por tipo (agora com 4 colunas)
        this.servicosNoDia = senhaRepository.servicosNoDia();        
        this.subServicosNoDia = senhaRepository.subServicosNoDia();
    }

    // Método para o Gráfico de Barras (Serviços por Tipo)
    public String getServicosNoDiaJson() {
        
        try {
            List<Map<String, Object>> lista = new ArrayList<>();

            if (servicosNoDia != null) {
                for (Object[] row : servicosNoDia) {
                    Map<String, Object> map = new HashMap<>();
                    
                    // Coluna 0: Nome do Serviço (Label)
                    map.put("label", row[0]); 
                    // Coluna 1: Quantidade Total (Mantida, mas não usada no Chart.js final)
                    map.put("value", ((Number) row[1]).longValue()); 
                    
                    // Coluna 2: Quantidade Normal
                    map.put("normal", ((Number) row[2]).longValue()); 
                    // Coluna 3: Quantidade Prioridade
                    map.put("prioridade", ((Number) row[3]).longValue()); 
                    
                    lista.add(map);
                }
            }
            // Retorna um array JSON como: [{"label":"Serviço A","value":50,"normal":30,"prioridade":20}, ...]
            return new ObjectMapper().writeValueAsString(lista); 
        } catch (Exception e) {
            // Tratar exceção
            return "[]";
        }
    }

    public DashboardResumoDTO getDashboardResumoDTO() {
        return dashboardResumoDTO;
    }


    // =========================================================
    // MÉTODOS AUXILIARES (WebSocket / Notificações)
    // =========================================================

    private void enviarParaTela(Senha senha, String guiche) {
        Map<String, Object> payload = new HashMap<>();

        payload.put("senha", senha.getSenha());
        payload.put("tipo", senha.getTipo());
        payload.put("paciente", senha.getPaciente());
        payload.put("guiche", guiche);

        // Campos novos:
        payload.put("senhaFormatada", formatarSenhaDisplay(senha));   // Ex: "N A004"
        payload.put("descricao", montarDescricaoSenha(senha));        // Ex: "ATENDIMENTO GERAL / GERAL"

        // Se quiser mandar também separado:
        payload.put("servico", senha.getServico() != null ? senha.getServico().getNome() : null);
        payload.put("subServico", senha.getSubServico() != null ? senha.getSubServico().getNome() : null);
        
        payload.put("comparecer", senha.getServico().getComparecer());
        
        Tela t = telaRepository.codTela(senha.getServico().getTela());
        		
        payload.put("tela", t.getCodigo());        
        
        messagingTemplate.convertAndSend("/topic/senha/" + t.getCodigo(), payload);
    }
    
    /**
     * Ex: "N A004" (tipo + prefixo do sub-serviço + número com 3 dígitos).
     */
    private String formatarSenhaDisplay(Senha senha) {
    	String prefixoTipo = senha.getTipo().equalsIgnoreCase("P") ? "P" : "N";

        String prefixoServico = "";
        if (senha.getServico() != null && senha.getServico().getPrefixo() != null) {
            prefixoServico = senha.getServico().getPrefixo().toUpperCase();
        }

        String prefixoSubServico = "";
        if (senha.getSubServico() != null && senha.getSubServico().getPrefixo() != null) {
            prefixoSubServico = senha.getSubServico().getPrefixo().toUpperCase();
        }

        String numeroFormatado = String.format("%02d", senha.getSenha());

        return prefixoTipo + " " + prefixoServico + prefixoSubServico + numeroFormatado;
    }

    /**
     * Ex: "ATENDIMENTO GERAL / GERAL"
     */
    private String montarDescricaoSenha(Senha s) {
        if (s == null) return "";

        String servico = (s.getServico() != null && s.getServico().getNome() != null) ? s.getServico().getNome() : "";

        String sub = (s.getSubServico() != null && s.getSubServico().getNome() != null) ? s.getSubServico().getNome() : "";

        if (!servico.isEmpty() && !sub.isEmpty()) {
            return servico;     // return servico + " / " + sub;
        }
        if (!servico.isEmpty()) return servico;
        return sub;
    }


    private void notificarPainelFila() {
        // Usado para disparar atualização da fila de espera via WebSocket
        messagingTemplate.convertAndSend("/topic/aguardando", Collections.singletonMap("refresh", true));
    }

    private void notificarSistema(String msg) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("msg", msg);
        messagingTemplate.convertAndSend("/topic/notificacao", payload);
    }
}