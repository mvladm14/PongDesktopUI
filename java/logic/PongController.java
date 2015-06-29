package logic;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import models.NextBallPosition;
import models.NextBallPositionFactory;
import models.ball.BallCoordinates;
import models.ball.BallSpeed;
import models.ball.PongBall;
import models.player.Coordinates;
import models.player.Dimension;
import models.player.HittableRegion;
import models.player.Pallet;
import models.player.Player;
import models.player.Screen;
import models.sensors.LinearAcceleration;
import ui.PongPanel;

public class PongController {

	private Player player1;
	private Player player2;

	private PongBall ball;

	private NextBallPosition nextBallPosition;

	private Pallet pallet;
	private Pallet pallet2;

	static final float NS2S = 1.0f / 10000000.0f;
	long last_timestamp = 0;

	private long initialTime, finalTime;

	private long counter;

	private logic.TCPServer mServer;
	private List<Float> distances;
	private List<Float> linearAccelerations;
	private boolean playing = false;
	private boolean gameOver = false;
	private PongPanel pongPanel;
	private Screen screen;

	public PongController(Screen screen, PongPanel pongPanel) {

		this.pongPanel = pongPanel;
		this.screen = screen;

		linearAccelerations = new ArrayList<Float>();
		distances = new ArrayList<Float>();

		initializeTCPServer();

		BallCoordinates ballCoordinates = new BallCoordinates(250, 250);
		BallSpeed ballSpeed = new BallSpeed(-1, 3);
		ball = new PongBall(1, ballCoordinates, 20, ballSpeed);

		player1 = new Player(1, "vlad", 0, true, new HittableRegion(5));
		player2 = new Player(2, "roxy", 0, true, new HittableRegion(5));

		Dimension dimension = new Dimension(20, 100);
		Coordinates coordinates = new Coordinates(200, 200);

		Dimension dimension2 = new Dimension(20, 100);
		Coordinates coordinates2 = new Coordinates(200, 200);

		pallet = new Pallet(coordinates, dimension);
		pallet2 = new Pallet(coordinates2, dimension2);

		initialTime = System.nanoTime();

	}

	private void initializeTCPServer() {
		// creates the object OnMessageReceived asked by the TCPServer
		// constructor
		mServer = new TCPServer(new TCPServer.OnMessageReceived() {
			// this method declared in the interface from TCPServer
			// class is implemented here
			// this method is actually a callback method, because it
			// will run every time when it will be called from
			// TCPServer class (at while)
			public void messageReceived(LinearAcceleration linearAcceleration) {

				float yAcceleration = linearAcceleration.getValues();
				//mServer.sendMessage("" + yAcceleration);
				//System.out.println("Received: " + yAcceleration);
				linearAccelerations.add(Float.valueOf(yAcceleration));

				float dt = (linearAcceleration.getTimestamp() - last_timestamp)
						* NS2S;
				if (dt > 0.0f && last_timestamp != 0) {
					float distance = yAcceleration * dt * dt * 2.0f;
					distances.add(Float.valueOf(distance));
					//System.out.println(distance + "");
					updatePalletPosition(pallet, distance);
				}
				last_timestamp = linearAcceleration.getTimestamp();
				
				
			}
		});
		mServer.start();
	}

	public void performStep(int screenHeight, int screenWidth) {
		if (playing) {

			nextBallPosition = NextBallPositionFactory.create(ball);

			checkBounds(screenHeight);

			checkCollisionWithPallet(pallet, nextBallPosition);
			checkCollisionWithPallet(pallet2, nextBallPosition);

			updateScore(nextBallPosition, screenWidth);
			moveBall();
		}
	}

