package fi.solita.phantomrunner.util;

import java.util.concurrent.atomic.AtomicReference;

public class ObjectMemoizer<T, P> {

	private final ParametrizedFactory<T, P> factory;
	private final AtomicReference<T> instance = new AtomicReference<>();
	private final P parameter;
	
	public ObjectMemoizer(ParametrizedFactory<T, P> factory, P parameter) {
		this.factory = factory;
		this.parameter = parameter;
	}
	
	public T get() {
		T data = instance.get();
		if (data == null) {
			data = this.factory.create(parameter);
			instance.set(data);
		}
		return data;
	}
}
