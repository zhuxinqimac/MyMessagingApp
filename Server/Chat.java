// Chat cloud among ServerRunnable and ServerOutRunnable

class Chat {
	public synchronized void send(MyProtocol protocolMessage) {
		(ServerRunnable.getUserState(protocolMessage.whom)).addMessage(protocolMessage);
		notifyAll();
	}

	public synchronized MyProtocol receive(String receiver) {
		UserState receiverState = ServerRunnable.getUserState(receiver);
		// MyPrint.printDebug("Chat receiver: " + receiver + " " + receiverState.isReceiving.toString());
		while (!receiverState.isReceiving) {
			try {
				MyPrint.printDebug(receiver + " is waiting.");
				wait();
			} catch (InterruptedException e) {
				MyPrint.printDebug("Chat interrupted.");
			}
		}

		// MyPrint.printDebug("Chat receiver: " + receiver + " " + receiverState.isReceiving.toString());
		MyPrint.printDebug(receiver + " is not waiting.");
		MyProtocol receivedMessage = new MyProtocol(receiverState.getMessage());
		receiverState.removeMessage();
		notifyAll();
		return receivedMessage;
	}
}