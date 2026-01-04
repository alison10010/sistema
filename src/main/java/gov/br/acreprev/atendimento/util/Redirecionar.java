package gov.br.acreprev.atendimento.util;

import java.io.Serializable;

import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

@SessionScoped
public class Redirecionar implements Serializable {

	private static final long serialVersionUID = 1L;

	
	// REDIRECIONA COM MSG
	public static String irParaURL(String url){
		try {
			FacesContext context = FacesContext.getCurrentInstance();	
			context.getExternalContext().getFlash().setKeepMessages(true);
			context.getExternalContext().redirect("/sistema/"+url);	 
		} catch (Exception e) {
			e.printStackTrace();
		}		      
        return "";
    }
}
