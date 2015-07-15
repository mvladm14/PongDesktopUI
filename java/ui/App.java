package ui;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

public class App {

	public static void main(String[] args) {

		JFrame frame = new JFrame("Pong");

		frame.setLayout(new BorderLayout());

		final PongPanel pongPanel = new PongPanel();
		frame.add(pongPanel, BorderLayout.CENTER);

		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				pongPanel.getPongController().stopServers();
			}
		});

	}
}
