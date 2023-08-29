/*
 * This file is part of Smooby project,  
 * 
 * Copyright (c) 2011-2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */

package no.auke.util.old;

import java.io.*;
import java.util.*;

public class FileMerge {
    private List<String> files;
    private File file;

    public FileMerge(File FileSpec) {
        file = FileSpec;
        if (file.exists()) file.delete();
    }

    public void Add(String filename) {
        if (files == null) files = new ArrayList<String>();
        files.add(filename);
    }

    public void Merge() throws IOException {
        if (files == null || files.size() == 0) return;
        Iterator<String> it = files.iterator();
        OutputStream ou = null;
        byte[] buff = new byte[102400];
        while (it.hasNext()) {
            String chunkName = it.next();
            if (ou == null) ou = new FileOutputStream(file);
            InputStream is = new BufferedInputStream(new FileInputStream(chunkName), 102400);
            while (true) {
                int len = is.read(buff);
                if (len < 0) break;
                ou.write(buff, 0, len);
            }
            is.close();
        }
        ou.close();
    }
}