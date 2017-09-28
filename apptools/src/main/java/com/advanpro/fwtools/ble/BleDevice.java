package com.advanpro.fwtools.ble;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Created by zengfs on 2016/1/28.
 * 蓝牙设备
 */
public class BleDevice implements Comparable<BleDevice>, Cloneable, Parcelable {
	public String name;
	public String devId;
	public String mac;
    public String firmware;
    public String vv;
    public int battery;
	public boolean isLeft;
	public int rssi;
	public int type;

	public BleDevice() {
	}

	public BleDevice(String name, String mac, boolean isLeft, int type) {
		this.name = name;
		this.mac = mac;
		this.isLeft = isLeft;
		this.type = type;
	}

    @Override
	public boolean equals(Object obj) {
		return obj != null && obj instanceof BleDevice && mac.equals(((BleDevice)obj).mac);
	}

	@Override
	public int hashCode() {
		return mac.hashCode();
	}

	@Override
	public int compareTo(@NonNull BleDevice another) {
		int result;
		if (rssi == 0) {
			return -1;
		} else if (another.rssi == 0) {
			return 1;
		} else {
			result = Integer.valueOf(another.rssi).compareTo(rssi);
			if (result == 0) {
				result = name.compareTo(another.name);
			}
		}
		return result;
	}

	@Override
	public BleDevice clone() {
		BleDevice bleDevice = null;
		try {
			bleDevice = (BleDevice) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return bleDevice;
	}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.devId);
        dest.writeString(this.mac);
        dest.writeString(this.firmware);
        dest.writeString(this.vv);
        dest.writeInt(this.battery);
        dest.writeByte(this.isLeft ? (byte) 1 : (byte) 0);
        dest.writeInt(this.rssi);
        dest.writeInt(this.type);
    }

    protected BleDevice(Parcel in) {
        this.name = in.readString();
        this.devId = in.readString();
        this.mac = in.readString();
        this.firmware = in.readString();
        this.vv = in.readString();
        this.battery = in.readInt();
        this.isLeft = in.readByte() != 0;
        this.rssi = in.readInt();
        this.type = in.readInt();
    }

    public static final Creator<BleDevice> CREATOR = new Creator<BleDevice>() {
        @Override
        public BleDevice createFromParcel(Parcel source) {
            return new BleDevice(source);
        }

        @Override
        public BleDevice[] newArray(int size) {
            return new BleDevice[size];
        }
    };
}
