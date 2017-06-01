package org.jdbdt;

@SuppressWarnings("javadoc")
public enum BuildEnvironment {
  AppVeyor,
  Travis,
  Default;
  
  static final String ENV_VAR = "BUILD_ENVIRONMENT";
  
  public static BuildEnvironment get() {
    String envSetting = System.getenv(ENV_VAR);
    BuildEnvironment env = Default;
    if (envSetting != null) {
      try {
        env = Enum.valueOf(BuildEnvironment.class, envSetting);
      } 
      catch (IllegalArgumentException e) { 
        // stick with default
      }
    }
    return env;
  }
}
