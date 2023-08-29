package no.auke.gui.general;

import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

public abstract class Forms extends JFrame {
    private static final long serialVersionUID = -6517347143430678699L;

    public Forms() {
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    }

    public Forms(GraphicsConfiguration gc) {
        super(gc);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    }

    public static void showMessage(String msg) {
        JOptionPane.showMessageDialog(null, msg);
    }

    public static void showMessage(Component comp, String msg) {
        JOptionPane.showMessageDialog(comp, msg);
    }

    public static Image getImageResource(String path, String description) {
        URL imageURL = Forms.class.getResource(path);
        if (imageURL == null) {
            System.err.println("Resource not found: " + path);
            return null;
        } else {
            return (new ImageIcon(imageURL, description)).getImage();
        }
    }

    protected void onCommand(Object source, String actionCommand) {
        Forms.showMessage(actionCommand);
    }
}
