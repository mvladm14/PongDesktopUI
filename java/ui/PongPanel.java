package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JPanel;
import javax.swing.Timer;

import logic.PongController;
import models.Screen;

@SuppressWarnings("serial")
public class PongPanel extends JPanel implements ActionListener, KeyListener {

	private PongController controller;

	private Screen screen;

	private boolean showTitleScreen = true;

	// construct a PongPanel
	public PongPanel() {

		initializeNonUIfields();

		initializeUIFields();

	}

	private void initializeUIFields() {
		setBackground(Color.BLACK);

		// listen to key presses
		setFocusable(true);
		addKeyListener(this);

		// call step() 60 fps
		Timer timer = new Timer(1000 / 100, this);
		timer.start();

	}

	private void initializeNonUIfields() {
		screen = new Screen();
		controller = new PongController(screen);
	}

	public void actionPerformed(ActionEvent e) {
		step();
	}

	public void step() {

		controller.performStep(screen.getHeight(), screen.getWidth());
		// stuff has moved, tell this JPanel to repaint itself
		repaint();
	}

	// paint the game screen
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.WHITE);

		screen.setWidth((int) g.getClipBounds().getWidth());
		screen.setHeight((int) g.getClipBounds().getHeight());

		controller.getPlayer1().getHittableRegion()
				.setX(screen.getWidth() / 10);
		controller.getPlayer2().getHittableRegion()
				.setX(screen.getWidth() * 9 / 10);

		controller.getPallet().getCoordinates()
				.setX(controller.getPlayer1().getHittableRegion().getX());
		controller.getPallet2().getCoordinates()
				.setX(controller.getPlayer2().getHittableRegion().getX());

		if (showTitleScreen) {

			g.setFont(new Font(Font.DIALOG, Font.BOLD, 36));
			g.setFont(new Font(Font.DIALOG, Font.BOLD, 36));
			g.drawString("Pong", screen.getWidth() / 2, screen.getHeight() / 2);

			g.setFont(new Font(Font.DIALOG, Font.BOLD, 18));

			g.drawString("Press 'P' to play.", screen.getWidth() / 2,
					(int) screen.getHeight() / 2 + 80);

		} else if (controller.isPlaying()) {

			int playerOneRight = controller.getPlayer1().getHittableRegion()
					.getX();
			int playerTwoLeft = controller.getPlayer2().getHittableRegion()
					.getX();

			// draw dashed line down center
			for (int lineY = 0; lineY < getHeight(); lineY += 50) {
				g.drawLine(screen.getWidth() / 2, lineY, screen.getWidth() / 2,
						lineY + 25);
			}

			// draw "goal lines" on each side
			g.drawLine(playerOneRight, 0, playerOneRight, getHeight());
			g.drawLine(playerTwoLeft, 0, playerTwoLeft, getHeight());

			// draw the scores
			g.setFont(new Font(Font.DIALOG, Font.BOLD, 36));
			g.drawString(String.valueOf(controller.getPlayer1().getScore()),
					(int) (screen.getWidth() / 3.5), 100);
			g.drawString(String.valueOf(controller.getPlayer2().getScore()),
					(int) (screen.getWidth() / 1.5), 100);

			// draw the ball
			g.fillOval(controller.getBall().getCoordinates().getX(), controller
					.getBall().getCoordinates().getY(), controller.getBall()
					.getDiameter(), controller.getBall().getDiameter());

			// draw the players' pallets
			g.drawRect(controller.getPallet().getCoordinates().getX(),
					controller.getPallet().getCoordinates().getY(), controller
							.getPallet().getDimension().getWidth(), controller
							.getPallet().getDimension().getHeight());

			g.drawRect(controller.getPallet2().getCoordinates().getX(),
					controller.getPallet2().getCoordinates().getY(), controller
							.getPallet2().getDimension().getWidth(), controller
							.getPallet2().getDimension().getHeight());

		} else if (controller.isGameOver()) {

			g.setFont(new Font(Font.DIALOG, Font.BOLD, 36));
			g.drawString(String.valueOf(controller.getPlayer1().getScore()),
					100, 100);
			g.drawString(String.valueOf(controller.getPlayer2().getScore()),
					400, 100);

			g.setFont(new Font(Font.DIALOG, Font.BOLD, 36));
			if (controller.getPlayer1().getScore() > controller.getPlayer2()
					.getScore()) {
				g.drawString("Player 1 Wins!", 165, 200);
			} else {
				g.drawString("Player 2 Wins!", 165, 200);
			}

			g.setFont(new Font(Font.DIALOG, Font.BOLD, 18));
			g.drawString("Press space to restart.", 150, 400);
		}
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
		if (showTitleScreen) {
			if (e.getKeyCode() == KeyEvent.VK_P) {
				showTitleScreen = false;
				controller.setPlaying(true);
			}
		} else if (controller.isPlaying()) {

		} else if (controller.isGameOver()) {
			if (e.getKeyCode() == KeyEvent.VK_SPACE) {
				controller.setGameOver(false);
				showTitleScreen = true;
				controller.getBall().getCoordinates().setX(250);
				controller.getBall().getCoordinates().setY(250);
				controller.getPlayer1().setScore(0);
				controller.getPlayer2().setScore(0);
			}
		}
	}

	public void keyReleased(KeyEvent arg0) {
	}
}