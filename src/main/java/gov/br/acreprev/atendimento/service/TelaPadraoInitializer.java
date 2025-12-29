package gov.br.acreprev.atendimento.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.gov.acreprev.atendimento.util.Ferramentas;
import gov.br.acreprev.atendimento.model.Tela;
import gov.br.acreprev.atendimento.repository.TelaRepository;

@Component
public class TelaPadraoInitializer implements CommandLineRunner {

    private final TelaRepository telaRepository;

    public TelaPadraoInitializer(TelaRepository telaRepository) {
        this.telaRepository = telaRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        final String NOME_PADRAO = "PADRAO";

        if (telaRepository.existsByNomeIgnoreCase(NOME_PADRAO)) {
            return; // já existe, sobe normal
        }

        Tela t = new Tela();
        t.setNome(NOME_PADRAO);
        t.setAtiva(true);
        t.setThema("LIGHT");
        t.setLayout(0);

        t.setCodigo(Ferramentas.geraCodTela());

        telaRepository.save(t);
    }
}

