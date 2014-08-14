package io.supermonkey.crawler.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.swingViewer.View;
import org.graphstream.ui.swingViewer.Viewer;
import io.supermonkey.crawler.hierarchy.Element;
import io.supermonkey.crawler.strategy.Command;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static io.supermonkey.crawler.ui.ColorSchema.*;

/**
 * @author Erik Nijkamp (erik.nijkamp@gmail.com)
 * @since 31.05.14
 */
public class TraversalViewer {

	private static final Log log = LogFactory.getLog(TraversalViewer.class);

	static {
		System.setProperty("org.graphstream.ui.renderer","org.graphstream.ui.j2dviewer.J2DGraphRenderer");
	}

	public static final int THUMBNAIL_HEIGHT = 250;

	private static final String styleSheet =
			"graph { padding: 100px; fill-color: #2b2b2b; }" +
			"node { size: 20px; fill-color: #bbb; text-color: #bbb; text-alignment: under; text-background-mode: rounded-box; text-background-color: #565656; text-padding: 5px, 4px; text-offset: 0px, 5px; }" +
			"node.current { fill-color: #ffc438; }" +
			"edge { fill-color: #bbb; text-color: #bbb; text-alignment: under; text-background-mode: rounded-box; text-background-color: #565656; text-padding: 5px, 4px; text-offset: 0px, 5px; }" +
			"edge.loop { text-alignment: left; text-background-mode: rounded-box; text-background-color: #565656; text-padding: 5px, 4px; text-offset: 20px, -25px; }";


	public static void main(String ... args) throws InterruptedException {
		final TraversalViewer viewer = TraversalViewer.create();
		for (int i = 0; i < 20; i++) {
			viewer.addScreenshotThumbnail(createDummyImage(), "DummyActivity");
		}

		viewer.fadeToast("Launching app ...");
		viewer.addViewTransition(createCommand(), createView(createId("old")), createView(createId("new")));
		viewer.addViewScreenshot(createView(createId("view")), createDummyImage());
		viewer.addViewTransition(createCommand(), createView(createId("new")), createView(createId("new")));
	}

	private static io.supermonkey.crawler.device.View.Id createId(final String name) {
		return new io.supermonkey.crawler.device.View.Id() {
			@Override
			public String getQualifiedName() {
				return name;
			}

			@Override
			public String getShortName() {
				return name;
			}
		};
	}

	private static Command createCommand() {
		return new Command.GoToHome();
	}

	private static io.supermonkey.crawler.device.View createView(io.supermonkey.crawler.device.View.Id viewId) {
		Element element = new Element(new HashMap<String, String>(), Element.Type.UNKNOWN, false, false);
		List<io.supermonkey.crawler.hierarchy.Node> childs = Collections.emptyList();
		io.supermonkey.crawler.hierarchy.Node elements = new io.supermonkey.crawler.hierarchy.Node(element, childs);

		return new io.supermonkey.crawler.device.View(viewId, elements);
	}

	private static BufferedImage createDummyImage() {
		BufferedImage image = new BufferedImage(400, 800, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, 400, 800);
		g.setColor(Color.gray);
		g.fillRect(100, 100, 100, 100);

		return image;
	}

	public static TraversalViewer create() {
		final TraversalViewer viewer = new TraversalViewer();
		final Lock lock = new ReentrantLock();
		final Condition isReady  = lock.newCondition();

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					viewer.open();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}

