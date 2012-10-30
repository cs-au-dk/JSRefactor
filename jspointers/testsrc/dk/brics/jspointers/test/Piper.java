package dk.brics.jspointers.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

public class Piper implements Runnable {
    private Reader in;
    private Writer out;
    
    public Piper(Reader in, Writer out) {
        this.in = in;
        this.out = out;
    }
    public Piper(InputStream in, OutputStream out) {
        this.in = new InputStreamReader(in);
        this.out = new OutputStreamWriter(out);
    }

    @Override
    public void run() {
        try {
            int data;
            while ((data = in.read()) != -1) {
                out.write(data);
            }
            in.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
