package io.supermonkey.crawler.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;

import static io.supermonkey.crawler.ui.ColorSchema.*;

/**
* @author Erik Nijkamp
* @since 01.06.14
*/
public class ToastGlassPane extends JComponent {

	class Animator implements ActionListener {
		final int frequency = 50;
		final int delay = 4000;
		final int fading = 1000;
		final Timer timer = new Timer(frequency, this);

		boolean isVisible = false;
		long begin = now();
		long end = now();
		int alpha = 255;
		boolean fadeOut = false;

		public Animator() {
			timer.setInitialDelay(delay);
		}

		public void start(boolean fadeOut) {
			// transition
			this.fadeOut = fadeOut;
			this.alpha = 255;
			this.isVisible = true;
			this.begin = now() + delay;
			this.end = now() + delay + fading;

			// repaint
			frame.repaint();
			if(fadeOut) {
				timer.start();
			}
		}

		public boolean isVisible() {
			return isVisible;
		}

		public int getAlpha() {
			return alpha;
		}

		public void actionPerformed(ActionEvent evt) {
			float total = (end - begin);
			float current = (now() - begin);
			float progress = Math.min(1f, (current / total));

			this.alpha = (int) (bounds(1f - progress) * 255);

			if (now() > end) {
				this.isVisible = false;
				timer.stop();
			}

			frame.repaint();
		}

		private float bounds(float value) {
			if(value < 0f) {
				return 0f;
			}
			if(value > 1f) {
				return 1f;
			}
			return value;
		}

		private long now() {
			return System.currentTimeMillis();
		}
	}

	private final JFrame frame;
	private final Font font = new Font("Arial", Font.PLAIN, 26);
	private final Animator animator = new Animator();

	private String text;

	public ToastGlassPane(JFrame frame) {
		this.frame = frame;
	}

	public void setToast(String text, boolean fadeOut) {
		this.text = text;
		this.animator.start(fadeOut);
	}

	public void paint(Graphics g) {

		if(animator.isVisible() == false) {
			return;
		}

		Graphics2D g2 = (Graphics2D) g.create();
		{
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

			FontMetrics metrics = g2.getFontMetrics(font);
			Rectangle2D bounds = metrics.getStringBounds(text, g2);

			final int padding = 40;

			final int rectH = font.getSize() + padding;
			final int rectW = (int) (bounds.getWidth()) + padding;

			final int frameW = frame.getWidth();
			final int frameH = frame.getHeight();

			final int rectX = ((frameW - rectW) / 2);
			final int rectY = ((frameH - rectH) / 2) - 100;

			final int textX = rectX + (padding / 2);
			final int textY = rectY + (padding / 2) + font.getSize() - 5;

			final int a = animator.getAlpha();

			g2.setColor(withAlpha(MEDIUM_GRAY, a));
			g2.fillRoundRect(rectX, rectY, rectW, rectH, 20, 20);

			g2.setFont(font);
			g2.setColor(withAlpha(LIGHT_GRAY, a));
			g2.drawString(text, textX, textY);
		}
		g2.dispose();
	}

	private Color withAlpha(Color c, int a) {
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
	}
}
