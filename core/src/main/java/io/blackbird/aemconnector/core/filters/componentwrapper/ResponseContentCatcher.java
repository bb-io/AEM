package io.blackbird.aemconnector.core.filters.componentwrapper;

import lombok.Getter;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.CharArrayWriter;
import java.io.PrintWriter;

public class ResponseContentCatcher extends HttpServletResponseWrapper {

    private final CharArrayWriter buffer = new CharArrayWriter();

    @Getter
    private boolean writerUsed = false;

    public ResponseContentCatcher(HttpServletResponse response) {
        super(response);
    }

    @Override
    public PrintWriter getWriter() {
        writerUsed = true;
        return new PrintWriter(buffer) {
            @Override
            public void close() {
                flush();
            }
        };
    }

    public String getContent() {
        return buffer.toString();
    }

    public boolean hasContent() {
        return writerUsed && buffer.size() > 0;
    }
}
