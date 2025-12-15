package br.gov.acreprev.atendimento.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.Normalizer;
import java.util.regex.Pattern;

public class Ferramentas {
	
	//	GERA MD5 E RETORNA O VALOR 
	public static String geraMd5(String valor) {
		try {
			String valorMD5 = valor;
			MessageDigest msgDig = MessageDigest.getInstance("MD5");
			msgDig.update(valorMD5.getBytes(), 0, valorMD5.length());
			valorMD5 = new BigInteger(1, msgDig.digest()).toString(16);
			return valorMD5;
		} catch (Exception e) {
		}
		return "";
	}
	
	//	REMOVE CARACTERE ESPECIAIS
	public static String removeAcento(String str) {
        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD); 
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }

}
