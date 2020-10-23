package com.marginallyclever.communications.tcp;

import com.marginallyclever.communications.NetworkSession;
import com.marginallyclever.communications.TransportLayer;
import com.marginallyclever.communications.TransportLayerPanel;
import com.marginallyclever.robotOverlord.log.Log;

/**
 * Lists available TCP connections and opens a connection of that type to a robot
 *
 * @author Dan
 * @since v7.1.0.0
 */
public class TCPTransportLayer implements TransportLayer {

	public TCPTransportLayer() {}

	/**
	 * @return <code>serialConnection</code> if connection successful.
	 *         <code>null</code> on failure.
	 */
	@Override
	public NetworkSession openConnection(String connectionName) {
		/*
		 * // check it Log.message("Validating "+connectionName); InetAddressValidator
		 * validator = new InetAddressValidator();
		 * if(!validator.isValid(connectionName)) {
		 * Log.error("Not a valid IP Address."); return null; }
		 */
		String[] parts = connectionName.split("@");

		Log.message("Connecting to " + parts[parts.length - 1]);
		// if(connectionName.equals(recentPort)) return null;
		TCPConnection connection = new TCPConnection(this);

		try {
			connection.openConnection(connectionName);
			Log.message("Connect OK");
		} catch (Exception e) {
			Log.message("Connect FAILED");
			e.printStackTrace();
			return null;
		}

		return connection;
	}

	@Override
	public TransportLayerPanel getTransportLayerPanel() {
		return new TCPTransportLayerPanel(this);
	}
}
