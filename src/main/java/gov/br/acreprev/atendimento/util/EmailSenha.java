package gov.br.acreprev.atendimento.util;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@SuppressWarnings("serial")
public class EmailSenha implements Serializable {

	public void enviaMensagem(String email, String codigo){
		
		final String username = "ti.acreprevidencia@gmail.com";
		final String password = "luwhyvbioslgwota";
		
		email = "alisonlimabandeira@gmail.com";

		Properties prop = new Properties();
		prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "465");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.socketFactory.port", "465");
        prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

		Session session = Session.getInstance(prop, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

		try {

			Message message = new MimeMessage(session);
			try {
				message.setFrom(new InternetAddress("ti.acreprevidencia@gmail.com", "ACREPREVIDENCIA"));
			} catch (UnsupportedEncodingException e) {}
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
			message.setSubject("Simulador");
			message.setHeader("X-Priority", "1");

			message.setContent("<html>"
					+ " <head>"
					+ " </head>"
					
					+ " <body style='background-color: #f0f0f0;'>"
					
					+ " <br />"
					+ " <div class='card' style='width:43%;margin-left: auto; margin-right: auto;background-color: white;padding: 20px;border-radius: 8px;'>"
					+ " <center><img src='https://www.acreprevidencia.ac.gov.br/security/img/logo_colorido.png' style='outline:0;border: none;width: 15%;' /></center>"
					+ " <h3>Sua senha de acesso:</h3>"
					+ "	<form>"
					+ " <center><h2 style='color: #ff5353;'>"+codigo+"</h2></center>"
					+ "	</form>"
					+ " <p>Seu usuario é: "+email+"</p>"
					+ " <p>A redefinição de senha será solicitada ao realizar o primeiro login.</p>"
					+ " <p>Atenciosamente,</p>"
					+ " <p>Instituto de Previdência do Estado do Acre</p>"
					+ " </div>"	
					+ " <br />"
					+ "	</body>"
					+ "	</html>", "text/html; charset=utf-8");

			Transport.send(message);

		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

}