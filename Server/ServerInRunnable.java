// Data in from client

import java.util.concurrent.BlockingQueue;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

class ServerInRunnable implements Runnable {
	private final BlockingQueue<String> in; // In from main thread
	private final BlockingQueue<MyProtocol> out; // Out to main thread
	public Socket socket;

	ServerInRunnable(BlockingQueue<String> input, BlockingQueue<MyProtocol> output, Socket serverSocket) {
		this.in = input;
		this.out = output;
		this.socket = serverSocket;
	}

	public void run() {
		MyProtocol fromClientSentence = null;
		String [] word;
		String clientSentence;
		BufferedReader inFromClient = null;
		try {
			inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			MyPrint.printDebug("ClientInRunnable In From Server Error.");
			return;
		}

		while (true) {
			try {
				// inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			    clientSentence = inFromClient.readLine();
			    if (clientSentence == null) {
			    	continue;
			    }
			    MyPrint.printDebug("ServerInRunnable " + clientSentence);
			    fromClientSentence = new MyProtocol(clientSentence);
			    out.put(fromClientSentence);
			} catch (InterruptedException e) {
				MyPrint.printDebug("ServerInRunnable Interrupted.");
				break;
			} catch (IOException e){
				MyPrint.printDebug("Input from Client error.");
				break;
			}
		}
	}
}