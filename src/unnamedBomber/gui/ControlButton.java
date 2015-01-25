package unnamedBomber.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Created by DrElo on 13.05.2015.
 */
public class ControlButton extends JButton {

    private int playernr;
    private int key;

    public ControlButton(String text, ImageIcon ii, int pnr, int k) {
        super(text, ii);
        this.playernr = pnr;
        this.key = k;
        this.setPreferredSize(new Dimension(64, 64));
        this.setHorizontalTextPosition(SwingConstants.CENTER);
        this.setContentAreaFilled(false);
        this.setBorderPainted(false);
        this.setBorder(null);
        this.addKeyListener(new ControlButtonObserver(this));
    }

//    @Override public void paintComponent(Graphics g) {
//        super.paintComponent(g);
//        Graphics2D g2d = (Graphics2D) g;
//        int width = getSize().width;
//        int height = getSize().height;
//        g2d.setPaint(Color.WHITE);
//
//        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//
//        FontRenderContext frc = g2d.getFontRenderContext();
//        TextLayout tlayout = new TextLayout(getText(), getFont(), frc);
//        AffineTransform transform = g2d.getTransform();
//        Shape outline = tlayout.getOutline(transform);
//        Rectangle bounds = outline.getBounds();
//        transform.translate(width / 2 - (bounds.width / 2), height / 2 + (bounds.height / 2));
//        g2d.transform(transform);
//        g2d.setColor(Color.BLACK);
//        g2d.draw(outline);
//        g2d.setColor(Color.WHITE);
//        g2d.fill(outline);
//        g2d.setClip(outline);
//    }

    public int getPlayernr() {
        return playernr;
    }

    public int getKey() {
        return key;
    }
}
