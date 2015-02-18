package org.renjin.test;

public class ClassInTransitiveJvmDep {
  
  
  public static String execute() {
    return ClassInTransitiveJvmDep.class.getSimpleName();
  }
}