				lock.lock();
				try {
					isReady.signal();
				} finally {
					lock.unlock();
				}
			}
		});

		lock.lock();
		try {
			isReady.await();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			lock.unlock();
		}

		return viewer;
	}

	private final Graph graph = createGraph();
	private ToastGlassPane overlay;
	private JPanel images;

	private Graph createGraph() {
		Graph graph = new MultiGraph("Traversal");
		graph.addAttribute("ui.quality");
		graph.addAttribute("ui.antialias");
		graph.addAttribute("ui.stylesheet", styleSheet);

		return graph;
	}

	private void open() throws Exception {
		final Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		viewer.enableAutoLayout();
		final View view = viewer.addDefaultView(false);

		JPanel panel = new JPanel();
		panel.setBackground(DARK_GRAY);
		panel.setLayout(new FlowLayout());

		{
			JButton button = createFlatButton("Reset");
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent actionEvent) {
					view.getCamera().resetView();
				}
			});
			panel.add(button);
		}
		{
			JButton button = createFlatButton("Zoom out");
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent actionEvent) {
					view.getCamera().setViewPercent(2.0);
				}
			});
			panel.add(button);
		}
		{
			JButton button = createFlatButton("Zoom in");
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent actionEvent) {
					view.getCamera().setViewPercent(0.5);
				}
			});
			panel.add(button);
		}

		images = new JPanel();
		images.setLayout(new FlowLayout());
		images.setBackground(MEDIUM_GRAY);

		JScrollPane scrollPane = new JScrollPane(images, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setPreferredSize(new Dimension(100, THUMBNAIL_HEIGHT));

		JScrollBar scrollbar = scrollPane.getHorizontalScrollBar();
		scrollbar.setUI(new CustomScrollbarUI());

		JFrame frame = new JFrame("SuperMonkey");
		frame.setBackground(DARK_GRAY);
		frame.setVisible(true);
		frame.setSize(800, 800);
		frame.setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.add(panel, BorderLayout.PAGE_START);
		frame.add(view, BorderLayout.CENTER);
		frame.add(scrollPane, BorderLayout.PAGE_END);

		overlay = new ToastGlassPane(frame);
		frame.setGlassPane(overlay);
		overlay.setVisible(true);
	}

	private static JButton createFlatButton(String text) {
		JButton button = new JButton(text);
		button.setForeground(LIGHT_GRAY);
		button.setBackground(MEDIUM_GRAY);
		button.setBorderPainted(false);
		button.setFocusPainted(false);
		button.setContentAreaFilled(true);
		button.setPreferredSize(new Dimension(110, 25));
		button.setFont(new Font("Arial", Font.PLAIN, 12));
		return button;
	}

	public void showToast(String text) {
		this.overlay.setToast(text, false);
	}

	public void fadeToast(String text) {
		this.overlay.setToast(text, true);
	}

	public void addViewTransition(Command command, io.supermonkey.crawler.device.View oldView, io.supermonkey.crawler.device.View newView) {

		org.graphstream.graph.Node oldNode = getOrCreateNode(oldView);
		org.graphstream.graph.Node newNode = getOrCreateNode(newView);

		oldNode.setAttribute("ui.label", oldView.getId().getShortName());
		newNode.setAttribute("ui.label", newView.getId().getShortName());

		for(Node node : graph.getEachNode()) {
			node.removeAttribute("ui.class");
		}
		newNode.addAttribute("ui.class", "current");

		String edgeLabel = toString(command);
		Edge edge = getOrCreateEdge(toEdgeId(edgeLabel, oldNode, newNode), oldNode, newNode);
		if(edge.isLoop()) {
			edge.addAttribute("ui.class", "loop");
		}
		//edge.setAttribute("ui.label", edgeLabel);
	}

	private String toString(Command command) {
		return command.getId().toString();
	}

	public void addViewScreenshot(io.supermonkey.crawler.device.View view, BufferedImage image) {
		addScreenshotThumbnail(image, view.getId().getShortName());
	}

	private void addScreenshotThumbnail(BufferedImage screenshot, String title) {
		final int SCROLLBAR_HEIGHT = 27;
		final int TITLE_HEIGHT = 18;
		double scale = (double) (THUMBNAIL_HEIGHT - SCROLLBAR_HEIGHT - TITLE_HEIGHT) / screenshot.getHeight();

		int w = (int)(screenshot.getWidth() * scale);
		int h = (int)(screenshot.getHeight() * scale);
		BufferedImage thumbnail = resizeImage(screenshot, w, h);

		JLabel label = new JLabel(new ImageIcon(thumbnail));
		label.setText(title);
		label.setHorizontalTextPosition(JLabel.CENTER);
		label.setVerticalTextPosition(JLabel.BOTTOM);
		label.setForeground(LIGHT_GRAY);
		label.setFont(new Font("Arial", Font.PLAIN, 10));

		images.add(label);
		images.updateUI();
	}

	private static BufferedImage resizeImage(BufferedImage original, int newWidth, int newHeight) {
		BufferedImage resized = new BufferedImage(newWidth, newHeight, original.getType());
		Graphics2D g = resized.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(original, 0, 0, newWidth, newHeight, 0, 0, original.getWidth(), original.getHeight(), null);
		g.dispose();

		return resized;
	}

	private String toEdgeId(String prefix, Node node1, Node node2) {
		String[] nodeIds = { node1.getId(), node2.getId() };
		Arrays.sort(nodeIds);
		String id = prefix + "_" + nodeIds[0] + "_" + nodeIds[1];

		return id;
	}

	private Edge getOrCreateEdge(String id, Node oldNode, Node newNode) {
		Edge edge = graph.getEdge(id);
		if(edge == null) {
			return graph.addEdge(id, oldNode.getId(), newNode.getId());
		} else {
			return edge;
		}
	}

	private Node getOrCreateNode(io.supermonkey.crawler.device.View view) {
		if(graph.getNode(view.getId().getQualifiedName()) != null) {
			return graph.getNode(view.getId().getQualifiedName());
		} else {
			return graph.addNode(view.getId().getQualifiedName());
		}
	}
}
