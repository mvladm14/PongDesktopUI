package PongGame_UI.main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JPanel;
import javax.swing.Timer;

import restInterfaces.PlayerSvcApi;
import restInterfaces.PongBallSvcApi;
import retrofit.RestAdapter;
import models.BallCoordinates;
import models.PhoneCoordinates;
import models.PongBall;
import models.Screen;

@SuppressWarnings("serial")
public class PongPanel extends JPanel implements ActionListener, KeyListener {

	private Screen screen;

	private PongBall ball;

	private boolean player1CanHit = true;
	private boolean player2CanHit = false;

	// private static final String SERVER =
	// "http://131.254.101.102:8080/myriads";
	private static final String SERVER = "http://131.254.101.102:8080/PongServerSide";

	private PongBallSvcApi pongBallSvc = new RestAdapter.Builder()
			.setEndpoint(SERVER).build().create(PongBallSvcApi.class);
	private PlayerSvcApi playerSvc = new RestAdapter.Builder()
			.setEndpoint(SERVER).build().create(PlayerSvcApi.class);

	private boolean showTitleScreen = true;
	private boolean playing = false;
	private boolean gameOver = false;

	private int ballDeltaX = -1;
	private int ballDeltaY = 3;

	private int playerOneX;
	private int playerOneWidth = 10;

	private int playerTwoX;

	private int playerOneScore = 0;
	private int playerTwoScore = 0;

	private boolean ballCanBeHit = true;

	private boolean ballCanBeHit2 = true;

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
		Timer timer = new Timer(1000 / 60, this);
		timer.start();

	}

	private void initializeNonUIfields() {

		screen = new Screen();

		int ballX = 250;
		int ballY = 250;
		int diameter = 20;

		BallCoordinates coordinates = BallCoordinates.create().withX(ballX)
				.withY(ballY).build();

		ball = PongBall.create().withId(1).withDiameter(diameter)
				.withCoordinates(coordinates).build();

		pongBallSvc.addPongBall(ball);

	}

	public void actionPerformed(ActionEvent e) {
		step();
	}

	public void step() {

		if (playing) {

			// where will the ball be after it moves?
			int nextBallLeft = ball.getCoordinates().getX() + ballDeltaX;
			int nextBallRight = ball.getCoordinates().getX()
					+ ball.getDiameter() + ballDeltaX;
			int nextBallTop = ball.getCoordinates().getY() + ballDeltaY;
			int nextBallBottom = ball.getCoordinates().getY()
					+ ball.getDiameter() + ballDeltaY;

			// ball bounces off top and bottom of screen
			if (nextBallTop < 0 || nextBallBottom > getHeight()) {
				ballDeltaY *= -1;
			}

			// has the ball entered the area where it can be hit
			// by the first (left sided) player?
			if (nextBallLeft < playerOneX) {
				// ball has entered the area where it can be hit
				leftPlayerHitBall();

				// missed the paddle?
				if (nextBallLeft <= 0) {
					playerTwoScore++;
					ball.getCoordinates().setX(250);
					ball.getCoordinates().setY(250);
				}
			} else {
				ballCanBeHit = true;
			}

			// has the ball entered the area where it can be hit
			// by the second (right sided) player?
			if (nextBallRight > playerTwoX) {
				// ball has entered the area where it can be hit
				rightPlayerHitBall();

				// missed the paddle?
				if (nextBallRight >= screen.getWidth()) {
					playerOneScore++;
					ball.getCoordinates().setX(250);
					ball.getCoordinates().setY(250);
				}
			} else {
				ballCanBeHit2 = true;
			}

			moveBall();

			sendBallCoordinatesToServer();
		}

		// stuff has moved, tell this JPanel to repaint itself
		repaint();
	}

	private void rightPlayerHitBall() {
		new Thread() {
			public void run() {
				if (ballCanBeHit2) {
					PhoneCoordinates phoneCoordinates = playerSvc
							.getPhoneCoordinates(1);
					if (phoneCoordinates.getY() > -2
							&& phoneCoordinates.getY() < 2) {
						if (phoneCoordinates.getZ() > 80) {
							ballCanBeHit2 = false;
							ballDeltaY *= -1;
							ballDeltaX *= -1;							
						}
					}
				}
			}
		}.start();
		
	}

	private void leftPlayerHitBall() {
		new Thread() {
			public void run() {
				if (ballCanBeHit) {
					PhoneCoordinates phoneCoordinates = playerSvc
							.getPhoneCoordinates(1);
					if (phoneCoordinates.getY() > -2
							&& phoneCoordinates.getY() < 2) {
						if (phoneCoordinates.getZ() > 80) {
							ballCanBeHit = false;
							ballDeltaY *= -1;
							ballDeltaX *= -1;							
						}
					}
				}
			}
		}.start();

	}

	private void sendBallCoordinatesToServer() {
		new Thread() {
			public void run() {
				pongBallSvc.setCoordinates(ball.getId(), ball.getCoordinates());
			}
		}.start();
	}

	private void moveBall() {
		ball.getCoordinates().setX(ball.getCoordinates().getX() + ballDeltaX);
		ball.getCoordinates().setY(ball.getCoordinates().getY() + ballDeltaY);
	}

	// paint the game screen
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.WHITE);

		screen.setWidth((int) g.getClipBounds().getWidth());
		screen.setHeight((int) g.getClipBounds().getHeight());

		playerTwoX = screen.getWidth() * 9 / 10;
		playerOneX = screen.getWidth() / 10;

		if (showTitleScreen) {

			g.setFont(new Font(Font.DIALOG, Font.BOLD, 36));
			g.setFont(new Font(Font.DIALOG, Font.BOLD, 36));
			g.drawString("Pong", screen.getWidth() / 2, screen.getHeight() / 2);

			g.setFont(new Font(Font.DIALOG, Font.BOLD, 18));

			g.drawString("Press 'P' to play.", screen.getWidth() / 2,
					(int) screen.getHeight() / 2 + 80);

		} else if (playing) {

			int playerOneRight = playerOneX + playerOneWidth;
			int playerTwoLeft = playerTwoX;

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
			g.drawString(String.valueOf(playerOneScore),
					(int) (screen.getWidth() / 3.5), 100);
			g.drawString(String.valueOf(playerTwoScore),
					(int) (screen.getWidth() / 1.5), 100);

			// draw the ball
			g.fillOval(ball.getCoordinates().getX(), ball.getCoordinates()
					.getY(), ball.getDiameter(), ball.getDiameter());

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

		} else if (gameOver) {
			if (e.getKeyCode() == KeyEvent.VK_SPACE) {
				gameOver = false;
				showTitleScreen = true;
				ball.getCoordinates().setX(250);
				ball.getCoordinates().setY(250);
				playerOneScore = 0;
				playerTwoScore = 0;
			}
		}
	}

	public void keyReleased(KeyEvent arg0) {
	}
}