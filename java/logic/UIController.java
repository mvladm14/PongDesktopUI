package logic;

import ui.PongPanel;
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

public class UIController {

	private Pallet pallet;
	private Pallet pallet2;

	private Player player1;
	private Player player2;
	private PongBall ball;
	private NextBallPosition nextBallPosition;

	private boolean playing = false;
	private boolean gameOver = false;

	private PongPanel pongPanel;
	private Screen screen;

	public UIController(Screen screen, PongPanel pongPanel) {

		this.pongPanel = pongPanel;
		this.screen = screen;

		BallCoordinates ballCoordinates = new BallCoordinates(250, 250);
		BallSpeed ballSpeed = new BallSpeed(-1, 1);
		ball = new PongBall(1, ballCoordinates, 20, ballSpeed);
		
		HittableRegion hittableRegionP1 = HittableRegion.create()
				.withX(5).build();
		HittableRegion hittableRegionP2 = HittableRegion.create()
				.withX(5).build();
		
		player1 = Player.create()
				.withId(1)
				.withUsername("vlad")
				.withScore(0)
				.withCanHitBall(true)
				.withHittableRegion(hittableRegionP1)
				.build();
		
		player2 = Player.create()
				.withId(2)
				.withUsername("roxy")
				.withScore(0)
				.withCanHitBall(true)
				.withHittableRegion(hittableRegionP2)
				.build();

		Dimension dimension = new Dimension(20, 100);
		Coordinates coordinates = new Coordinates(200, 200);

		Dimension dimension2 = new Dimension(20, 100);
		Coordinates coordinates2 = new Coordinates(200, 200);

		pallet = new Pallet(coordinates, dimension);
		pallet2 = new Pallet(coordinates2, dimension2);
	}

	public void performStep(int screenHeight, int screenWidth) {

		nextBallPosition = NextBallPositionFactory.create(ball);

		checkBounds(screenHeight);

		checkCollisionWithPallets(nextBallPosition);
		updateScore(nextBallPosition, screenWidth);
		moveBall();
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

	private void moveBall() {
		ball.getCoordinates().setX(
				ball.getCoordinates().getX() + ball.getBallSpeed().getDeltaX());
		ball.getCoordinates().setY(
				ball.getCoordinates().getY() + ball.getBallSpeed().getDeltaY());
	}

	private void checkBounds(int screenHeight) {

		// ball bounces off top and bottom of screen
		if (nextBallPosition.getTop() < 0
				|| nextBallPosition.getBottom() > screenHeight) {
			ball.getBallSpeed().setDeltaY(ball.getBallSpeed().getDeltaY() * -1);
		}
	}

	private void checkCollisionWithPallets(NextBallPosition nextBallPosition) {
		checkCollisionWithPallet(pallet, nextBallPosition);
		checkCollisionWithPallet(pallet2, nextBallPosition);
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

	public Pallet getPallet() {
		return pallet;
	}

	public Pallet getPallet2() {
		return pallet2;
	}

	public Player getPlayer1() {
		return player1;
	}

	public Player getPlayer2() {
		return player2;
	}

	public PongBall getBall() {
		return ball;
	}

	public NextBallPosition getNextBallPosition() {
		return nextBallPosition;
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

	public PongPanel getPongPanel() {
		return pongPanel;
	}

	public void setPongPanel(PongPanel pongPanel) {
		this.pongPanel = pongPanel;
	}

	public Screen getScreen() {
		return screen;
	}

	public void setScreen(Screen screen) {
		this.screen = screen;
	}
	
	

}
