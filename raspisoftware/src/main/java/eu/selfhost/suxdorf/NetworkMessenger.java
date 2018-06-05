package eu.selfhost.suxdorf;

public interface NetworkMessenger {

	void addNewMessageListener(MessageProcessor pcl);

	boolean connectClient();

	void disconnect();

	String getUserName();

	boolean isConnected();

	void openChannel(String channel);

	void openChannel(String channel, int qos);

	void removeNewMessageListener(MessageProcessor pcl);

	void sendMessage(String channel, String message);
}