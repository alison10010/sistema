package gov.br.acreprev.atendimento.controller;

import java.io.Serializable;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
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

        if (tela.getLayout() == 1) { // layout vídeo
            String url = tela.getVideoUrl();

            if (url == null || url.isBlank()) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Informe a URL do vídeo do YouTube.", null));
                return;
            }

            if (!isYoutubeUrl(url)) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "A URL informada não é do YouTube.", 
                        "Use apenas links do youtube.com ou youtu.be"));
                return;
            }
        }

        telaRepository.save(this.tela);
    }

    public int layout(String cod) {
    	if (cod == null || cod.isBlank()) {
            return 0; // fallback: layout senha
        }
    	return telaRepository.layout(cod);
    }
    
    public boolean isYoutubeUrl(String url) {
        if (url == null || url.isBlank()) return false;

        String u = url.trim().toLowerCase();

        return u.matches("^(https?://)?(www\\.)?(youtube\\.com|youtu\\.be)/.+$");
    }

    
    // Video na Tela
    public String videoEmbedUrl(String codigo) {
        if (codigo == null || codigo.isBlank()) return "";

        Tela t = telaRepository.findByCodigo(codigo.toUpperCase());
        if (t == null || t.getVideoUrl() == null || t.getVideoUrl().isBlank()) return "";

        // Aceita URL completa ou ID
        String raw = t.getVideoUrl().trim();

        // extrai ID se vier watch?v=... ou youtu.be/...
        String id = extrairYoutubeId(raw);
        if (id == null || id.isBlank()) return "";

        // IMPORTANTE: em XHTML use &amp;
        return "https://www.youtube-nocookie.com/embed/" + id
                + "?enablejsapi=1&amp;autoplay=1&amp;controls=0&amp;rel=0&amp;modestbranding=1"
                + "&amp;loop=1&amp;playlist=" + id;
    }

    private String extrairYoutubeId(String urlOuId) {
        // se já for um ID
        if (!urlOuId.contains("http") && urlOuId.length() >= 8) {
            return urlOuId;
        }

        // exemplos:
        // https://www.youtube.com/watch?v=VayOrXlERHs
        // https://youtu.be/VayOrXlERHs
        String u = urlOuId;

        int idx = u.indexOf("v=");
        if (idx >= 0) {
            String v = u.substring(idx + 2);
            int e = v.indexOf("&");
            return (e > 0) ? v.substring(0, e) : v;
        }

        idx = u.indexOf("youtu.be/");
        if (idx >= 0) {
            String v = u.substring(idx + "youtu.be/".length());
            int e = v.indexOf("?");
            return (e > 0) ? v.substring(0, e) : v;
        }

        idx = u.indexOf("/embed/");
        if (idx >= 0) {
            String v = u.substring(idx + "/embed/".length());
            int e = v.indexOf("?");
            return (e > 0) ? v.substring(0, e) : v;
        }

        return null;
    }

    
    public String thema(String cod) {
        if (cod == null || cod.isBlank()) return "LIGHT";

        String t = telaRepository.thema(cod.toUpperCase());
        if (t == null || t.isBlank()) return "LIGHT";

        return t.trim().toUpperCase(); // "LIGHT" ou "DARK"
    }
    
}

