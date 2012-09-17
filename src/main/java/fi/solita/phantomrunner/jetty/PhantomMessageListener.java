package fi.solita.phantomrunner.jetty;

import com.fasterxml.jackson.databind.JsonNode;

public interface PhantomMessageListener {

	void message(JsonNode readTree);

}
