package fi.solita.phantomrunner.testinterpreter;

import java.io.File;
import java.util.List;

public interface JavascriptTestInterpreter {

	List<JavascriptTest> listTestsFrom(File jsFile);

}
