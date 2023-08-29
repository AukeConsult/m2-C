package no.auke.gui.general;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;


import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class DialogPopup extends JDialog {
    private static final long serialVersionUID = -2335683744238025832L;
    
    private final JPanel contentPanel = new JPanel();
	
	public enum DialogResult {
		
		OK, CANCEL, NONE
	}

	
	private FormDialogInterface dialogImpl;
	private DialogResult Result = DialogResult.NONE;
	
	
	public DialogResult getDialogResult() {
		
		return Result;
	}
	
	
	public static DialogPopup showPopup(Frame parent, JPanel panel, String title) {
		
		
		DialogPopup popup = new DialogPopup(parent, title);
		
		popup.getContentPane().add(panel);
		popup.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		popup.setMinimumSize(new Dimension(panel.getSize().width, panel.getSize().height + 50));

		// popup.setSize(panel.getSize().width, panel.getSize().height + 50);
		
		if(panel instanceof FormDialogInterface) {
			
			popup.setDialogImpl((FormDialogInterface) panel);
		}
		
		if(popup.getDialogImpl()!=null) {
			
			popup.getDialogImpl().onOpen();
		}
		
		popup.pack(); 
		popup.setLocationRelativeTo(parent);
		popup.setVisible(true);
		
		return popup;
	}

	/**
	 * Create the dialog.
	 */
	
	public DialogPopup(Frame parent, String title) {
		super(parent, title, true);
		setAlwaysOnTop(true);
		
		//setBounds(100, 100, 562, 378);
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(new FlowLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		 addWindowListener( new WindowAdapter(){
        	 public void windowClosing( WindowEvent e ) {  
        		 
        		 Result = DialogResult.CANCEL;

        		 if(getDialogImpl() !=null) {
        			 
        			 getDialogImpl().onClose();
        		 }
             }  
        	 
        	 
        } );  
		
		getContentPane().add(contentPanel, BorderLayout.CENTER); {
			
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
			
						if(getDialogImpl() !=null) {
							
							if(getDialogImpl().Ok()) {
								
								setVisible(false);
								Result = DialogResult.OK;
								dispose();
								getDialogImpl().onClose();
								
							
							} else {
								
								Result = DialogResult.NONE;
							}
						
						} else {
							
							setVisible(false);
							Result = DialogResult.OK;
							dispose();
						}
					}
				});
				
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
				
					public void actionPerformed(ActionEvent e) {
						
						if(getDialogImpl() !=null) {
							
							if(getDialogImpl().Cancel()) {
								
								setVisible(false);
								Result = DialogResult.CANCEL;
								dispose();
								getDialogImpl().onClose();
							
							} else {
								
								Result = DialogResult.NONE;
							}
						
						} else {
							
							setVisible(false);
							Result = DialogResult.CANCEL;

							dispose();
							
						}
					}
				});
				
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

	public FormDialogInterface getDialogImpl() {
		
		return dialogImpl;
	}

	public void setDialogImpl(FormDialogInterface dialogImpl) {
		
		this.dialogImpl = dialogImpl;
	}

}
