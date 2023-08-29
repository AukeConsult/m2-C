package no.auke.mail;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.security.Security;
import java.util.Properties;

public class MailSender {

    private static final Logger logger = LoggerFactory.getLogger(MailSender.class);
    
    private static final String SMTP_HOST_NAME = "smtp.gmail.com";//"mail.auke.no";
    private static final String SMTP_AUTH_USER = "auketeam@gmail.com";//"admin";
    private static final String SMTP_AUTH_PWD = "auketeam123";//"jippi99";
    private static final String SMTP_PORT = "465";
//    private static final String SMTP_HOST_NAME = "mail.auke.no";
//    private static final String SMTP_AUTH_USER = "admin";
//    private static final String SMTP_AUTH_PWD = "jippi99";
//    private static final String SMTP_PORT = "25";
    private static final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
    
    private Session session;
    
    private boolean dosend=true;

    @SuppressWarnings("restriction")
	public MailSender() {
               
        boolean debug = false;
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
      
        Properties props = new Properties();
        props.setProperty("mail.debug", "true");
        props.setProperty("mail.smtp.host", SMTP_HOST_NAME); //we use smtp
        props.setProperty("mail.smtp.auth", "true");
        
       
        props.setProperty("mail.smtp.port", SMTP_PORT);
        props.setProperty("mail.smtp.socketFactory.port", SMTP_PORT);
        props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
        props.setProperty("mail.smtp.socketFactory.fallback", "false");
        
        //IMAP provider (if any)
        props.setProperty("mail.imap.port", "993");
        props.setProperty("mail.imap.socketFactory.port", "993");
        props.setProperty("mail.imap.socketFactory.class", SSL_FACTORY);
        props.setProperty("mail.imap.socketFactory.fallback", "false");
        //POP3 provider (if any)
        props.setProperty("mail.pop3.port", "995");
        props.setProperty("mail.pop3.socketFactory.port", "995");
        props.setProperty("mail.pop3.socketFactory.class", SSL_FACTORY);
        props.setProperty("mail.pop3.socketFactory.fallback", "false");
        //NNTP provider (if any)
        props.setProperty("mail.nntp.port", "563");
        props.setProperty("mail.nntp.socketFactory.class", SSL_FACTORY);
        props.setProperty("mail.nntp.socketFactory.port", "563");
        props.setProperty("mail.nntp.socketFactory.fallback", "false");

        
        
        Authenticator auth = new SMTPAuthenticator();
        session = Session.getInstance(props, auth);

        session.setDebug(debug);
        
    }


    public void postMail(String recipients[], String subject, String message, String from, String[] files) {
        
        

        if(dosend)
        {    
            // create a message
            try {
                
                Message msg = new MimeMessage(session);
        
                // set the from and to address
                InternetAddress addressFrom = new InternetAddress(from);
            
                msg.setFrom(addressFrom);
        
                InternetAddress[] addressTo = new InternetAddress[recipients.length];
                for (int i = 0; i < recipients.length; i++) {
                    addressTo[i] = new InternetAddress(recipients[i]);
                }
                msg.setRecipients(Message.RecipientType.TO, addressTo);
        
        
                // Setting the Subject and Content Type
                msg.setSubject(subject);
                msg.setContent(message, "text/plain");
        
                Multipart multipart = new MimeMultipart();
        
                BodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart = new MimeBodyPart();
                messageBodyPart.setText(message);
                multipart.addBodyPart(messageBodyPart);
               
        
                if (files != null && files.length >0) {
        
                	for(String filename : files)
                	{
                		FileDataSource fileDataSource = new FileDataSource(filename);
                		MimeBodyPart attachPart = new MimeBodyPart();
                		attachPart.setDataHandler(new DataHandler(fileDataSource));
                		attachPart.setFileName(fileDataSource.getName());
                		multipart.addBodyPart(attachPart);
                	}
                }
               msg.setContent(multipart);
        
                
                Transport.send(msg);
                //Transport transport = session.getTransport("smtp");
                //transport.connect(SMTP_HOST_NAME, 25, SMTP_AUTH_USER, SMTP_AUTH_PWD);
        
                //Transport.send(msg);
            
            } catch (AddressException e) {
                
                logger.error("Sending mail error " + e.getMessage());
                
            } catch (MessagingException e) {
    
                logger.error("Sending mail error " + e.getMessage());
            }
        }
    }


    /**
     * SimpleAuthenticator is used to do simple authentication
     * when the SMTP server requires it.
     */
    private class SMTPAuthenticator extends javax.mail.Authenticator {

        public PasswordAuthentication getPasswordAuthentication() {
            String username = SMTP_AUTH_USER;
            String password = SMTP_AUTH_PWD;
            return new PasswordAuthentication(username, password);
        }
    }

}

