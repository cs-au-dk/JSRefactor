package dk.brics.jspointers.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.runner.RunWith;

import dk.brics.jsparser.AstUtil;
import dk.brics.jspointers.Main;
import dk.brics.jspointers.test.instrument.InstrumentData;
import dk.brics.jspointers.test.instrument.Instrumenter;
import dk.brics.jspointers.test.instrument.JsPointersInstrumentData;

@RunWith(AutoTester.class)
@TestConfig(testFolder="test/instrument-tests",outputFolder="output/instrument")
public class RunInstrumentTests implements TestExecutor {

    private String instrumentLib;
    private static final File instrumentedOutputDir = new File("output/instrument");
    private ExecutorService threads;
    private Timer timer;

    @Override
    public void executeTest(TestCase test) throws Throwable {
        File file = test.getFile();
        final Main main = new Main(false, file);
        
        InstrumentData data = new JsPointersInstrumentData(main);
        
        new Instrumenter(data).instrument();
        
        File out = new File(instrumentedOutputDir, file.getName());
        out.getParentFile().mkdirs();
        String code = AstUtil.toSourceString(main.getUserFiles().get(0).getAst());
        Writer writer = new BufferedWriter(new FileWriter(out));
        try {
            writer.write(instrumentLib);
            writer.write(code);
        } finally {
            writer.close();
        }
        
        ProcessBuilder pb = new ProcessBuilder("v8-shell", file.getName());
        pb.directory(instrumentedOutputDir);
        final Process proc = pb.start();
        final boolean[] done = new boolean[1];
        final boolean[] timeout = new boolean[1];
        StringWriter output = new StringWriter();
        threads.submit(new Piper(new InputStreamReader(proc.getInputStream()), output));
        threads.submit(new Piper(new InputStreamReader(proc.getErrorStream()), output));
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (done) {
                    if (done[0])
                        return;
                    proc.destroy(); // timeout if it takes too long
                }
                synchronized (timeout) {
                    timeout[0] = true;
                }
            }
        }, 60000);
        
        int exitCode = proc.waitFor();
        synchronized (done) {
            done[0] = true;
        }
        
        if (exitCode != 0) {
            String msg = output.getBuffer().toString();
            if (timeout[0]) {
                msg += "\nTimeout!";
            }
            throw new RuntimeException(msg);
        }
    }

    @Override
    public boolean shouldTestRun(TestCase test) {
        return true;
    }

    @Override
    public void initialize(boolean isSingleTest) {
        instrumentLib = TestUtil.readResource("dk/brics/jspointers/test/instrument/instrumentlib.js");
        timer = new Timer();
        threads = Executors.newFixedThreadPool(2);
    }

    @Override
    public void close() {
    }
    
}
