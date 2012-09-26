package fi.solita.phantomrunner;

public class PhantomProcessException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public PhantomProcessException(Throwable parent) {
        super(parent);
    }
    
    public PhantomProcessException(String msg, Throwable parent) {
        super(msg, parent);
    }
}
