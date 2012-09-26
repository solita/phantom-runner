package fi.solita.phantomrunner;

public interface PhantomServer {

    PhantomServer start() throws Exception;
    PhantomServer stop() throws Exception;
    PhantomProcessNotifier createNotifier();
    
}
