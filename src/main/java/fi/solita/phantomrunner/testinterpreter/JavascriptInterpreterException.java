package fi.solita.phantomrunner.testinterpreter;

public class JavascriptInterpreterException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public JavascriptInterpreterException(String msg, Exception parent) {
        super(msg, parent);
    }

}
