package gov.br.acreprev.atendimento.controller;

import java.io.Serializable;

import javax.faces.bean.ViewScoped;
import javax.inject.Named;

import org.springframework.web.bind.annotation.CrossOrigin;

@Named
@ViewScoped
@CrossOrigin(origins = "*")
public class TelaViewController implements Serializable {

    private String codigoTela;

    public String getCodigoTela() { return codigoTela; }
    public void setCodigoTela(String codigoTela) { this.codigoTela = codigoTela; }
}

