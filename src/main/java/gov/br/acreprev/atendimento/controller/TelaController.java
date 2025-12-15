package gov.br.acreprev.atendimento.controller;

import java.io.Serializable;
import java.util.List;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.annotation.SessionScope;

import gov.br.acreprev.atendimento.model.Tela;
import gov.br.acreprev.atendimento.repository.TelaRepository;
import lombok.Getter;
import lombok.Setter;

@Named
@Getter @Setter
@SessionScope
@Controller
public class TelaController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Autowired
    private TelaRepository telaRepository;

    private Tela tela; // tela em edição

    public List<Tela> getTelas() {
        return telaRepository.findAllByOrderByCreatedAtDesc();
    }

    // abrir dialog para NOVA
    public void novaTela() {
        this.tela = new Tela();
        // opcional: defaults
        // this.tela.setAtiva(true);
    }

    // abrir dialog para EDITAR
    public void editarTela(Tela t) {
        this.tela = t;
    }

    // salvar (serve para novo e editar)
    public void salvar() {
        telaRepository.save(this.tela);
    }
    
    public int layout(String cod) {
    	if (cod == null || cod.isBlank()) {
            return 0; // fallback: layout senha
        }
    	return telaRepository.layout(cod);
    }
}

