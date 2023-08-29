package no.auke.gui.general;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;


public class TextScroller extends javax.swing.JScrollPane {
	JTextArea textArea = new JTextArea();
	boolean direction=false;
	public TextScroller(boolean direction) {
		this.direction = direction;
		setViewportView(textArea);
	}
	private static final long serialVersionUID = 3136415449246306621L;
	
	private synchronized void settxtMessage(final String txt) {

		try {

			if (textArea.getText().length() > 10000) {
				textArea.setText("");
			}

			StringBuilder builder = new StringBuilder();

			if(direction) {

				builder.append(textArea.getText());
				builder.append(System.getProperty("line.separator"));
				builder.append(txt);
				
			} else {

				builder.append(txt);
				builder.append(System.getProperty("line.separator"));
				builder.append(textArea.getText());
				
			}
			textArea.setText(builder.toString());
			textArea.setCaretPosition(builder.length() - 1);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setText(final String message) {

		if (SwingUtilities.isEventDispatchThread()) {
			settxtMessage(message);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					settxtMessage(message);
				}

			});
		}
	}
}
