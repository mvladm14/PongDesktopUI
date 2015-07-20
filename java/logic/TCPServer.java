package logic;

import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import models.sensors.LinearAcceleration;

/**
 * The class extends the Thread class so we can receive and send messages at the
 * same time
 */
public class TCPServer extends Thread {

	private int serverPort;
	private boolean running = false;
	private PrintWriter mOut;
	private OnMessageReceived messageListener;

	/**
	 * Constructor of the class
	 * 
	 * @param messageListener
	 *            listens for the messages
	 */
	public TCPServer(int serverPort, OnMessageReceived messageListener) {
		this.serverPort = serverPort;
		this.messageListener = messageListener;
	}

	/**
	 * Method to send the messages from server to client
	 * 
	 * @param message
	 *            the message sent by the server
	 */
	public void sendMessage(String message) {
		System.out.println("Sending back " + message);
		if (mOut != null && !mOut.checkError()) {
			mOut.println(message);
			mOut.flush();
		}
	}

	@Override
	public void run() {
		super.run();

		running = true;

		try {
			System.out.println("[SERVER:" +serverPort + "] Connecting...");

			// create a server socket. A server socket waits for requests to
			// come in over the network.
			ServerSocket serverSocket = new ServerSocket(serverPort);

			// create client socket... the method accept() listens for a
			// connection to be made to this socket and accepts it.
			Socket client = serverSocket.accept();
			System.out.println("[SERVER:" +serverPort + "] Receiving...");

			try {

				// sends the message to the client
				mOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);

				// read the message received from client
				InputStream inputStream = client.getInputStream();
				ObjectInputStream in = new ObjectInputStream(inputStream);

				// in this while we wait to receive messages from client (it's
				// an infinite loop)
				// this while it's like a listener for messages
				while (running) {
					LinearAcceleration linear = (LinearAcceleration) in
							.readObject();

					if (linear != null && messageListener != null) {
						// call the method messageReceived from ServerBoard
						// class
						messageListener.messageReceived(linear);
					}
				}

			} catch (SocketException se) {
				System.exit(0);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException cn) {
				cn.printStackTrace();
			} finally {
				client.close();
				System.out.println("[SERVER:" +serverPort + "] Done.");
			}

		} catch (Exception e) {
			System.out.println("[SERVER:" +serverPort + "] Error");
			e.printStackTrace();
		}

	}

	// Declare the interface. The method messageReceived(String message) will
	// must be implemented in the ServerBoard
	// class at on startServer button click
	public interface OnMessageReceived {
		public void messageReceived(LinearAcceleration linearAcceleration);
	}

}