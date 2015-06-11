package models;

import models.ball.PongBall;

public class NextBallPositionFactory {

	public static NextBallPosition create(PongBall ball) {
		int nextBallLeft = ball.getCoordinates().getX()
				+ ball.getBallSpeed().getDeltaX();
		int nextBallRight = ball.getCoordinates().getX() + ball.getDiameter()
				+ ball.getBallSpeed().getDeltaX();
		int nextBallTop = ball.getCoordinates().getY()
				+ ball.getBallSpeed().getDeltaY();
		int nextBallBottom = ball.getCoordinates().getY() + ball.getDiameter()
				+ ball.getBallSpeed().getDeltaY();

		return new NextBallPosition(nextBallLeft, nextBallRight, nextBallTop,
				nextBallBottom);
	}
}
