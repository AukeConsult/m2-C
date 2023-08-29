package no.auke.util;

import java.net.*;
import java.util.*;

import static java.lang.System.out;

public class ListNetworks {

	static void displayInterfaceInformation(NetworkInterface netint)
	        throws SocketException {
		out.printf("Display name: %s\n", netint.getDisplayName());
		out.printf("Name: %s\n", netint.getName());
		Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
		for (InetAddress inetAddress : Collections.list(inetAddresses)) {
			out.printf("InetAddress: %s\n", inetAddress);
		}
		out.printf("\n");
	}


	static public List<String> getIPv4Addresses() {

		List<String> ipv4addrReturn = new ArrayList<String>();
		try {


			//SortedMap<Integer, String> ipv4addr = new TreeMap<Integer, String>();
			List<String> ipv4addr = new LinkedList<String>();
			Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
			
			for (NetworkInterface netint : Collections.list(nets)) {

			    //if(netint.isUp()&&netint.getParent()==null)
			    {        
			    	
    				Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
    				for (InetAddress inetAddress : Collections.list(inetAddresses)) {
    					
    					
    					if (inetAddress.toString().contains(".")) {
    
    						String a = inetAddress.toString();
    
    						if (a.contains("/127.0.0")) {
    							
    							// ipv4addr.put(1100+cnt, a.substring(1));
    						} else if (a.contains("/169.254")) {
    								
    							//ipv4addr.put(1000+cnt, a.substring(1));
    							ipv4addr.add( a.substring(1));
    							
    						} else if (a.contains("/10.")) {
    							
    							//ipv4addr.put(700+cnt, a.substring(1));
    							ipv4addr.add( a.substring(1));
    
    						} else if (a.contains("/192.168")) {
    							
    							//ipv4addr.put(1000+cnt, a.substring(1));
    							ipv4addr.add( a.substring(1));
    
    						} else if (a.contains("/172.")) {
    
    							//ipv4addr.put(500+cnt, a.substring(1));
    							ipv4addr.add( a.substring(1));
    
    						} else {
    
    							//ipv4addr.put(100+cnt, a.substring(1));
    							ipv4addr.add( a.substring(1));
    						}
    						
    					}
    				}
			    }
			}

//			for(Integer key : ipv4addr.keySet())
//			{
//				ipv4addrReturn.add(ipv4addr.get(key));
//			}
			ipv4addrReturn.addAll(ipv4addr);
			
		} catch (SocketException se) {
		}
		
		return ipv4addrReturn;

	}


	private static String getIpFromDns(String address) {
		try {

			InetAddress adr = InetAddress.getByName(address);
			String addressIP = adr.getHostAddress();
			return addressIP;

		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		return null;
	}

	public static String getIPv4Address() {

		List<String> ipv4addrReturn = ListNetworks.getIPv4Addresses();
		for (String ip : ipv4addrReturn) {

			if (ip != null && !ip.equals("")) {
				return getIpFromDns(ip);
			}
		}
		return "";
	}
	
	public static String getMacAddress() {
        
    	String macaddress = "";
    	try {

            Enumeration<NetworkInterface> nets =
                    NetworkInterface.getNetworkInterfaces();
            
            for (NetworkInterface netint : Collections.list(nets)) {
            
            	if (Arrays.toString(netint.getHardwareAddress()).length() >7){
            		macaddress = Arrays.toString(netint.getHardwareAddress());
            	}
            }            
        } catch (SocketException e) {
        }
    	
    	return macaddress;
    }
	
	public static byte[] getMacAddressByte() {
        
    	try {

            Enumeration<NetworkInterface> nets =
                    NetworkInterface.getNetworkInterfaces();
            
            for (NetworkInterface netint : Collections.list(nets)) {
            	if (netint.getHardwareAddress()!=null && netint.getHardwareAddress().length >7){
            		return netint.getHardwareAddress();
            	}            	
            }            
        } catch (SocketException e) {
        }
    	
    	return null;
    }	


}
