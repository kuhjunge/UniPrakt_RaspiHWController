package eu.selfhost.suxdorf;

public interface NetworkMessenger {

	void addPropertyChangeListener(MessageProcessor pcl);

	boolean connectClient();

	void disconnect();

	String getUserName();

	boolean isConnected();

	void openChannel(String channel);

	void openChannel(String channel, int qos);

	void removePropertyChangeListener(MessageProcessor pcl);

	void sendMessage(String channel, String message);
}