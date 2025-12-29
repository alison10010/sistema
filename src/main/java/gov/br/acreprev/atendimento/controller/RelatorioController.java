package gov.br.acreprev.atendimento.controller;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.annotation.SessionScope;

import gov.br.acreprev.atendimento.dto.AtendimentoResumoDTO;
import gov.br.acreprev.atendimento.model.Servico;
import gov.br.acreprev.atendimento.model.SubServico;
import gov.br.acreprev.atendimento.repository.AtendimentoRepository;
import gov.br.acreprev.atendimento.repository.ServicoRepository;
import lombok.Getter;
import lombok.Setter;

@Named
@Getter
@Setter
@SessionScope
@Controller
public class RelatorioController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Autowired
    private ServicoRepository servicoRepository;

    @Autowired
    private AtendimentoRepository atendimentoRepository;

    // ===== filtros (padrão: hoje) =====
    private LocalDate dataInicio;
    private LocalDate dataFim;

    // ===== dados para a tela =====
    private List<Servico> servicos = new ArrayList<>();

    // ===== resumo consolidado =====
    private List<AtendimentoResumoDTO> resumoAtendimentos = new ArrayList<>();

    // ===== KPIs =====
    private Long totalAtendimentos = 0L;
    private Long totalNormais = 0L;
    private Long totalPrioritarias = 0L;

    private Integer totalServicos = 0;
    private Integer totalServicosAtivos = 0;
    private Integer totalSubServicosAtivos = 0;

    private String topServicoNome = "-";
    private Long topServicoQtd = 0L;

    private LocalDateTime agora;

    @PostConstruct
    public void init() {
        this.dataInicio = LocalDate.now();
        this.dataFim = LocalDate.now();
        carregar();
    }

    public void carregar() {
        this.agora = LocalDateTime.now();

        // Se usuário não preencher, assume hoje
        LocalDate di = (dataInicio != null) ? dataInicio : LocalDate.now();
        LocalDate df = (dataFim != null) ? dataFim : di; // se não vier fim, usa início

        // Se vier invertido (início maior que fim), troca
        if (di.isAfter(df)) {
            LocalDate tmp = di;
            di = df;
            df = tmp;
        }

        LocalDateTime inicio = di.atStartOfDay();
        LocalDateTime fim = df.atTime(LocalTime.MAX);

        // 1) Serviços
        this.servicos = servicoRepository.findAll();
        this.totalServicos = servicos.size();
        this.totalServicosAtivos = (int) servicos.stream().filter(Servico::isAtivo).count();

        this.totalSubServicosAtivos = (int) servicos.stream()
                .filter(Objects::nonNull)
                .flatMap(s -> s.getSubServicos() == null ? java.util.stream.Stream.<SubServico>empty() : s.getSubServicos().stream())
                .filter(Objects::nonNull)
                .filter(SubServico::isAtivo)
                .count();

        // 2) Resumo consolidado
        this.resumoAtendimentos = atendimentoRepository.resumoPorServicoESubNP(inicio, fim);

        // 3) KPIs
        this.totalNormais = resumoAtendimentos.stream().mapToLong(r -> nvl(r.getTotalN())).sum();
        this.totalPrioritarias = resumoAtendimentos.stream().mapToLong(r -> nvl(r.getTotalP())).sum();
        this.totalAtendimentos = totalNormais + totalPrioritarias;

        // 4) Top serviço
        Map<String, Long> porServico = resumoAtendimentos.stream()
                .collect(Collectors.groupingBy(
                        r -> safe(r.getServico()),
                        Collectors.summingLong(r -> nvl(r.getTotal()))
                ));

        porServico.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresent(e -> {
                    this.topServicoNome = e.getKey().isBlank() ? "-" : e.getKey();
                    this.topServicoQtd = e.getValue();
                });

        if (porServico.isEmpty()) {
            this.topServicoNome = "-";
            this.topServicoQtd = 0L;
        }

        // Atualiza as datas (caso tenha invertido)
        this.dataInicio = di;
        this.dataFim = df;
    }
    
    public void hoje() {
        this.dataInicio = LocalDate.now();
        this.dataFim = LocalDate.now();
        carregar();
    }



    public String getPeriodoLabel() {
        if (dataInicio == null && dataFim == null) return "-";
        LocalDate di = (dataInicio != null) ? dataInicio : dataFim;
        LocalDate df = (dataFim != null) ? dataFim : dataInicio;

        return String.format("%02d/%02d/%04d até %02d/%02d/%04d",
                di.getDayOfMonth(), di.getMonthValue(), di.getYear(),
                df.getDayOfMonth(), df.getMonthValue(), df.getYear()
        );
    }

    public Date getAgoraDate() {
        LocalDateTime agora = LocalDateTime.now(ZoneId.of("America/Rio_Branco"));
        return Date.from(agora.atZone(ZoneId.of("America/Rio_Branco")).toInstant());
    }

    public String percentual(Long subtotal) {
        if (subtotal == null || totalAtendimentos == null || totalAtendimentos == 0L) return "0";
        double pct = (subtotal.doubleValue() * 100.0) / totalAtendimentos.doubleValue();
        return String.format(Locale.forLanguageTag("pt-BR"), "%.1f", pct);
    }

    private long nvl(Long v) {
        return v == null ? 0L : v;
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}