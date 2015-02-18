package org.renjin.test;

import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class JvmMethodInDependencyTest {
  
  @Test
  public void scriptEngineTest() throws ScriptException {
    ScriptEngineManager sem = new ScriptEngineManager();
    ScriptEngine renjin = sem.getEngineByName("Renjin");
    renjin.eval("library(org.renjin.jvmdep)");
    renjin.eval("stopifnot(executeJvmMethodInDependency() == 'ClassInTransitiveJvmDep')");
  }
}
