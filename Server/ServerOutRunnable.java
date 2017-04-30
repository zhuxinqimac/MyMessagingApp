// Data to Client

import java.util.concurrent.BlockingQueue;
import java.net.Socket;
import java.io.DataOutputStream;
import java.io.IOException;

class ServerOutRunnable implements Runnable{
	private final BlockingQueue<MyProtocol> in; // In from main thread
	private final BlockingQueue<String> out; // Out to main thread
	Socket socket;
	Chat cloud; // used to connect all other server out thread
	String receiver;

	ServerOutRunnable(BlockingQueue<MyProtocol> input, BlockingQueue<String> output, Socket serverSocket, Chat cloud, String username) {
		this.in = input;
		this.out = output;
		this.socket = serverSocket;
		this.cloud = cloud;
		this.receiver = username;
	}

	public void run() {
		String sentence = "_sentence";
		MyProtocol data;
		DataOutputStream outToClient = null;
		try {
			outToClient = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			MyPrint.printDebug("ClientInRunnable In From Server Error.");
			return;
		}
		
		try {
            Thread.sleep(Constant.SERVER_OUT_DELAY * 500);
	    } catch (InterruptedException e) {
            MyPrint.printDebug("ServerOutRunnable thread delay error.");
            return;
	    }

	    // try {
     //        sentence = "setTimeout " + GlobalVar.getTimeout();
     //        outToClient.writeBytes(sentence + '\n');
	    // } catch (IOException e) {
     //        MyPrint.printDebug("ServerOutRunnable setTimeout error.");
	    // }
		while (true) {
			try {
				data = cloud.receive(receiver);
				sentence = data.toString();
				MyPrint.printDebug("ServerOutRunnable " + sentence);
				// outToClient = new DataOutputStream(socket.getOutputStream());
				outToClient.writeBytes(sentence + '\n');
				if ((data.name).equals("logout") && ((data.type).equals("Ack"))) {
					break;
				}
			} catch (IOException e) {
				MyPrint.printDebug("ServerOutRunnable Out To Client Error.");
				break;
			}

		}
	}
}