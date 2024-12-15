package qaservice.Common.mailutil;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailSendUtil {
	private static Properties prop_ = new Properties();

	public MailSendUtil() {
		if(prop_.isEmpty()) {
			loadMailProperties();
		}
	}

	public boolean sendMail(String sendMailAddress, String text) {
		if(prop_.isEmpty()) {
			return false;
		}
		String username = prop_.getProperty("mailaddress");
		String password = prop_.getProperty("password");
		Session session = Session.getInstance(prop_, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});
		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(username));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(sendMailAddress));
			message.setSubject("Mail Check Test");
			message.setText(text);
			Transport.send(message);
		} catch(MessagingException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void loadMailProperties() {
		try {
			prop_.load(new FileReader("conf/mail.properties"));
		} catch(IOException e) {
			e.printStackTrace();
			prop_.clear();
		}
	}

}
