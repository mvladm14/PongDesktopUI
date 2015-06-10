package logic;

import java.util.Collection;
import java.util.Iterator;

import restInterfaces.PlayerSvcApi;
import restInterfaces.PongBallSvcApi;
import retrofit.RestAdapter;
import models.Screen;
import models.ball.PongBall;
import models.phone.PhoneSensors;
import models.player.Coordinates;
import models.player.Dimension;
import models.player.Pallet;
import models.player.Player;
import models.sensors.SensorManager;

public class PongController {

	private static final String SERVER = "http://131.254.101.102:8080/myriads";

	private PongBallSvcApi pongBallSvc = new RestAdapter.Builder()
			.setEndpoint(SERVER).build().create(PongBallSvcApi.class);
	private PlayerSvcApi playerSvc = new RestAdapter.Builder()
			.setEndpoint(SERVER).build().create(PlayerSvcApi.class);

	private Player player1;
	private Player player2;
	private PongBall ball;

	private Pallet pallet;
	private Pallet pallet2;

	private boolean playing = false;
	private boolean gameOver = false;

	public PongController(Screen screen) {

		Collection<PongBall> balls = pongBallSvc.getPongBalls();
		ball = balls.iterator().next();

		Collection<Player> players = playerSvc.getPlayersList();
		Iterator<Player> playerIterator = players.iterator();
		player1 = playerIterator.next();
		player2 = playerIterator.next();

		pongBallSvc.addPongBall(ball);

		Dimension dimension = new Dimension(20, 100);
		Coordinates coordinates = new Coordinates(200, 200);
		
		Dimension dimension2 = new Dimension(20, 100);
		Coordinates coordinates2 = new Coordinates(200, 200);

		pallet = new Pallet(coordinates, dimension);
		pallet2 = new Pallet(coordinates2, dimension2);

	}

	public void performStep(int screenHeight, int screenWidth) {
		if (playing) {

			int nextBallLeft = ball.getCoordinates().getX()
					+ ball.getBallSpeed().getDeltaX();
			int nextBallRight = ball.getCoordinates().getX()
					+ ball.getDiameter() + ball.getBallSpeed().getDeltaX();

			movePallets();

			checkBounds(screenHeight);

			checkCollisionWithPallet(pallet, nextBallLeft);

			updateScore(nextBallLeft, nextBallRight, screenWidth);

			/*
			 * // has the ball entered the area where it can be hit // by the
			 * first (left sided) player? if (nextBallLeft <
			 * player1.getHittableRegion().getX()) { // ball has entered the
			 * area where it can be hit leftPlayerHitBall();
			 * 
			 * // missed the paddle? if (nextBallLeft <= 0) {
			 * player2.setScore(player2.getScore() + 1);
			 * ball.getCoordinates().setX(250); ball.getCoordinates().setY(250);
			 * } } else { player1.setCanHitBall(true); }
			 * 
			 * // has the ball entered the area where it can be hit // by the
			 * second (right sided) player? if (nextBallRight >
			 * player2.getHittableRegion().getX()) { // ball has entered the
			 * area where it can be hit rightPlayerHitBall();
			 * 
			 * // missed the paddle? if (nextBallRight >= screenWidth) {
			 * player1.setScore(player1.getScore() + 1);
			 * ball.getCoordinates().setX(250); ball.getCoordinates().setY(250);
			 * } } else { player2.setCanHitBall(true); }
			 */

			moveBall();

			sendBallCoordinatesToServer();
		}
	}

	private void updateScore(int nextBallLeft, int nextBallRight,
			int screenWidth) {

		// missed the paddle?
		if (nextBallLeft <= 0) {
			player2.setScore(player2.getScore() + 1);
			ball.getCoordinates().setX(250);
			ball.getCoordinates().setY(250);
		}

		// missed the paddle?
		if (nextBallRight >= screenWidth) {
			player1.setScore(player1.getScore() + 1);
			ball.getCoordinates().setX(250);
			ball.getCoordinates().setY(250);
		}

	}

	private void checkCollisionWithPallet(Pallet pallet, int nextBallLeft) {

		// where will the ball be after it moves?

		if ((nextBallLeft == pallet.getCoordinates().getX()
				+ pallet.getDimension().getWidth()
				&& ball.getCoordinates().getY() > pallet.getCoordinates()
						.getY() && ball.getCoordinates().getY() < pallet
				.getCoordinates().getY() + pallet.getDimension().getHeight())
				|| (nextBallLeft == pallet.getCoordinates().getX())) {
			ball.getBallSpeed().setDeltaY(ball.getBallSpeed().getDeltaY() * -1);
			ball.getBallSpeed().setDeltaX(ball.getBallSpeed().getDeltaX() * -1);

		}

	}

	private void checkBounds(int screenHeight) {
		int nextBallTop = ball.getCoordinates().getY()
				+ ball.getBallSpeed().getDeltaY();
		int nextBallBottom = ball.getCoordinates().getY() + ball.getDiameter()
				+ ball.getBallSpeed().getDeltaY();

		// ball bounces off top and bottom of screen
		if (nextBallTop < 0 || nextBallBottom > screenHeight) {
			ball.getBallSpeed().setDeltaY(ball.getBallSpeed().getDeltaY() * -1);
		}
	}

	private void movePallets() {
		new Thread() {
			public void run() {
				updatePalletPosition(pallet);
				updatePalletPosition(pallet2);
			}
		}.start();

	}

	private void leftPlayerHitBall() {
		new Thread() {
			public void run() {
				if (player1.canHitBall()) {
					PhoneSensors phoneSensors = playerSvc.getPhoneSensors(1);
					float R[] = new float[9];
					float I[] = new float[9];
					float[] mGravity = phoneSensors.getAccelerometer()
							.getValues();
					float[] mGeomagnetic = phoneSensors.getMagneticField()
							.getValues();

					boolean success = SensorManager.getRotationMatrix(R, I,
							mGravity, mGeomagnetic);
					if (success) {
						float orientation[] = new float[3];
						SensorManager.getOrientation(R, orientation);
						convertToDegrees(orientation);
						if (orientation[1] > -2 && orientation[1] < 2) {
							if (orientation[2] > 80) {
								player1.setCanHitBall(false);
								ball.getBallSpeed().setDeltaY(
										ball.getBallSpeed().getDeltaY() * -1);
								ball.getBallSpeed().setDeltaX(
										ball.getBallSpeed().getDeltaX() * -1);
							}
						}
					}
				}
			}
		}.start();
	}

	private void updatePalletPosition(Pallet p) {

		PhoneSensors phoneSensors = playerSvc.getPhoneSensors(1);
		float[] mGravity = phoneSensors.getAccelerometer().getValues();
		long timestamp = phoneSensors.getSensorTimeStamp();

		p.updatePosition(mGravity[1], timestamp);
		// pallet.resolveCollisionWithBounds(mHorizontalBound, mVerticalBound);
	}

	private void convertToDegrees(float orientation[]) {
		// Convert to degrees
		for (int i = 0; i < orientation.length; i++) {
			Double degrees = (orientation[i] * 180) / Math.PI;
			orientation[i] = degrees.floatValue();
		}
	}

	private void moveBall() {
		ball.getCoordinates().setX(
				ball.getCoordinates().getX() + ball.getBallSpeed().getDeltaX());
		ball.getCoordinates().setY(
				ball.getCoordinates().getY() + ball.getBallSpeed().getDeltaY());
	}

	private void sendBallCoordinatesToServer() {
		new Thread() {
			public void run() {
				pongBallSvc.setCoordinates(ball.getId(), ball.getCoordinates());
			}
		}.start();
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

}
