package no.auke.p2p.m2;

import no.auke.p2p.m2.general.Description;


public class InitVar {
	
// -------- fixed variables --------------------------------------------------------------
	
	// standard boot

	public static String BOOT_ADDRESS="175.41.246.31:8434,176.34.59.198:8434,176.34.59.202:8434,176.34.59.204:8434";
	//public static String BOOT_ADDRESS="89.221.242.157:8450,89.221.242.157:8451,175.41.246.31:8434,176.34.59.198:8434,176.34.59.202:8434,176.34.59.204:8434";
	
	public static String TEST_BOOT_ADDRESS="89.221.242.155:8450,89.221.242.155:8451,89.221.242.155:8452,89.221.242.155:8453";
	
	// will use TEST_BOOT_ADDRESS when set
	public static boolean USE_TEST_KA=false;
	
	// packet size (max)
	public static int PACKET_SIZE = 512;
	
	// stream packet length (max)
	public static int STREAM_MAX_DATA_LENGTH = 512;
	
	// default delay between each stream packet
	public static double STREAM_PACKET_DELAY = 1;
	
	// number of packets in each chunk
	public static int CHUNK_SIZE = 8;

	// agent pool frequency
	final public static int POOL_WAIT = 15000;

	// agent pool minimum connected agents before new search
	final public static int POOL_MINIMUM_AGENTS = 1;

	// agent pool maximum connected agents before new search
	final public static int POOL_MAXIMUM_AGENTS = 6;

	// Keep alive agent
	// period to add to frequency
	final public static int ADD_PING_PERIOD = 15000;

	// max ping frequency period
	final public static int MAX_PING_PERIOD = 90000;

	// number of pin trials before give up on KA server
	final public static int MAX_PING_TRIALS = 3;

	// time to wait before check ping is returned from KA server
	final public static int KEEPALIVE_WAIT = 2000; // 5 seconds
	
	// number of middle to be avail on KA before MM is used
	public static int NUMBER_OF_MIDDLEMAN_REQURIED = 2;

	// default encryption method (not in use, hardcoded in cypher classes )
	public static int ENCRYPTION = 0;

    // Max waiting incoming socket bufferes to be handled")
	public static final int MAX_INCOMING_SOCKET_BUFFERS = 25;

    // Size outgoing com channel buffer    
	public static int OUT_QUEUE_SIZE = 2000;
	// Size incoming com channel buffer  
    public static int IN_QUEUE_SIZE = 2000;	
    
    // because not overload KA with request for lookup from application
    // when a ID is reported not found
    // Wait at least ms for new lookup in KA after not found
	public static long WAIT_FOR_LOOKUP_PERIOD = 1500;

    // Startup connection wait time (ms)"
	public static int START_WAIT = 10000;

	public static int SYNC_SOCKET_TIMEOUT = 10000;

	// max time a message is in buffer, waiting to complete
	public static long MESSAGE_MAX_TIME_WAIT_TO_COMPLETE = 60000; // 30 sec wait for cleanup    
	
// ========================================================== 
// changeable params 
// ========================================================== 
	
		
	@Description("//debug level")
	public static int DEBUG_LEVEL = 1;
	
// ---------------------------------------------------------------
// enable license
// ---------------------------------------------------------------
	
	public static boolean NO_LICENSE_CHECK=true;
	public static boolean NO_CONTACT_LICENSE_SERVER_WHEN_CHECK = false;

	// Frequency checking license 
	// one an hour, or when startup
	
	public static final long LICENSE_CHECK_FREQUENCY = 60 * 60 * 1000;
	
	// License server
    
	//@Description("//Default license server")    
	public static String LICENSE_SERVER = "89.221.242.80:8034";
	
    //@Description("//Default trial license server")    
    public static String TRIAL_LICENSE_SERVER = "89.221.242.80:8034";
		
// ------------------------------------------------------------
// Speed
// ------------------------------------------------------------
	
	@Description("// Max speed in kb pr. second ")
	public static int MAX_SPEED = 500;	

	@Description("// Minimum speed in kb pr. second ")
	public static int MIN_SPEED = 25;	
	
	@Description("// Fixed speed in kb pr. second ")
	public static int FIXED_SPEED = 0;	
	
// ---------------------------------------------------	
// chunking
// ---------------------------------------------------	
	
	@Description("// Minimum wait for resend pr. chunk. (ms)")	
	public static int SEND_RESEND_TIMEOUT = 1500;
	
