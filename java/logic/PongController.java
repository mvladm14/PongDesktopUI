package logic;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import models.NextBallPosition;
import models.NextBallPositionFactory;
import models.ball.PongBall;
import models.phone.PhoneSensors;
import models.player.Coordinates;
import models.player.Dimension;
import models.player.Pallet;
import models.player.Player;
import models.player.Screen;
import models.sensors.SensorManager;
import restInterfaces.PlayerSvcApi;
import restInterfaces.PongBallSvcApi;
import retrofit.RestAdapter;

public class PongController {

	private static final String SERVER = "http://131.254.101.102:8080/myriads";

	private PongBallSvcApi pongBallSvc = new RestAdapter.Builder()
			.setEndpoint(SERVER).build().create(PongBallSvcApi.class);
	private PlayerSvcApi playerSvc = new RestAdapter.Builder()
			.setEndpoint(SERVER).build().create(PlayerSvcApi.class);

	private Player player1;
	private Player player2;
	private PongBall ball;

	private NextBallPosition nextBallPosition;

	private Pallet pallet;
	private Pallet pallet2;

	private Screen screen;

	private boolean playing = false;
	private boolean gameOver = false;

	static final float NS2S = 1.0f / 100000000000.0f;
	long last_timestamp = 0;

	private ArrayList<Float> distances;

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

		last_timestamp = playerSvc.getTimeStamp(1);

		this.screen = screen;
		distances = new ArrayList<Float>();

		// movePallets();

	}

	public void performStep(int screenHeight, int screenWidth) {
		if (playing) {

			nextBallPosition = NextBallPositionFactory.create(ball);

			checkBounds(screenHeight);

			checkCollisionWithPallet(pallet, nextBallPosition);
			checkCollisionWithPallet(pallet2, nextBallPosition);

			updateScore(nextBallPosition, screenWidth);
			moveBall();
			movePallets();

			// has the ball entered the area where it can be hit
			// by the first (left sided) player?
			if (nextBallPosition.getLeft() < player1.getHittableRegion().getX()) {
				// ball has entered the area where it can be hit
				leftPlayerHitBall();
			} else {
				player1.setCanHitBall(true);
			}

			// has the ball entered the area where it can be hit
			// by the second (right sided) player?
			if (nextBallPosition.getRight() > player2.getHittableRegion()
					.getX()) {
				// ball has entered the area where it can be hit
				rightPlayerHitBall();
			} else {
				player2.setCanHitBall(true);
			}

			sendBallCoordinatesToServer();
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

	private void movePallets() {
		new Thread() {
			public void run() {
				//while (true) {
					updatePalletPosition(pallet);
					updatePalletPosition(pallet2);
				//}
			}
		}.start();

	}

	private void rightPlayerHitBall() {
		new Thread() {
			public void run() {
				if (player2.canHitBall()) {
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
								player2.setCanHitBall(false);
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
						System.out.println(orientation[1]);
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

		updateAccordingToOrientation(p);
		p.resolveCollisionWithBounds(this.screen);
		// updateAccordingToLinearAcceleration(p);
	}

	private void updateAccordingToOrientation(Pallet p) {
		PhoneSensors phoneSensors = playerSvc.getPhoneSensors(1);
		float R[] = new float[9];
		float I[] = new float[9];
		float[] mAcceleration = phoneSensors.getAccelerometer().getValues();
		float[] mGeomagnetic = phoneSensors.getMagneticField().getValues();

		boolean success = SensorManager.getRotationMatrix(R, I, mAcceleration,
				mGeomagnetic);
		if (success) {
			float orientation[] = new float[3];
			SensorManager.getOrientation(R, orientation);
			convertToDegrees(orientation);
			p.updatePosition(orientation);
		}
	}

	private void updateAccordingToLinearAcceleration(Pallet p) {
		PhoneSensors phoneSensors = playerSvc.getPhoneSensors(1);
		float[] linearAcceleration = phoneSensors.getLinearAcceleration()
				.getValues();
		long timestamp = phoneSensors.getSensorTimeStamp();

		float dt = (timestamp - last_timestamp) * NS2S;
		if (dt > 0f) {
			float distance2 = linearAcceleration[1] * dt * dt / 2.0f;
			System.out.println(distance2);
			p.updatePosition(distance2, timestamp);
			p.resolveCollisionWithBounds(this.screen);
			distances.add(Float.valueOf(distance2));
		}
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

	public void stopGame() {

		PrintWriter writer;
		try {
			writer = new PrintWriter("the-file-name.txt", "UTF-8");
			for (int counter = 0; counter < distances.size(); counter++) {
				writer.println(distances.get(counter));
			}
			writer.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}
}
