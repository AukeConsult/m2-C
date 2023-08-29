package no.auke.mail;

import java.util.List;

public class EmailMessages {
	private List<String> mailTo;
	private String[] fileName;

	public EmailMessages(List<String> to, String[] fileName){
		this.mailTo = to;
		this.fileName = fileName;
	}

	public List<String> getMailTo() {
		return mailTo;
	}

	public String[] getFileName() {
		return fileName;
	}
}