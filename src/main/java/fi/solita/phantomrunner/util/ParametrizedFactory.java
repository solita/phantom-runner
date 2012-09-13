package fi.solita.phantomrunner.util;

public interface ParametrizedFactory<T, P> {

	T create(P param);
	
}
