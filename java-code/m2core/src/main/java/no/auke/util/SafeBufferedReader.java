package no.auke.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/*
The readline( ) in BufferReader tends to hang its thread when reading streams where lines end in carriage returns,
as is commonly the case when the streams derive from a Macintosh or a Macintosh text file
*/
public class SafeBufferedReader extends BufferedReader {

    private boolean _newLineFound = false;

    public SafeBufferedReader(Reader in, int bufferSize) {
        super(in, bufferSize);
    }

    public SafeBufferedReader(Reader in) {
        this(in, 1024);
    }

    public String readLine() throws IOException {

        StringBuilder sb = new StringBuilder("");
        while (true) {
            int c = this.read();
            if (c == -1) {
                return sb.toString();
            } else if ((char) c == '\r') {
                _newLineFound = true;
                return sb.toString();
            } else if ((char) c == '\n') {
                if (_newLineFound) {
                    _newLineFound = false;
                    continue;
                } else return sb.toString();
            } else {
                _newLineFound = false;
                sb.append((char) c);
            }
        }
    }


}
