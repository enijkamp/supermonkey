package io.supermonkey.crawler.ui;

import javax.swing.*;
import javax.swing.plaf.metal.MetalScrollBarUI;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
* @author Erik Nijkamp
* @since 01.06.14
*/
public class CustomScrollbarUI extends MetalScrollBarUI {

	private final Image imageThumb = createMockImage(32, 32, ColorSchema.DARK_GRAY);
	private final Image imageTrack = createMockImage(32, 32, ColorSchema.MEDIUM_GRAY);
	private final JButton invisibleButton = new JButton() {
		@Override
		public Dimension getPreferredSize() {
			return new Dimension(0, 0);
		}
	};

	@Override
	protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
		g.setColor(ColorSchema.DARK_GRAY);
		((Graphics2D) g).drawImage(imageThumb, r.x, r.y, r.width, r.height, null);
	}

	@Override
	protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
		((Graphics2D) g).drawImage(imageTrack, r.x, r.y, r.width, r.height, null);
	}

	@Override
	protected JButton createDecreaseButton(int orientation) {
		return invisibleButton;
	}

	@Override
	protected JButton createIncreaseButton(int orientation) {
		return invisibleButton;
	}

	public static Image createMockImage(int w, int h, Color c) {
		BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = image.createGraphics();
		g2d.setPaint(c);
		g2d.fillRect(0, 0, w, h);
		g2d.dispose();

		return image;
	}
}
