/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */
package no.auke.p2p.m2.workers.keepalive;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import no.auke.encryption.EncryptException;
import no.auke.encryption.Encryption;
import no.auke.encryption.EncryptionType;
import no.auke.encryption.KeyReader;
import no.auke.encryption.PrivateKey;
import no.auke.encryption.PublicKey;
import no.auke.util.Base64;
import no.auke.util.ByteUtil;
import no.auke.util.FileUtil;

public class KeepAliveMap {
	private String MAPFILE = "kamap.txt";
	private String MAPKEY = "map-may2012";
	private String configdir = "";
	private String mapfilename;
	private String encryptedMap = "";
	private PublicKey publickey = null;
	public PublicKey getPublickey() {
		return publickey;
	}
	public KeepAliveMap() {}
	public KeepAliveMap(String configdir) {
		FileUtil.createDirectory(configdir);
		mapfilename = configdir + "/" + MAPFILE;
		this.configdir = configdir;
		try {
			publickey = new PublicKey(FileUtil.readBytesFromJAR(this, MAPKEY + ".key"));
			if (publickey.getGuid() == null) {
				publickey = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (EncryptException e) {
			e.printStackTrace();
		}
	}
	//
	// initial boot addresses
	// always present if used
	//
	KeepAliveAddressEntry bootEntry = new KeepAliveAddressEntry();
	public KeepAliveAddressEntry getBootEntry() {
		return bootEntry;
	}
	public void addBootAddressList(String bootAddress) {
		bootEntry = new KeepAliveAddressEntry(new boolean[] { true }, bootAddress);
	}
	public List<NetAddress> getBootAddresses() {
		List<NetAddress> addresses = new ArrayList<NetAddress>(bootEntry.getAddresses());
		return addresses;
	}
	// Adding a map from from a boot address
	public KeepAliveMap(String configdir, String addresses) {
		this(configdir);
		
		if (addresses != null && !addresses.equals("")) {
			mapversion = 1000;
		
			getKeepAliveList().add(new KeepAliveAddressEntry(new boolean[] { true }, addresses));
			getKeepAliveList().add(new KeepAliveAddressEntry(new boolean[] { false }, addresses));
			getKeepAliveList().add(new KeepAliveAddressEntry(new boolean[] { true, true }, addresses));
			getKeepAliveList().add(new KeepAliveAddressEntry(new boolean[] { true, false }, addresses));
			getKeepAliveList().add(new KeepAliveAddressEntry(new boolean[] { false, true }, addresses));
			getKeepAliveList().add(new KeepAliveAddressEntry(new boolean[] { false, false }, addresses));
		}
	
	}
	
	public KeepAliveMap(String configdir, String address, long mapversion) {
		this(configdir, address);
		this.mapversion = mapversion;
	}
	public String getMapfilename() {
		return mapfilename;
	}
	private ArrayList<KeepAliveAddressEntry> keepalivelist = new ArrayList<KeepAliveAddressEntry>();
	public void setKeepAliveList(ArrayList<KeepAliveAddressEntry> keepalivelist) {
		this.keepalivelist = keepalivelist;
	}
	public ArrayList<KeepAliveAddressEntry> getKeepAliveList() {
		return keepalivelist;
	}
	private long mapversion;
	public void setVersion(long mapversion) {
		this.mapversion = mapversion;
	}
	public long getVersion() {
		return mapversion;
	}
	private String readFromJARFile() throws IOException {
		InputStream is = getClass().getResourceAsStream("/" + MAPFILE);
		return FileUtil.readFromFile(is);
	}
	private String readFromFile() throws IOException {
		if (!mapfilename.equals("") && FileUtil.isFileExists(mapfilename)) {
			return FileUtil.readFromFile(new FileInputStream(mapfilename));
		} else {
			return "";
		}
	}
	public void save() throws IOException {
		// writing map to file
		if (!mapfilename.equals("")) {
			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(getMapfilename()), "UTF-8");
			out.write(toString());
			out.close();
		}
	}
	public void read() throws IOException {
		// LHA: TODO: not very good programmed, make better later
		// reading map from file
		if (mapversion == 0) { // empty map
			String resource = readFromJARFile();
			String fromfile = readFromFile();
			// compare version
			if (!resource.equals("") && fromfile != "") {
				String[] res = resource.split("\n");
				String[] fil = fromfile.split("\n");
				if (Long.valueOf(res[0].split(",")[0]) > Long.valueOf(fil[0].split(",")[0])) { // compare
																								// versions
					fromString(resource);
				} else {
					fromString(fromfile);
				}
			} else if (!resource.equals("")) {
				fromString(resource);
			} else if (!fromfile.equals("")) {
				fromString(fromfile);
			}
		} else {
			fromString(readFromFile());
		}
	}
	private ArrayList<boolean[]> AddMapLevel(ArrayList<boolean[]> list) {
		@SuppressWarnings("unchecked")
		ArrayList<boolean[]> new_list = (ArrayList<boolean[]>) list.clone();
		for (boolean[] map : list) {
			boolean[] new_map_1 = new boolean[map.length + 1];
			System.arraycopy(map, 0, new_map_1, 0, map.length);
			boolean[] new_map_0 = new boolean[map.length + 1];
			System.arraycopy(map, 0, new_map_0, 0, map.length);
			new_map_1[map.length] = true;
			new_map_0[map.length] = false;
			new_list.add(new_map_1);
			new_list.add(new_map_0);
		}
		return new_list;
	}
	public void genMap(int levels) {
		ArrayList<boolean[]> list = new ArrayList<boolean[]>();
		list.add(new boolean[] { true });
		list.add(new boolean[] { false });
		for (int i = 1; i < levels; i++) {
			list = AddMapLevel(list);
		}
		keepalivelist.clear();
		for (boolean[] b : list) {
			keepalivelist.add(new KeepAliveAddressEntry(b));
		}
	}
	public String toString() {
		if (encryptedMap.length() > 0) {
			// LHA: get encrypted version is exists
			return encryptedMap;
		} else {
			// LHA: get content non encrypted
			StringBuilder text = new StringBuilder();
			text.append(String.valueOf(mapversion)).append("\n");
			for (KeepAliveAddressEntry cluster : keepalivelist) {
				text.append(cluster.toString()).append("\n");
			}
			return text.toString();
		}
	}
	public void enCrypt() throws EncryptException {
		PrivateKey privatkey;
		try {
			privatkey = new PrivateKey(FileUtil.getByteArrayFromFile(configdir + "/" + MAPKEY + ".prv"));
		} catch (Exception e1) {
			throw new EncryptException("encrypt error: " + e1.getMessage());
		}
		if (publickey.getGuid().toString().equals(privatkey.getGuid().toString())) {
			StringBuilder text = new StringBuilder();
			text.append(String.valueOf(mapversion) + "," + privatkey.getGuid().toString()).append("\n");
			try {
				List<byte[]> list = new ArrayList<byte[]>();
				for (KeepAliveAddressEntry entry : keepalivelist) {
					list.add(entry.getBytes());
				}
				text.append(Base64.encodeToString(
						Encryption.getEncryption(EncryptionType.standard).getEncryptedBytesPrivateRSA(ByteUtil.mergeBytes(list),
								new KeyReader().getPrivateKey(privatkey.getKey())), false));
			} catch (Exception e) {
				e.printStackTrace();
			}
			encryptedMap = text.toString();
		} else {
			throw new EncryptException("not same public and private key when encrypt");
		}
	}
	public void fromString(String mapstring) {
		if (mapstring != null && !mapstring.equals("")) {
			mapstring = rtrim(mapstring);
			keepalivelist.clear();
			String[] lines = mapstring.split("\n");
			// TODO: get encrypt key ID
			String[] header = rtrim(lines[0]).split(",");
			mapversion = Long.valueOf(rtrim(header[0]));
			if (header.length > 1 && publickey != null) {
				// map file are encrypted
				String keyid = header[1];
				// compare key
				if (publickey.getGuid().toString().equals(keyid)) {
					try {
						byte[] map = Encryption.getEncryption(EncryptionType.standard).getDecryptedBytesPublicRSA(Base64.decode(lines[1]),
								new KeyReader().getPublicKey(publickey.getKey()));
						// Split
						int x = 0;
						for (int i = 0; i < map.length; i++) {
							if (map[i] == '#' && map[i + 1] == '#' && map[i + 2] == '#' && x > 0) {
								byte[] buff = new byte[x];
								System.arraycopy(map, i - x, buff, 0, buff.length);
								keepalivelist.add(new KeepAliveAddressEntry(buff));
								i = i + 2;
								x = 0;
							} else {
								x++;
							}
						}
						encryptedMap = mapstring;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				for (int i = 1; i < lines.length; i++) {
					keepalivelist.add(new KeepAliveAddressEntry(rtrim(lines[i])));
				}
			}
		}
	}
	/* remove trailing whitespace */
	public static String rtrim(String source) {
		return source.replaceAll("\\s+$", "");
	}
	public List<NetAddress> getUserAddresses(byte[] userIdHash) {
		// core locate
		// get all possible keep alive servers for a user id
		// make priority
		// return a list of address
		// read list from bottom, Most significant
		TreeMap<String, NetAddress> list = new TreeMap<String, NetAddress>();
		List<NetAddress> addresses = getBootAddresses();
		for (int i = keepalivelist.size() - 1; i >= 0; i--) {
			KeepAliveAddressEntry ka = keepalivelist.get(i);
			if (ka.isValid(userIdHash)) {
				for (NetAddress adr : ka.getAddresses()) {
					if (!list.containsKey(adr.getAddressPort())) {
						list.put(adr.getAddressPort(), adr);
						addresses.add(adr);
					}
				}
			}
		}
		// return a list of keep alive address, starting from most significant
		return addresses;
	}
	public List<NetAddress> getAddresses() {
		// read list from bottom, Most significant
		TreeMap<String, NetAddress> list = new TreeMap<String, NetAddress>();
		List<NetAddress> addresses = getBootAddresses();
		for (int i = keepalivelist.size() - 1; i >= 0; i--) {
			KeepAliveAddressEntry ka = keepalivelist.get(i);
			for (NetAddress adr : ka.getAddresses()) {
				if (!list.containsKey(adr.getAddressPort())) {
					list.put(adr.getAddressPort(), adr);
					addresses.add(adr);
				}
			}
		}
		for (int i = keepalivelist.size() - 1; i >= 0; i--) {
			KeepAliveAddressEntry ka = keepalivelist.get(i);
			for (NetAddress adr : ka.getAddresses()) {
				if (!list.containsKey(adr.getAddressPort())) {
					list.put(adr.getAddressPort(), adr);
					addresses.add(adr);
				}
			}
		}
		// return a list of keep alive address, starting from most significant
		return addresses;
	}
}
