package eu.selfhost.suxdorf;

public interface MessageProcessor {
	void processMessageDoubleOut(final String topic, final double val);

	void processMessageStringIn(final String topic, final String message);

	void processMessageStringOut(final String topic, final String message);
}
