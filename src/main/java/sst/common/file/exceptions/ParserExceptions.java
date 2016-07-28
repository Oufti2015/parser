package sst.common.file.exceptions;

public class ParserExceptions extends RuntimeException {

    public ParserExceptions() {
    }

    public ParserExceptions(String message) {
        super(message);
    }

    public ParserExceptions(Throwable cause) {
        super(cause);
    }

    public ParserExceptions(String message, Throwable cause) {
        super(message, cause);
    }
}
