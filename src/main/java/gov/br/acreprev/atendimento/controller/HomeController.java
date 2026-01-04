package gov.br.acreprev.atendimento.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
	
	@Autowired
	private PainelController painelController;
	
	@Autowired
	private TotemController totemController;
	
	@GetMapping("/login")
	public String login() {
		return "login/index"; 
	}
	
	@GetMapping("/")
	public String home() {
		totemController.init();
		return "painel/totem";
	}
	
	@GetMapping("/painel")
	public String painel() {
		return "painel/painel";
	}
	
	@GetMapping("/dashboard")
	public String dashboard() {
		return "painel/dashboard";
	}
	
	@GetMapping("/tela")
	public String tela() {
		return "painel/tela";
	}
	
	@GetMapping("/relatorio-periodo")
	public String relatorioPeriodo() {
		painelController.carregarResumo();
		return "painel/relatorio-periodo";
	}
	
	@GetMapping("/relatorio")
	public String relatorio() {
		painelController.carregarResumo();
		return "painel/relatorio";
	}
	
	@GetMapping("/gerenciar-telas")
	public String gerenciarTelas() {
		return "telas/gerenciar-telas";
	}
	
}
