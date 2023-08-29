/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */
package no.auke.p2p.m2.workers.keepalive;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import no.auke.util.ByteUtil;

public class KeepAliveAddressEntry {
	private long version = 0;
	private boolean[] mask = new boolean[0];
	public long getVersion() {
		return version;
	}
	public void setVersion(long version) {
		this.version = version;
	}
	public void setMask(boolean[] mask) {
		this.mask = mask;
	}
	public boolean[] getMask() {
		return this.mask;
	}
	public long getPriority() {
		return mask.length;
	}
	private ArrayList<NetAddress> addresses = new ArrayList<NetAddress>();
	public ArrayList<NetAddress> getAddresses() {
		return addresses;
	}
	public void setAddresses(ArrayList<NetAddress> addresses) {
		this.addresses = addresses;
	}
	public String getId() {
		StringBuffer buf = new StringBuffer();
		buf.append(getPriority());
		for (boolean b : mask) {
			buf.append(b ? "1" : "0");
		}
		return buf.toString();
	}
	private void toObject(String line) {
		String[] l = line.split("#");
		if (l.length > 2) {
			version = Long.valueOf(l[1]);
			String[] mm = l[2].split(":");
			boolean[] mb = new boolean[mm.length];
			for (int i = 0; i < mm.length; i++) {
				mb[i] = mm[i].trim().equals("1") ? true : false;
			}
			if (l.length > 3) {
				toObject(mb, l[3].trim());
			}
		}
	}
	private void toObject(boolean[] Mask, String Addresslist) {
		
		String[] ss = Addresslist.split("\\s*[;,]\\s*");
		for (String s : ss) {
			String p[] = s.split(":");
			if (p.length > 0) {
				try {
					addresses.add(new NetAddress(p[0].trim(), (p.length > 1 ? Integer.parseInt(p[1]) : 8434)));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
		}
		mask = Mask;
	}
	
	public KeepAliveAddressEntry() {}
	public KeepAliveAddressEntry(boolean[] mask) {
		this.mask = mask;
	}
	public KeepAliveAddressEntry(boolean[] mask, String addressList) {
		toObject(mask, addressList);
	}
	public KeepAliveAddressEntry(String line) {
		toObject(line);
	}
	public boolean isValid(byte[] code) {
		int pos = 0;
		for (boolean positiv : mask) {
			if (code.length > pos) {
				if (positiv && (int) code[pos] < 0) {
					return false;
				} else if (!positiv && (int) code[pos] > 0) {
					return false;
				}
				pos++;
			} else {
				return false;
			}
		}
		return true;
	}
	public String toString() {
		// write mask
		StringBuffer maskstring = new StringBuffer();
		for (boolean b : mask) {
			maskstring.append((maskstring.length() == 0 ? "" : ":") + (b ? "1" : "0"));
		}
		// write addresses
		StringBuffer addrstring = new StringBuffer();
		for (NetAddress addr : addresses) {
			addrstring.append((addrstring.toString().length() == 0 ? "" : ",") + addr.getAddress() + ":" + String.valueOf(addr.getPort()));
		}
		return String.valueOf(getPriority()) + "#" + String.valueOf(version) + "#" + maskstring.toString()
				+ (addrstring.toString().equals("") ? "" : "#" + addrstring.toString());
	}
	// get byte values to compress address
	// accept only octet address
	public byte[] getBytes() {
		byte[] maskbyte = new byte[mask.length];
		for (int i = 0; i < mask.length; i++) {
			maskbyte[i] = mask[i] ? (byte) 1 : (byte) 0;
		}
		ArrayList<byte[]> list = new ArrayList<byte[]>();
		for (NetAddress addr : addresses) {
			list.add(ByteUtil.mergeBytes(new Address(addr.getAddress()).getBytes(), ByteUtil.getBytes(addr.getPort(), 4)));
		}
		return ByteUtil.mergeDynamicBytesWithLength(ByteUtil.getBytes(version), maskbyte, ByteUtil.getBytes(list.size()), ByteUtil.mergeBytes(list),
				new byte[] { '#', '#', '#' });
	}
	public KeepAliveAddressEntry(byte[] value) {
		if (value != null) {
			List<byte[]> subs = ByteUtil.splitDynamicBytes(value);
			version = ByteUtil.getInt(subs.get(0));
			byte[] maskbyte = subs.get(1);
			mask = new boolean[maskbyte.length];
			for (int i = 0; i < mask.length; i++) {
				mask[i] = maskbyte[i] == 1 ? true : false;
			}
			addresses = new ArrayList<NetAddress>();
			int numaddress = ByteUtil.getInt(subs.get(2));
			List<byte[]> address_arr = ByteUtil.splitBytesWithFixedLength(subs.get(3), 8);
			for (int i = 0; i < numaddress; i++) {
				List<byte[]> address = ByteUtil.splitBytesWithFixedLength(address_arr.get(i), 4);
				Address addr = new Address(address.get(0));
				int port = ByteUtil.getInt(address.get(1));
				addresses.add(new NetAddress(addr.toString(), port));
			}
		}
	}
	public class Address {
		byte firstOctet;
		byte secondOctet;
		byte thirdOctet;
		byte fourthOctet;
		public Address(byte firstOctet, byte secondOctet, byte thirdOctet, byte fourthOctet) {
			this.firstOctet = firstOctet;
			this.secondOctet = secondOctet;
			this.thirdOctet = thirdOctet;
			this.fourthOctet = fourthOctet;
		}
		public Address(String address) {
			StringTokenizer st = new StringTokenizer(address, ".");
			int i = 0;
			while (st.hasMoreTokens()) {
				byte temp = (byte) Integer.parseInt(st.nextToken());
				switch (i) {
				case 0:
					firstOctet = temp;
					++i;
					break;
				case 1:
					secondOctet = temp;
					++i;
					break;
				case 2:
					thirdOctet = temp;
					++i;
					break;
				case 3:
					fourthOctet = temp;
					++i;
					break;
				}
			}
		}
		public Address(byte[] address) {
			firstOctet = address[0];
			secondOctet = address[1];
			thirdOctet = address[2];
			fourthOctet = address[3];
		}
		private int getAbs(byte value) {
			byte[] val = new byte[2];
			val[1] = value;
			return ByteUtil.getInt(val);
		}
		public String toString() {
			return String.valueOf(getAbs(firstOctet)) + "." + String.valueOf(getAbs(secondOctet)) + "." + String.valueOf(getAbs(thirdOctet)) + "."
					+ String.valueOf(getAbs(fourthOctet));
		}
		public byte[] getBytes() {
			byte[] result = new byte[4];
			result[0] = firstOctet;
			result[1] = secondOctet;
			result[2] = thirdOctet;
			result[3] = fourthOctet;
			return result;
		}
		public InetAddress getInetAddress() throws UnknownHostException {
			return InetAddress.getByAddress(getBytes());
		}
		public int hashCode() {
			return (firstOctet << 24) + (secondOctet << 16) + (thirdOctet << 8) + fourthOctet;
		}
	}
	public int size() {
		return addresses.size();
	}
}