package no.auke.gui.general;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JPanel;

public class FancyFrameController {

    // ///////////DO NOT MODIFY/////////////////
    private boolean isShapingSupported;
    private boolean isOpacityControlSupported;
    private boolean isTranslucencySupported;
    private GraphicsConfiguration translucencyCapableGC;
    private ComponentListener shapeListener = null;

    public enum FrameShape {
        RECTANGULAR, ROUNDEDCONNERS, OVAL
    }

    public GraphicsConfiguration getGraphicsConfiguration() {
        return translucencyCapableGC;
    }

    // ////////////////////////////////////////////

    // //////////CONFIG HERE///////////////////////
    private FrameShape theShape = FrameShape.RECTANGULAR;
    private int opacityLevel = 100;
    private boolean perPixelTranslucencyEffect = false;
    private boolean gradient = false;

    // ///////////////////////////////////////////

    public FancyFrameController(FrameShape shape, int opacityLevel,
            boolean perPixelTranslucencyEffect, boolean showGradient) {

        this.theShape = shape;
        this.opacityLevel = opacityLevel;
        this.perPixelTranslucencyEffect = perPixelTranslucencyEffect;
        this.gradient = showGradient;

        isShapingSupported = AWTUtilitiesWrapper
                .isTranslucencySupported(AWTUtilitiesWrapper.PERPIXEL_TRANSPARENT);
        isOpacityControlSupported = AWTUtilitiesWrapper
                .isTranslucencySupported(AWTUtilitiesWrapper.TRANSLUCENT);
        isTranslucencySupported = AWTUtilitiesWrapper
                .isTranslucencySupported(AWTUtilitiesWrapper.PERPIXEL_TRANSLUCENT);
        translucencyCapableGC = GraphicsEnvironment
                .getLocalGraphicsEnvironment().getDefaultScreenDevice()
                .getDefaultConfiguration();

        if (!AWTUtilitiesWrapper.isTranslucencyCapable(translucencyCapableGC)) {
            translucencyCapableGC = null;

            GraphicsEnvironment env = GraphicsEnvironment
                    .getLocalGraphicsEnvironment();
            GraphicsDevice[] devices = env.getScreenDevices();

            for (int i = 0; i < devices.length && translucencyCapableGC == null; i++) {
                GraphicsConfiguration[] configs = devices[i]
                        .getConfigurations();
                for (int j = 0; j < configs.length
                        && translucencyCapableGC == null; j++) {
                    if (AWTUtilitiesWrapper.isTranslucencyCapable(configs[j])) {
                        translucencyCapableGC = configs[j];
                    }
                }
            }
            if (translucencyCapableGC == null) {
                isTranslucencySupported = false;
            }
        }

        if (!isShapingSupported) {
            theShape = FrameShape.RECTANGULAR;
        }

        if (!isOpacityControlSupported) {
            opacityLevel = 100;
        }

        if (!isTranslucencySupported) {
            perPixelTranslucencyEffect = false;
        }

    }

    public void resetOpacity(int level, Window w) {
        this.opacityLevel = level;
        applyOpacity(w);
    }
    
    public synchronized void applyOpacity(Window fancyFrame) {
        if (!isOpacityControlSupported) {
            return;
        }
        if (fancyFrame == null) {
            return;
        }
        AWTUtilitiesWrapper.setWindowOpacity(fancyFrame, opacityLevel / 100.0f);
        if (perPixelTranslucencyEffect) {
            AWTUtilitiesWrapper.setWindowOpaque(fancyFrame, false);
        }
    }

    public synchronized void applyGradient(JPanel jPanel) {
        Graphics g = jPanel.getGraphics();
        if (g instanceof Graphics2D && gradient) {
            final int R = 240;
            final int G = 240;
            final int B = 240;

            Paint p = new GradientPaint(0.0f, 0.0f, new Color(R, G, B, 0),
                    jPanel.getWidth(), jPanel.getHeight(), new Color(R, G, B,
                            255), true);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setPaint(p);
            g2d.fillRect(0, 0, jPanel.getWidth(), jPanel.getHeight());
        }
        jPanel.paintComponents(g);
        jPanel.setOpaque(!gradient);
        if (!gradient) {
            jPanel.setBackground(new Color(240, 240, 240, 128));
        }
    }

    public synchronized void applyShape(Window fancyFrame) {
        if (!isShapingSupported) {
            return;
        }
        if (fancyFrame == null) {
            return;
        }
        if (shapeListener != null) {
            shapeListener.componentResized(null);
            return;
        }

        final Window fd = fancyFrame;
        fd.addComponentListener(shapeListener = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent evt) {
                Shape shapeKind = null;

                if (theShape == FrameShape.ROUNDEDCONNERS) {
                    shapeKind = new RoundRectangle2D.Float(0, 0, fd.getWidth(),
                            fd.getHeight(), 30, 30);
                } else if (theShape == FrameShape.OVAL) {
                    shapeKind = new Ellipse2D.Float(0, 0, fd.getWidth(), fd
                            .getHeight());
                }
                AWTUtilitiesWrapper.setWindowShape(fd, shapeKind);
            }
        });
    }

}
