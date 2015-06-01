package PongGame_UI.main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import models.Coordinates;
import models.PongBall;

public class PongPanel extends JPanel implements ActionListener, KeyListener {

	private boolean showTitleScreen = true;
	private boolean playing = false;
	private boolean gameOver = false;

	private boolean upPressed = false;
	private boolean downPressed = false;
	private boolean wPressed = false;
	private boolean sPressed = false;

	private PongBall ball;

	private int ballDeltaX = -1;
	private int ballDeltaY = 3;

	private int playerOneX;
	private int playerOneY = 250;
	private int playerOneWidth = 10;
	private int playerOneHeight = 50;

	private int playerTwoX;
	private int playerTwoY = 250;
	private int playerTwoWidth = 10;
	private int playerTwoHeight = 50;

	private int paddleSpeed = 5;

	private int playerOneScore = 0;
	private int playerTwoScore = 0;

	// construct a PongPanel
	public PongPanel() {
		
		int ballX = 250;
		int ballY = 250;
		int diameter = 20;

		Coordinates coordinates = Coordinates.create().withX(ballX)
				.withY(ballY).build();

		ball = PongBall.create().withId(1).withDiameter(diameter)
				.withCoordinates(coordinates).build();

		setBackground(Color.BLACK);

		// listen to key presses
		setFocusable(true);
		addKeyListener(this);

		// call step() 60 fps
		Timer timer = new Timer(1000 / 60, this);
		timer.start();
	}

	public void actionPerformed(ActionEvent e) {
		step();
	}

	public void step() {

		if (playing) {
			// move player 1
			if (upPressed) {
				if (playerOneY - paddleSpeed > 0) {
					playerOneY -= paddleSpeed;
				}
			}
			if (downPressed) {
				if (playerOneY + paddleSpeed + playerOneHeight < getHeight()) {
					playerOneY += paddleSpeed;
				}
			}

			// move player 2
			if (wPressed) {
				if (playerTwoY - paddleSpeed > 0) {
					playerTwoY -= paddleSpeed;
				}
			}
			if (sPressed) {
				if (playerTwoY + paddleSpeed + playerTwoHeight < getHeight()) {
					playerTwoY += paddleSpeed;
				}
			}

			// where will the ball be after it moves?
			int nextBallLeft = ball.getCoordinates().getX() + ballDeltaX;
			int nextBallRight = ball.getCoordinates().getX()
					+ ball.getDiameter() + ballDeltaX;
			int nextBallTop = ball.getCoordinates().getY() + ballDeltaY;
			int nextBallBottom = ball.getCoordinates().getY()
					+ ball.getDiameter() + ballDeltaY;

			int playerOneRight = playerOneX + playerOneWidth;
			int playerOneTop = playerOneY;
			int playerOneBottom = playerOneY + playerOneHeight;

			float playerTwoLeft = playerTwoX;
			float playerTwoTop = playerTwoY;
			float playerTwoBottom = playerTwoY + playerTwoHeight;

			// ball bounces off top and bottom of screen
			if (nextBallTop < 0 || nextBallBottom > getHeight()) {
				ballDeltaY *= -1;
			}

			// will the ball go off the left side?
			if (nextBallLeft < playerOneRight) {
				// is it going to miss the paddle?
				if (nextBallTop > playerOneBottom
						|| nextBallBottom < playerOneTop) {

					playerTwoScore++;

					ball.getCoordinates().setX(250);
					ball.getCoordinates().setY(250);
				} else {
					ballDeltaX *= -1;
				}
			}

			// will the ball go off the right side?
			if (nextBallRight > playerTwoLeft) {
				// is it going to miss the paddle?
				if (nextBallTop > playerTwoBottom
						|| nextBallBottom < playerTwoTop) {

					playerOneScore++;

					ball.getCoordinates().setX(250);
					ball.getCoordinates().setY(250);
				} else {
					ballDeltaX *= -1;
				}
			}

			// move the ball
			ball.getCoordinates().setX(
					ball.getCoordinates().getX() + ballDeltaX);
			ball.getCoordinates().setY(
					ball.getCoordinates().getY() + ballDeltaY);
		}

		// stuff has moved, tell this JPanel to repaint itself
		repaint();
	}

	// paint the game screen
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.WHITE);

		int width = (int) g.getClipBounds().getWidth();
		int height = (int) g.getClipBounds().getHeight();
		
		playerTwoX = width - width / 10;
		playerOneX = width / 10;

		if (showTitleScreen) {

			g.setFont(new Font(Font.DIALOG, Font.BOLD, 36));
			g.setFont(new Font(Font.DIALOG, Font.BOLD, 36));
			g.drawString("Pong", width / 2, height / 2);

			g.setFont(new Font(Font.DIALOG, Font.BOLD, 18));

			g.drawString("Press 'P' to play.", width / 2, (int) height / 2 + 80);

		} else if (playing) {

			int playerOneRight = playerOneX + playerOneWidth;
			int playerTwoLeft = playerTwoX;

			// draw dashed line down center
			for (int lineY = 0; lineY < getHeight(); lineY += 50) {
				g.drawLine(width / 2, lineY, width / 2, lineY + 25);
			}

			// draw "goal lines" on each side
			g.drawLine(playerOneRight, 0, playerOneRight, getHeight());
			g.drawLine(playerTwoLeft, 0, playerTwoLeft, getHeight());

			// draw the scores
			g.setFont(new Font(Font.DIALOG, Font.BOLD, 36));
			g.drawString(String.valueOf(playerOneScore), (int) (width / 3.5), 100);
			g.drawString(String.valueOf(playerTwoScore), (int) (width / 1.5), 100);

			// draw the ball
			g.fillOval(ball.getCoordinates().getX(), ball.getCoordinates()
					.getY(), ball.getDiameter(), ball.getDiameter());

			// draw the paddles
			g.fillRect(playerOneX, playerOneY, playerOneWidth, playerOneHeight);
			g.fillRect(playerTwoX, playerTwoY, playerTwoWidth, playerTwoHeight);
		} else if (gameOver) {

			g.setFont(new Font(Font.DIALOG, Font.BOLD, 36));
			g.drawString(String.valueOf(playerOneScore), 100, 100);
			g.drawString(String.valueOf(playerTwoScore), 400, 100);

			g.setFont(new Font(Font.DIALOG, Font.BOLD, 36));
			if (playerOneScore > playerTwoScore) {
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
				playing = true;
			}
		} else if (playing) {
			if (e.getKeyCode() == KeyEvent.VK_UP) {
				upPressed = true;
			} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
				downPressed = true;
			} else if (e.getKeyCode() == KeyEvent.VK_W) {
				wPressed = true;
			} else if (e.getKeyCode() == KeyEvent.VK_S) {
				sPressed = true;
			}
		} else if (gameOver) {
			if (e.getKeyCode() == KeyEvent.VK_SPACE) {
				gameOver = false;
				showTitleScreen = true;
				playerOneY = 250;
				playerTwoY = 250;
				ball.getCoordinates().setX(250);
				ball.getCoordinates().setY(250);
				playerOneScore = 0;
				playerTwoScore = 0;
			}
		}
	}

	public void keyReleased(KeyEvent e) {
		if (playing) {
			if (e.getKeyCode() == KeyEvent.VK_UP) {
				upPressed = false;
			} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
				downPressed = false;
			} else if (e.getKeyCode() == KeyEvent.VK_W) {
				wPressed = false;
			} else if (e.getKeyCode() == KeyEvent.VK_S) {
				sPressed = false;
			}
		}
	}

}