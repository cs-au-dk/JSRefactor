package dk.brics.jscontrolflow;

import java.io.File;

/**
 * Describes a location in a source code file. This information is rather coarse,
 * and is best used for debugging and very simple output.
 * 
 * @author Asger
 */
public class SourceLocation {
    private File file;
    private int lineNumber;
    private int linePosition;

    public SourceLocation(File file, int lineNumber, int linePosition) {
        this.file = file;
        this.lineNumber = lineNumber;
        this.linePosition = linePosition;
    }

    public File getFile() {
        return file;
    }
    public void setFile(File file) {
        this.file = file;
    }
    public int getLineNumber() {
        return lineNumber;
    }
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
    public int getLinePosition() {
        return linePosition;
    }
    public void setLinePosition(int linePosition) {
        this.linePosition = linePosition;
    }

    @Override
    public String toString() {
        return file.getName() + ":" + lineNumber + ":" + linePosition;
    }

}
