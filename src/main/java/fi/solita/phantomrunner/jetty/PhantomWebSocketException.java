package fi.solita.phantomrunner.jetty;

public class PhantomWebSocketException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public PhantomWebSocketException(String msg) {
        super(msg);
    }
}
