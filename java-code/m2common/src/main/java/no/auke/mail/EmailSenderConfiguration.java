package no.auke.mail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Properties;

/**
 * Email sender configuration file. Provide store SMTP client configuration 
 * and loading parameters from various sources.
 * 
 * @author Thai Huynh
 */
public class EmailSenderConfiguration {
	
	/**
	 * Authentication type which not require any access credentials.
	 */
	public final static int AUTH_NONE = 0;

	/**
	 * Authentication type which not require any access credentials.
	 */
	public final static int AUTH_LOGIN_PASSWORD = 1;
	
	/**
	 * SMTP server host name or IP address.
	 */
	private String smtpHost;
	
	/**
	 * SMTP server communication port.
	 */
	private int smtpPort;
	
	/**
	 * Server authentication type.
	 */
	private int authenticationType = AUTH_NONE;
	
	/**
	 * Login for password authentication.
	 */
	private String login;
	
	/**
	 * Password for password authentication.
	 */
	private String password;
	
	/**
	 * From email address.
	 */
	private String from;
	
	/**
	 * BCC mail
	 */
	private String bccField;
	
	/**
	 * Email encoding
	 */
	private String charsetEncoding;
	
	/**
	 * Session debug flag
	 */
	private Boolean sessionDebug = Boolean.FALSE; 
	
	/**
	 * Default constructor.
	 */
	public EmailSenderConfiguration() {
	}
	
	/**
	 * Load configuration from properties file. Open and load 
	 * configuration from file with name specified. Only UTF-8 encoding is supported.  
	 * 
	 * @param fileName the source file name.
	 * 
	 * @return configuration object.
	 * 
	 * @throws IOException if file couldn't be read due OS or access cause.
	 * 
	 * @throws FileNotFoundException if file is not exists or couldn't been opened.
	 */
	public static EmailSenderConfiguration buildConfiguration(String fileName) throws FileNotFoundException, IOException{
		return buildConfiguration(EmailSenderConfiguration.class.getResourceAsStream(fileName));
	}
	
	/**
	 * Load configuration from properties file opened as stream. Open and load 
	 * configuration from file from stream. Only UTF-8 encoding is supported.
	 * 
	 * @param is the source file input stream.
	 * 
	 * @return configuration object.
	 * 
	 * @throws IOExceptioyeppn if it is impossible to read data from the stream. 
	 */
	public static EmailSenderConfiguration buildConfiguration(InputStream is) throws IOException{
		EmailSenderConfiguration result = new EmailSenderConfiguration();
		
		return buildConfiguration(is, result);
	}

	/**
	 * Load configuration from properties file opened as stream. Open and load 
	 * configuration from file from stream. Only UTF-8 encoding is supported.
	 * 
	 * @param is source stream
	 * 
	 * @param configuration destination object
	 * 
	 * @return configuration object.
	 * 
	 * @throws IOException if it is impossible to read data from the stream. 
	 */
	
	public static EmailSenderConfiguration buildConfiguration(
			InputStream is, EmailSenderConfiguration configuration) throws IOException {

		Properties properties = new Properties();		
		properties.load(new InputStreamReader(is, Charset.forName("UTF-8")));
				
		configuration.setSmtpHost(properties.getProperty("mail.smtp.host", null));
		configuration.setSmtpPort(Integer.valueOf(properties.getProperty("mail.smtp.port", "25")).intValue());
		configuration.setAuthenticationType(Integer.valueOf(properties.getProperty("mail.smtp.auth", String.valueOf(AUTH_NONE))).intValue());
		configuration.setLogin(properties.getProperty("mail.login", null));
		configuration.setPassword(properties.getProperty("mail.password", null));
		configuration.setFrom(properties.getProperty("mail.from", configuration.getLogin() + '@' + configuration.getSmtpHost()));
		configuration.setCharsetEncoding(properties.getProperty("encoding"));
//		configuration.setBccField(properties.getProperty("mail.bcc"));
		configuration.setSessionDebug("true".equalsIgnoreCase(properties.getProperty("mail.debug")));
		return configuration;
	}
	
	/**
	 * @param smtpHost the smtpHost to set
	 */
	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}

	/**
	 * @return the smtpHost
	 */
	public String getSmtpHost() {
		return smtpHost;
	}

	/**
	 * @param smtpPort the smtpPort to set
	 */
	public void setSmtpPort(int smtpPort) {
		this.smtpPort = smtpPort;
	}

	/**
	 * @return the smtpPort
	 */
	public int getSmtpPort() {
		return smtpPort;
	}

	/**
	 * @param authenticationType the authenticationType to set
	 */
	public void setAuthenticationType(int authenticationType) {
		this.authenticationType = authenticationType;
	}

	/**
	 * @return the authenticationType
	 */
	public int getAuthenticationType() {
		return authenticationType;
	}

	/**
	 * @param login the login to set
	 */
	public void setLogin(String login) {
		this.login = login;
	}

	/**
	 * @return the login
	 */
	public String getLogin() {
		return login;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param from the from to set
	 */
	public void setFrom(String from) {
		this.from = from;
	}

	/**
	 * @return the from
	 */
	public String getFrom() {
		return from;
	}
	
	
	/**
	 * @return bcc
	 */
	public String getBccField() {
        return bccField;
    }

	/**
	 * @param bccField bcc
	 */
    public void setBccField(String bccField) {
        this.bccField = bccField;
    }
    
    
    /**
     * Returns email charset encoding
     * @return email charset encoding
     */
    public String getCharsetEncoding() {
        return charsetEncoding;
    }

    /**
     * Sets email charset encoding
     * @param email charset encoding
     */
    public void setCharsetEncoding(String charsetEncoding) {
        this.charsetEncoding = charsetEncoding;
    }
    
    /**
     * Gets session debug flag
     * @return session debug flag
     */
    public Boolean isSessionDebug() {
        return sessionDebug;
    }

    /**
     * Sets session debug flag
     * @param sessionDebug session debug flag
     */
    public void setSessionDebug(Boolean sessionDebug) {
        this.sessionDebug = sessionDebug;
    }
    
    @Override
	  public EmailSenderConfiguration clone() {
	      EmailSenderConfiguration esConfiguration = new EmailSenderConfiguration();
	      esConfiguration.setAuthenticationType(this.getAuthenticationType());
  	    esConfiguration.setFrom(this.getFrom());
	      esConfiguration.setLogin(this.getLogin());
	      esConfiguration.setPassword(this.getPassword());
	      esConfiguration.setSmtpHost(this.getSmtpHost());
	      esConfiguration.setSmtpPort(this.getSmtpPort());
	      esConfiguration.setBccField(this.getBccField());
	      esConfiguration.setCharsetEncoding(this.getCharsetEncoding());
	      esConfiguration.setSessionDebug(this.isSessionDebug());
        return esConfiguration;
	  }
	
}
