package org.jdbdt;

/**
 * Information regarding a call to the JDBDT API facade.
 * 
 * @since 0.1
 */
final class CallInfo {
  /**
   * Method info.
   */
  static final class MethodInfo {
    /** Class name. */
    private final String className;
    /** Method name. */
    private final String methodName;
    /** File name. */
    private final String fileName; 
    /** Line number. */
    private final int lineNumber;

    /**
     * Constructor.
     * @param ste Stack trace information.
     */
    MethodInfo(StackTraceElement ste) {
      className = ste.getClassName();
      methodName = ste.getMethodName();
      fileName = ste.getFileName();
      lineNumber = ste.getLineNumber();
    }
    
    /**
     * Get class name.
     * @return The name of the class for the method at stake.
     */
    String getClassName() { return className; }
    
    /**
     * Get method name.
     * @return The name of the method.
     */
    String getMethodName() { return methodName; }
    
    /**
     * Get file name. 
     * @return The name of the source file.
     */
    String getFileName() { return fileName; }
    
    /**
     * Get line number. 
     * @return The line number information.
     */
    int getLineNumber() { return lineNumber; }
    
    @Override
    public String toString() {
      return String.format("%s.%s() [%s:%d]", 
          getClassName(),
          getMethodName(),
          getFileName(),
          getLineNumber());
    }
  }
  
  /**
   * Create call-info object. 
   * @return A new call info message with an empty message.
   */
  static CallInfo create() {
    return new CallInfo("");
  }
  
  /**
   * Create call-info object with an associated message. 
   * @param message Message to set.
   * @return A new call info message with an empty message.
   */
  static CallInfo create(String message) {
    return new CallInfo(message);
  }
  
  /**
   * Info for caller method.
   */
  private final MethodInfo callerMethodInfo; 
  
  /**
   * Info for called method.
   */
  private final MethodInfo apiMethodInfo;
  
  /**
   * Message associated to call site, if any.
   */
  private final String message; 
  
  /**
   * Stack trace offset for API method.
   */
  private static final int API_METH_STO = 3;
  
  /**
   * Stack trace offset for API caller.
   */
  private static final int CALLER_METH_STO = 4;
   
  /**
   * Constructor. 
   * @param msg Call info context message.
   */
  private CallInfo(String msg) {
    StackTraceElement[] info = Thread.currentThread().getStackTrace();
    callerMethodInfo = new MethodInfo(info[CALLER_METH_STO]);
    apiMethodInfo = new MethodInfo(info[API_METH_STO]);
    message = msg;
  }
  
  /**
   * Get caller method info.
   * @return Info for the caller method.
   */
  MethodInfo getCallerMethodInfo() {
    return callerMethodInfo;
  }
  
  /**
   * Get API method info.
   * @return Info for the API method.
   */
  MethodInfo getAPIMethodInfo() {
    return apiMethodInfo;
  }
  
  /**
   * Get descriptive message for call info.
   * @return The message for the call info.
   */
  String getMessage() {
    return message;
  }
  
  @Override
  public String toString() {
    return String.format("%s --> %s \"%s\"", 
                         getCallerMethodInfo(), 
                         getAPIMethodInfo(),
                         getMessage());
  }
}
