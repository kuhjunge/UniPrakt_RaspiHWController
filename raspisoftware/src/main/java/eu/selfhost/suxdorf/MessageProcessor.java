package eu.selfhost.suxdorf;

public interface MessageProcessor {
	/**
	 * Sendet eine Nachricht eines Sensors
	 * @param topic MQTT Thema
	 * @param val Wert
	 * @param unit Einheit
	 */
	void processMessageDoubleOut(final String topic, final double val, final String unit);

	void processMessageStringIn(final String topic, final String message);

	void processMessageStringOut(final String topic, final String message);
}
