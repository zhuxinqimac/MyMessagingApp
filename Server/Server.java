// Main file of Server

import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;

public class Server {

	public static void main(String [] args) {
		int serverPort = Constant.SERVER_PORT;
		int blockDuration = Constant.BLOCK_DURATION;
		int timeout = Constant.TIMEOUT;
		Chat cloud;

		ServerSocket welcomeSocket = null;
		Socket connectionSocket = null;

		MyPrint.printMessage("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
		try	{
			if (args.length == 3) {
				serverPort = Integer.parseInt(args[0]);
				blockDuration = Integer.parseInt(args[1]);
				timeout = Integer.parseInt(args[2]);
			}
			else {
				System.out.println("Please input 3 integers as arguments.");
				return;
			}
		} catch (NumberFormatException x) {
			System.out.println("Please input 3 integers as arguments.");
			return;
		}

		// Initialization
		ServerRunnable.initiateUserState();
		GlobalVar.setServerPort(serverPort);
		GlobalVar.setBlockDuration(blockDuration);
		GlobalVar.setTimeout(timeout);
		LoginHistory.clearFile();

		for (int i = 0; i < ServerRunnable.listUserState.size(); i++) {
			MyPrint.printDebug(ServerRunnable.listUserState.get(i).getUsername());
		}
		
		try {
			// create server socket
			welcomeSocket = new ServerSocket(serverPort);
			welcomeSocket.setReuseAddress(true);
			MyPrint.printDebug("Welcome OK.");
		} catch (IOException e){
			MyPrint.printDebug("Server Socket Initialization Error.");
		}


		System.out.println("*****************************************************************");
		System.out.println("Welcome to my simple instant messaging application 1.0.");
		System.out.println("Author: Xinqi/Steven ZHU.");
		System.out.println("All rights reserved.");
		System.out.println("This is the server side program.");
		System.out.println("If you want to know more about this program, please head to ");
		System.out.println("http://zhuxinqi.space/project/messagingapplication.html ");
		System.out.println("for more information.");
		System.out.println("\n");
		System.out.println("Now you can start your client programs.");
		System.out.println("*****************************************************************");

		cloud = new Chat();

		while (true) {
			try	{
				connectionSocket = welcomeSocket.accept();
				MyPrint.printDebug("Connection from: " + connectionSocket);
			}
			catch (IOException e) {
				MyPrint.printDebug("Fail to establish connection.");
			}

			// New a thread for this client
			(new Thread(new ServerRunnable(connectionSocket, cloud))).start();
		}
	}
}