	@Description("// Max number of chunk resend times")	
	public static int CHUNK_RESENDS = 5;

	@Description("// Maximum abort timeout complete pr. chunk. (ms)")
	public static  int SEND_ABORT_TIMEOUT = 3000;

    @Description("// Timeout recieve, receiving is aborted if no data arrives in this timeframe (ms)")    
    public static int RECIEVE_TIMEOUT = 30000;

    @Description("// Simulate 1 packets loss pr. number of packets (ms) (for testing purpose) ")
    public static int PACKET_LOSS_SIMULATE = 0;

// ----------------------------------------------------------------------
// session connection
// ----------------------------------------------------------------------
    
    // direct connect active or not
    // does not work for the moment
    
    @Description("// Use direct connect, try to connect to last know IP address without lookup to KA serveres")
    public static  boolean PEER_DO_DIRECT_CONNECT = true;

	// sending ping to last known peer address
	// for direct connection, before start lookup via KA servers
    
    @Description("// Direct connect timeout, time frame tryning to do direct connect before normal lookup (ms)")
	public static  int PEER_DIRECT_CONNECT_TIMEOUT = 2000;

	// peer agent parameters
	// try to connect to peer time by sending normal NAT messages
	// if this not succeed, peer start ports scan
	// sending ping in 100ms periods
    
    @Description("// Time for waiting for request responce from KA (the time it tak to find the other peer and reply result")	
	public static int REQUEST_RESPONSE_KA_TIMEOUT = 3000;
    
    @Description("// Time for waiting for request responce from KA (the time it tak to find the other peer and reply result")	
	public static int REQUEST_RESPONSE_MIDDLEMAN_TIMEOUT = 5000;
    
    @Description("// Time frame between each packet when tryning to send hole punching packets (ms) -> (ddos delay)")
    public static int CONNECT_PING_DELAY = 250;

	// total wait for connection to another peer
	// procedure total wait, used in FindAndConnect
	// 1. Send request to KA
	// 2. got request, start negotiate and send ping WAIT_BEFORE_PORTSCAN time
	// 3. no request, send port scan MAX_PORTSCAN time
	// 4.
    
    @Description("// Max time try to connect (ms)")    
	public static int PEER_CONNECT_TIMEOUT = 10000;

 // ----------------------------------------------------------------------
 // session alive
 // ----------------------------------------------------------------------

    
	// Ping period
	// to hold connected peer open
    
    @Description("// peer ping time (ms)")  
	public static int PEER_PING_TIME = 30000; // send ping each 30 second

    @Description("// peer reply ping time (ms)")  
	public static int PEER_PING_REPLY_TIME = 5000; // time for a ping reply
    
	// time out for no ping response
	// or data from connected peer
	// assume peer is disconnected and close session
    
    @Description("// peer no ping timeout (ms)")  
	public static  int PEER_TIMEOUT = 2 * 60000; // I.e if no ping in 120 seconds, other peer regards disconnected

	// time out for no data sent
	// session is closed
    
    @Description("// peer no data timeout (ms)")  
    public static int PEER_DATA_TIMEOUT = 15 * 60000; // if no data transfer in 15 minutes, peer session is closed
    //public static int PEER_DATA_TIMEOUT = 1 * 60000; // if no data transfer in 15 minutes, peer session is closed
    
    // time out for no data sent
    // close session when fail sending
    
    @Description("// Close session when fail sending")  
    public static boolean PEER_SESSION_CLOSE_ON_FAIL = false; 
	
// -----------------------------------------------------	
// Session encryption 
// -----------------------------------------------------

	// turn on session encryption, all parties must do same
	@Description("// do session encryption")    
	public static boolean DO_SESSION_ENCYPTION = true;
	
    @Description("// timeout for init encryption between to peers")    
	public static int INIT_PEER_ENCRYPTION_TIMEOUT = 7000;   
    
    @Description("// Frequesny sending session encryption pings")    
    public static final int PING_FREQUENCY_NOT_ENCRYPTING = 150;

    @Description("// Max open messages in a ocket at then same time")    
	public static final int MAX_INCOMMING_MESSAGES = 500;
    
// Testing 
    
    // speed max, remove packets if faster
	public static int TEST_SPEED_DELAY = 0;

	public static long STREAM_PACKET_LIFETIME = 100;
	public static long DATA_PACKET_LIFETIME = 100;
	public static long CONTROLL_PACKET_LIFETIME = 50; 

}
