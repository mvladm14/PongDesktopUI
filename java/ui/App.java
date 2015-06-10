package ui;

import java.awt.BorderLayout;

import javax.swing.JFrame;

public class App {
	public static void main(String[] args) {

		JFrame frame = new JFrame("Pong");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());

		PongPanel pongPanel = new PongPanel();
		frame.add(pongPanel, BorderLayout.CENTER);

		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);

	}
}
