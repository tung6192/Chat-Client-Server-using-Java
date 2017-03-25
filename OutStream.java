package Assignment.Assignment3;

import java.io.OutputStream;

/**
 * Created by dinhtungtp on 3/17/2017.
 */
public class OutStream
{
    private String name;
    private OutputStream outputStream;

    public OutStream(String name, OutputStream outputStream) {
        this.name = name;
        this.outputStream = outputStream;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }
}
