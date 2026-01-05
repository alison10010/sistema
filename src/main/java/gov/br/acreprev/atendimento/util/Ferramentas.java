package gov.br.acreprev.atendimento.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
	
	public static String geraCodTela() {
	    return Long.toString(
	            Math.abs(new SecureRandom().nextLong()), 36
	    ).toUpperCase().substring(0, 5);
	}

	public static int randomDeDataHora() {
        // Capturar a data e hora atual
        LocalDateTime currentDateTime = LocalDateTime.now();
        
        // Formatar a data e hora atual em um formato específico
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String formattedDateTime = currentDateTime.format(formatter);
        
        // Pegar os últimos 6 dígitos da data e hora formatada
        String lastFiveDigits = formattedDateTime.substring(formattedDateTime.length() - 6);
        
        // Converter os últimos 5 dígitos para um número inteiro
        int randomNumber = Integer.parseInt(lastFiveDigits);
        
        // retorna o número aleatório de 5 dígitos
        return randomNumber;
    }

}
