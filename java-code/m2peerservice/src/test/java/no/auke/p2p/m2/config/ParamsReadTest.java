package no.auke.p2p.m2.config;

import junit.framework.TestCase;
import no.auke.p2p.m2.InitVar;
import no.auke.p2p.m2.general.ParamReader;
import no.auke.util.FileUtil;

public class ParamsReadTest extends TestCase {

	public void testOnReadWrite()
	{
		FileUtil.deleteFile("m2.init");
		//read if existing or write to disk
		ParamReader.readIfExisting(); 
		//if not existing, should write content in the file
		assertTrue(FileUtil.isFileExists("m2.init"));
		//now we can read content from disk
		ParamReader.readIfExisting();
		//change params
		InitVar.PACKET_SIZE = 400;
		InitVar.DEBUG_LEVEL = 1;
		//and write back to m2.init after changing values
		ParamReader.write();
		//ok, read again, should load correctly
		ParamReader.readIfExisting();
		//param changed according to the file
		assertTrue(InitVar.PACKET_SIZE == 400);
		assertTrue(InitVar.DEBUG_LEVEL == 1);
	}
	
}
