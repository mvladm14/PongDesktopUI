package logic;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import models.player.Pallet;
import models.sensors.LinearAcceleration;

public class PongController {

	private UIController uiHelper;

	private static final int SERVER_PORT_1 = 4444;
	private static final int SERVER_PORT_2 = 4445;

	private static final float NS2S = 1.0f / 1000000000.0f;

	private static final float PHONE_ERROR = 1.8f;
	private static final float BAND_ERROR = 0.0f;

	private static final boolean USES_BAND = false;

	private logic.TCPServer mServer1;
	private logic.TCPServer mServer2;

	private List<Float> distances;
	private List<Float> linearAccelerations;
	private List<Float> velocities;
	private List<Float> velocities2;

	private Float velocity = 0f;

	private Float distance = 0f;

	public PongController(UIController uiHelper) {

		this.uiHelper = uiHelper;

		linearAccelerations = new ArrayList<Float>();
		distances = new ArrayList<Float>();
		velocities = new ArrayList<Float>();
		velocities2 = new ArrayList<Float>();

		// velocities.add(velocity);
		// velocities2.add(velocity);
		// distances.add(distance);

		initializeTCPServer();
	}

	private void initializeTCPServer() {
		// creates the object OnMessageReceived asked by the TCPServer
		// constructor
		mServer1 = new TCPServer(SERVER_PORT_1,
				new TCPServer.OnMessageReceived() {
					// this method declared in the interface from TCPServer
					// class is implemented here
					// this method is actually a callback method, because it
					// will run every time when it will be called from
					// TCPServer class (at while)
					public void messageReceived(
							LinearAcceleration linearAcceleration) {

						updatePallet(uiHelper.getPallet(), linearAcceleration);

					}

				});
		mServer1.start();

		mServer2 = new TCPServer(SERVER_PORT_2,
				new TCPServer.OnMessageReceived() {
					// this method declared in the interface from TCPServer
					// class is implemented here
					// this method is actually a callback method, because it
					// will run every time when it will be called from
					// TCPServer class (at while)
					public void messageReceived(
							LinearAcceleration linearAcceleration) {

						updatePallet(uiHelper.getPallet2(), linearAcceleration);

					}

				});
		mServer2.start();
	}

	public void performStep(int screenHeight, int screenWidth) {
		if (uiHelper.isPlaying()) {
			uiHelper.performStep(screenHeight, screenWidth);
		}
	}

	private void updatePallet(Pallet pallet,
			LinearAcceleration linearAcceleration) {
		if (pallet.getLastTimeStamp() != 0) {

			float dt = (linearAcceleration.getTimestamp() - pallet
					.getLastTimeStamp()) * NS2S;

			if (dt > 0f) {

				float yAcceleration = linearAcceleration.getValues();

				if (Math.abs(yAcceleration) < (USES_BAND ? BAND_ERROR
						: PHONE_ERROR)) {
					pallet.setErrorCounter(pallet.getErrorCounter() + 1);
				} else {
					pallet.setErrorCounter(0);
				}
				if (pallet.getErrorCounter() < 10) {

					float initialVelocity = pallet.getVelocity();

					float finalVelocity = initialVelocity + yAcceleration * dt;
					pallet.setVelocity(finalVelocity);

					float finalDistance = initialVelocity * dt
							+ pallet.getVelocity() * dt / 2f * 3000f;
					finalDistance = (USES_BAND ? finalDistance * 10000000000000f
							: finalDistance);
					pallet.setDistance(finalDistance);

					//velocities2.add(yAcceleration * dt);
					// linearAccelerations.add(yAcceleration);
					// velocities.add(pallet.getVelocity());
					// distances.add(pallet.getDistance());

					updatePalletPosition(pallet, pallet.getDistance());
				} else {
					pallet.setVelocity(0f);
					pallet.setErrorCounter(0);
				}
			}

		}
		pallet.setLastTimeStamp(linearAcceleration.getTimestamp());
	}

	private void updatePalletPosition(Pallet p, float distance) {
		updateAccordingToLinearAcceleration(p, distance);
	}

	private void updateAccordingToLinearAcceleration(Pallet p, float distance) {
		p.updatePosition(distance);
		if (p.resolveCollisionWithBounds(uiHelper.getScreen())) {
			p.setVelocity(0f);
		}
		uiHelper.getPongPanel().repaint();

	}

	public void stopGame() {
		PrintWriter writer;
		try {
			writer = new PrintWriter("accVelDistanceData.txt", "UTF-8");

			int min1 = Math.min(distances.size(), linearAccelerations.size());
			int min2 = Math.min(velocities.size(), velocities2.size());
			int min = Math.min(min1, min2);

			System.out.println("TOTAL DISTANCES = " + distances.size());
			for (int counter = 0; counter < min; counter++) {
				writer.println(linearAccelerations.get(counter) + ","
						+ velocities2.get(counter) + ","
						+ velocities.get(counter) + ","
						+ distances.get(counter));
			}
			writer.close();

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public void stopServers() {
		mServer1.stop();
		mServer2.stop();
		System.out.println("Servers were stopped");
	}

}