	private void updateScore(NextBallPosition next, int screenWidth) {

		// missed the paddle?
		if (next.getLeft() <= 0) {
			player2.setScore(player2.getScore() + 1);
			ball.getCoordinates().setX(250);
			ball.getCoordinates().setY(250);
		}

		// missed the paddle?
		if (next.getRight() >= screenWidth) {
			player1.setScore(player1.getScore() + 1);
			ball.getCoordinates().setX(250);
			ball.getCoordinates().setY(250);
		}

	}

	private void checkCollisionWithPallet(Pallet pallet,
			NextBallPosition nextBallPosition) {

		// where will the ball be after it moves?
		if (intersects(pallet, nextBallPosition)) {
			ball.getBallSpeed().setDeltaY(ball.getBallSpeed().getDeltaY() * -1);
			ball.getBallSpeed().setDeltaX(ball.getBallSpeed().getDeltaX() * -1);
		}

	}

	private boolean intersects(Pallet p, NextBallPosition next) {

		boolean intersectsOnXAxis = next.getLeft() >= p.getCoordinates().getX()
				&& next.getLeft() <= p.getCoordinates().getX()
						+ p.getDimension().getWidth();

		int palletTop = (int) p.getCoordinates().getY();
		int palletButtom = palletTop + p.getDimension().getHeight();

		boolean intersectsOnYAxis = next.getBottom() < palletButtom
				&& next.getTop() > palletTop;

		return intersectsOnXAxis && intersectsOnYAxis;
	}

	private void checkBounds(int screenHeight) {

		// ball bounces off top and bottom of screen
		if (nextBallPosition.getTop() < 0
				|| nextBallPosition.getBottom() > screenHeight) {
			ball.getBallSpeed().setDeltaY(ball.getBallSpeed().getDeltaY() * -1);
		}
	}

	private void updatePalletPosition(Pallet p, float distance) {

		// updateAccordingToOrientation(p);
		// p.resolveCollisionWithBounds(this.screen);
		updateAccordingToLinearAcceleration(p, distance);
	}

	private void updateAccordingToLinearAcceleration(Pallet p, float distance) {
		p.updatePosition(distance);
		p.resolveCollisionWithBounds(this.screen);
		pongPanel.repaint();

	}

	private void moveBall() {
		ball.getCoordinates().setX(
				ball.getCoordinates().getX() + ball.getBallSpeed().getDeltaX());
		ball.getCoordinates().setY(
				ball.getCoordinates().getY() + ball.getBallSpeed().getDeltaY());
	}

	public Pallet getPallet() {
		return pallet;
	}

	public PongBall getBall() {
		return ball;
	}

	public Player getPlayer1() {
		return player1;
	}

	public Player getPlayer2() {
		return player2;
	}

	public boolean isPlaying() {
		return playing;
	}

	public void setPlaying(boolean playing) {
		this.playing = playing;
	}

	public boolean isGameOver() {
		return gameOver;
	}

	public void setGameOver(boolean gameOver) {
		this.gameOver = gameOver;
	}

	public Pallet getPallet2() {
		return pallet2;
	}

	public void setPallet2(Pallet pallet2) {
		this.pallet2 = pallet2;
	}

	public void stopGame() {

		finalTime = System.nanoTime();
		PrintWriter writer;
		try {
			writer = new PrintWriter("distances.txt", "UTF-8");
			System.out.println("TOTAL DISTANCES = " + distances.size());
			for (int counter = 0; counter < distances.size(); counter++) {
				writer.println(distances.get(counter));
			}
			writer.close();

			try {
				writer = new PrintWriter("linearAcc.txt", "UTF-8");
				System.out.println("WROTE " + linearAccelerations.size());
				for (int counter = 0; counter < linearAccelerations.size(); counter++) {
					writer.println(linearAccelerations.get(counter));
				}
				writer.close();
			} catch (Exception e1) {
				e1.printStackTrace();
			}

		} catch (Exception e1) {
			e1.printStackTrace();
		}

		System.out.println("TIME ELAPSED = " + (finalTime - initialTime)
				+ " and made " + counter + " measurements.");
	}
}
