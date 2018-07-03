package eu.selfhost.suxdorf;

import org.json.JSONObject;

public interface ValueListener<E> {
	public E processValue(JSONObject val, MessageProcessor mp);
}
