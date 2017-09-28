package com.advanpro.fwtools.ble;

import java.util.UUID;

/**
 * Created by zengfs on 2015/12/14.
 */
public class UuidLib {
	public static final UUID PRIVATE_SERVICE = UUID.fromString("00001C00-D102-11E1-9B23-000EFB00DBDB");
	public static final UUID REAL_TIME_NOTIFY = UUID.fromString("00001C0F-D102-11E1-9B23-000EFB00DBDB");
	public static final UUID REAL_TIME_WRITE = UUID.fromString("00001C01-D102-11E1-9B23-000EFB00DBDB");
	public static final UUID HISTORY_NOTIFY = UUID.fromString("00001C0E-D102-11E1-9B23-000EFB00DBDB");
	public static final UUID HISTORY_WRITE = UUID.fromString("00001C02-D102-11E1-9B23-000EFB00DBDB");
	public static final UUID BATTERY_SERVICE = generateBluetoothBaseUuid(0x180f);
	public static final UUID BATTERY = generateBluetoothBaseUuid(0x2a19);
	public static final UUID DEVICE_INFO_SERVICE = generateBluetoothBaseUuid(0x180a);
	public static final UUID DEVICE_VV = generateBluetoothBaseUuid(0x2a25);
	public static final UUID DEVICE_ID = generateBluetoothBaseUuid(0x2a24);
	public static final UUID DEVICE_FIRMWARE_VERSION = generateBluetoothBaseUuid(0x2a26);
	public static final UUID CSR_OTA_BOOTLOADER_SERVICE = UUID.fromString("00001010-d102-11e1-9b23-00025b00a5a5");
	public static final UUID CSR_OTA_APPLICATION_SERVICE = UUID.fromString("00001016-d102-11e1-9b23-00025b00a5a5");
	public static final UUID CSR_OTA_CURRENT_APPLICATION = UUID.fromString("00001013-d102-11e1-9b23-00025b00a5a5");    
	public static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

	private static UUID generateBluetoothBaseUuid(long paramLong) {
		return new UUID(4096L + (paramLong << 32), -9223371485494954757L);
	}
}
