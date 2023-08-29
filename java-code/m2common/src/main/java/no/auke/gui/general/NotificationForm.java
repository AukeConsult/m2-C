package no.auke.gui.general;

import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class NotificationForm extends JWindow {
    private static final long serialVersionUID = 1L;
   
    public NotificationForm() {
        this(null);
    }

    FancyFrameController transparencyController;
    
    JLabel lbLeak = new JLabel();

    public NotificationForm(FancyFrameController transparencyController) {
        super(transparencyController != null ? transparencyController
                .getGraphicsConfiguration() : null);

        Rectangle clientArea = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        
        int startX = clientArea.x + clientArea.width - 350;
        int startY = clientArea.y + clientArea.height - 120;
        setLocation(startX, startY);

        JPanel panel = new JPanel();
        panel.addMouseListener(new MouseAdapter() {
            
        	@Override
            
        	public void mouseClicked(MouseEvent e) {

        		if (clickevent != null) {
                
        			clickevent.onClick();
                	
                }
            }
        });
        
        getContentPane().add(panel, BorderLayout.CENTER);
        // panel.setLayout(new BorderLayout(0, 0));
        panel.setBackground(SystemColor.inactiveCaption);
        panel.setLayout(new BorderLayout(0, 0));

        lbLeak.setHorizontalTextPosition(SwingConstants.CENTER);
        lbLeak.setFont(new Font("Tahoma", Font.PLAIN, 14));
        lbLeak.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lbLeak);

        this.transparencyController = transparencyController;

        if (this.transparencyController != null) {
        
        	this.transparencyController.applyShape(this);
            this.transparencyController.applyOpacity(this);

        }
        
        setPreferredSize(new Dimension(250, 70));
        setAlwaysOnTop(true);

    }

    public interface IOnClick {
        void onClick();
    }

    public IOnClick clickevent;

    public void setOnClick(IOnClick handler) {
        
    	clickevent = handler;

    }

  	private JWindow window=this;
	private int count = 100;
   
    public void setMessage(String message) {

    	setVisible(true);
        lbLeak.setText(message);
        
        transparencyController.resetOpacity(count, window);

        count = 100;
        timer.restart();

    }

	private Timer timer = new Timer(200, new ActionListener() {
        
        @Override
        public void actionPerformed(ActionEvent arg0) {
            
        	//notifier.setVisible(!clientMain.getMainFrame().isActive());
    
            if (isVisible()) {
              
            	count = count - 2;
            	if (count == 0) {
                
            		timer.stop();
                    count = 100;
                    setVisible(false);
                    
                }
            	
                // HUYDO: animate the window
                transparencyController.resetOpacity(count, window);
            
            } else {
            
            	timer.stop();
                count = 100;
                
            }
        }

    });    

}
