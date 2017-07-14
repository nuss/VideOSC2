package net.videosc2.db;

import android.provider.BaseColumns;

/**
 * Created by stefan on 22.06.17, package net.videosc2, project VideOSC22.
 */
public class SettingsContract {
	final private static String TAG = "SettingsContract";

	// prevent accidential instantiation
	private SettingsContract() {}

	/* Inner class that defines the table contents */
	public static class AddressSettingsEntry implements BaseColumns {
		public static final String TABLE_NAME = "vosc_client_addresses";
		public static final String IP_ADDRESS = "ip_address";
		public static final String PORT = "port";
		public static final String PROTOCOL = "protocol"; // TCP/IP or UDP
	}

	public static class SettingsEntries implements BaseColumns {
		public static final String TABLE_NAME = "vosc_settings";
		public static final String RES_H = "resolution_horizontal";
		public static final String RES_V = "resolution_vertical";
//		public static final String FRAMERATE_FIXED = "framerate_fixed";
		public static final String FRAMERATE_RANGE = "framerate_range";
		public static final String NORMALIZE = "normalized";
		public static final String REMEMBER_PIXEL_STATES = "remember_pixel_states";
		public static final String CALC_PERIOD = "calculation_period";
		public static final String ROOT_CMD = "root_cmd_name";
		public static final String UDP_RECEIVE_PORT = "udp_receive_port";
		public static final String TCP_RECEIVE_PORT = "tcp_receive_port";
	}

	public static class SensorSettingsEntries implements BaseColumns {
		public static final String TABLE_NAME = "vosc_sensor_settings";
		public static final String SENSOR = "sensor_name";
		public static final String VALUE = "sensor_activated";
	}

	public static class PixelSnapshotEntries implements BaseColumns {
		public static final String TABLE_NAME = "vosc_snapshots";
		public static final String SNAPSHOT_NAME = "snapshot_name";
		public static final String SNAPSHOT_VALUES = "snapshot_values";
	}

	// TODO
/*
	public static class MultiTouchSettingEntries implements BaseColumns {
		public static final String MULTITOUCH_SETTINGS = "vosc_multitouch_settings";
		// tbd
	}
*/
}
