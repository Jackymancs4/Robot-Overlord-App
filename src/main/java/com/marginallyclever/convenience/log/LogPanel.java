package com.marginallyclever.convenience.log;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LogPanel extends JPanel implements LogListener {
	private static final long serialVersionUID = -2753297349917155256L;
	private static final int LOG_LENGTH = 5000;

	// logging
	private DefaultListModel<String> listModel = new DefaultListModel<String>();
	private JList<String> logArea = new JList<String>(listModel);
	private JScrollPane logPane = new JScrollPane(logArea);
	private ConcurrentLinkedQueue<String> inBoundQueue = new ConcurrentLinkedQueue<String>();
	
	public LogPanel() {
		Log.addListener(this);

		this.setLayout(new BorderLayout());
		this.add(logPane, BorderLayout.CENTER);

		jumpToLogEnd();
	}

	private void jumpToLogEnd() {
		// did not work
		// JScrollBar vertical = logPane.getVerticalScrollBar();
		// vertical.setValue( vertical.getMaximum() );

		// works unreliably
		logArea.ensureIndexIsVisible(listModel.getSize() - 1);
	}

	private String cleanMessage(String msg) {
		msg = msg.trim();
		msg = msg.replace("\n", "<br>\n") + "\n";
		msg = msg.replace("\n\n", "\n");
		return msg;
	}

	// appends a message to the log tab and system out.
	@Override
	public void logEvent(String msg) {
		msg = cleanMessage(msg);
		if (msg.length() == 0) return;
		inBoundQueue.offer(msg);
		repaint();
	}
	
	@Override
	public void paint(Graphics g) {
		addMessages();
		super.paint(g);
	}
	
	private void addMessages() {
		while(!inBoundQueue.isEmpty()) {
			String msg = inBoundQueue.poll();
			if(msg!=null) addMessage(msg);
		}
	}
	
	private void addMessage(String msg) {
		int listSize = listModel.getSize() - 1;
		int lastVisible = logArea.getLastVisibleIndex();
		boolean isLast = (lastVisible == listSize);

		listModel.addElement(msg);
		trimLogPanel();
		if(isLast) jumpToLogEnd();
	}

	private int trimLogPanel() {
		int removed = 0;
		while (listModel.size() >= LOG_LENGTH) {
			listModel.remove(0);
			removed++;
		}
		return removed;
	}

	public void clearLog() {
		listModel.removeAllElements();
	}

	public static JFrame createFrame() {
		JFrame frame = new JFrame(Log.getLogLocation());
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setPreferredSize(new Dimension(600, 400));
		frame.add(new LogPanel());
		frame.pack();
		return frame;
	}

	public static void main(String[] args) {
		Log.start();
		JFrame frame = createFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
