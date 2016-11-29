package org.jdbdt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jdbdt.CallInfo.MethodInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * JDBDT log.
 * 
 * @since 0.1
 *
 */
final class Log implements AutoCloseable {
  
  /**
   * Creator method (stream variant).
   * @param out Output stream.
   * @return A new log instance.
   */
  static Log create(PrintStream out) {
    return new Log(out, true);
  }
  
  /**
   * Creator method (file variant).
   * @param outputFile Output file.
   * @return A new log instance.
   */
  static Log create(File outputFile) {
    try {
      return new Log(new PrintStream(outputFile), false);
    } 
    catch (FileNotFoundException e) {
      throw new InvalidOperationException("File does not exist.", e);
    }
  }
  
  /**
   * Output stream.
   */
  private final PrintStream out;
  
  /**
   * Closing behavior flag.
   */
  private final boolean ignoreClose;
  
  /**
   * XML document instance.
   */
  private final Document xmlDoc;

  /**
   * Constructor.
   * @param out Output stream.
   * @param ignoreClose If true, calls to {@link #close()} on the
   * created log will not close the output stream.
   */
  private Log(PrintStream out, boolean ignoreClose) {
    this.out = out;
    this.ignoreClose = ignoreClose;
    this.xmlDoc = XML_DOC_FACTORY.newDocument(); 
  }

  @SuppressWarnings({ "javadoc" })
  private Element root(CallInfo callInfo) {
    Element e = createNode(null, ROOT_TAG);
    e.setAttribute(VERSION_TAG, JDBDT.version());
    e.setAttribute(TIME_TAG, new Timestamp(System.currentTimeMillis()).toString());
    write(e, callInfo);
    return e;
  }

  @SuppressWarnings("javadoc")
  private void flush(Element rootNode) {
    DOMSource ds = new DOMSource(rootNode);
    try {
      StreamResult sr = new StreamResult(out);
      XML_TRANSFORMER.transform(ds, sr);
      out.flush();
    } 
    catch (Exception e) {
      throw new InternalAPIError(e);
    } 
  }

  /**
   * Close the log.
   * 
   * <p>
   * The log should not be used after this method is called.
   * </p>
   * 
   */
  @Override
  public void close() {
    if (!ignoreClose) {
      out.close();
    }
  }
  
  /**
   * Write a data set to the log.
   * @param callInfo Call info.
   * @param data Data set.
   */
  void write(CallInfo callInfo, DataSet data) {
    Element rootNode = root(callInfo);
    write(rootNode, data.getSource());
    Element dsNode = createNode(rootNode, DATA_SET_TAG);
    write(dsNode, ROWS_TAG, data.getSource().getMetaData().columns(), data.getRows().iterator());
    flush(rootNode);
  }

  @SuppressWarnings("javadoc")
  private void write(Element parent, DataSource source) {
    Element node = createNode(parent, DATA_SOURCE_TAG);
    node.setAttribute(JAVA_TYPE_TAG, source.getClass().getName());
    write(node, source.getMetaData());
    writeSQL(node, source.getSQLForQuery());
  }
  
  @SuppressWarnings("javadoc")
  private void write(Element root, CallInfo info) {
    Element ctxNode = createNode(root, CONTEXT_TAG);
    if (info.getMessage().length() > 0) {
      simpleNode(ctxNode, CTX_MESSAGE_TAG, info.getMessage());
    }
    write(ctxNode, CTX_CALLER_TAG, info.getCallerMethodInfo());
    write(ctxNode, CTX_API_METHOD_TAG, info.getAPIMethodInfo());
  }
  
  @SuppressWarnings("javadoc")
  private void write
  (Element ctxNode, String tag, MethodInfo mi) {
     Element miNode = createNode(ctxNode, tag);
     simpleNode(miNode, CTX_CLASS_TAG, mi.getClassName());
     simpleNode(miNode, CTX_METHOD_TAG, mi.getMethodName());
     simpleNode(miNode, CTX_FILE_TAG, mi.getFileName());
     simpleNode(miNode, CTX_LINE_TAG, String.valueOf(mi.getLineNumber()));
  }
  
  @SuppressWarnings("javadoc")
  private void write(Element parent, MetaData md) {
    int index = 1;
    Element node = createNode(parent, COLUMNS_TAG);
    node.setAttribute(COUNT_TAG, String.valueOf(md.getColumnCount()));
    for (MetaData.ColumnInfo col : md.columns()) {
      Element colNode = createNode(node, COLUMN_TAG);
      colNode.setAttribute(INDEX_TAG, String.valueOf(index++));
      colNode.setAttribute(LABEL_TAG, col.label());
      colNode.setAttribute(SQL_TYPE_TAG, col.type().toString());
    } 
  }
  
  /**
   * Log SQL code.
   * @param callInfo Call info.
   * @param sql SQL code.
   */
  void writeSQL(CallInfo callInfo, String sql) {
    Element rootNode = root(callInfo);
    writeSQL(rootNode, sql);
    flush(rootNode);
  }
  
  @SuppressWarnings("javadoc")
  private void writeSQL(Element parent, String sql) {
    Element sqlNode = createNode(parent, SQL_TAG);
    sqlNode.appendChild(xmlDoc.createCDATASection(sql));
  }
  
  /**
   * Log delta assertion.
   * @param callInfo Call info.
   * @param assertion Delta assertion.
   */
  void write(CallInfo callInfo, DeltaAssertion assertion) {
    final Element rootNode = root(callInfo);
    final DataSource ds = assertion.getSource();
    write(rootNode, ds);
    final Element daNode = createNode(rootNode, DELTA_ASSERTION_TAG);
    final List<MetaData.ColumnInfo> mdCols = ds.getMetaData().columns();
    final Element expectedNode = createNode(daNode, EXPECTED_TAG);    
    write(expectedNode, 
          OLD_DATA_TAG, 
          mdCols,
          assertion.data(DeltaAssertion.IteratorType.OLD_DATA_EXPECTED));
    write(expectedNode, 
          NEW_DATA_TAG, 
          mdCols,
          assertion.data(DeltaAssertion.IteratorType.NEW_DATA_EXPECTED));
    if (! assertion.passed()) {
      Element errorsNode = createNode(daNode, ERRORS_TAG),
              oldDataErrors = createNode(errorsNode, OLD_DATA_TAG),
              newDataErrors = createNode(errorsNode, NEW_DATA_TAG);
      write(oldDataErrors, 
            EXPECTED_TAG, 
            mdCols,
            assertion.data(DeltaAssertion.IteratorType.OLD_DATA_ERRORS_EXPECTED));
      write(oldDataErrors, 
            ACTUAL_TAG, 
            mdCols,
            assertion.data(DeltaAssertion.IteratorType.OLD_DATA_ERRORS_ACTUAL));
      write(newDataErrors, 
          EXPECTED_TAG, 
          mdCols,
          assertion.data(DeltaAssertion.IteratorType.NEW_DATA_ERRORS_EXPECTED));
      write(newDataErrors, 
            ACTUAL_TAG, 
            mdCols,
            assertion.data(DeltaAssertion.IteratorType.NEW_DATA_ERRORS_ACTUAL));
    }
    flush(rootNode);
  }

  /**
   * Log plain call information.
   * @param callInfo Call information.
   */
  void writeCallInfo(CallInfo callInfo) {
    final Element rootNode = root(callInfo); 
    createNode(rootNode, callInfo.getAPIMethodInfo().getMethodName());
    flush(rootNode);
  }
  
  /**
   * Log state assertion.
   * @param callInfo Call info.
   * @param assertion State assertion.
   */
  void write(CallInfo callInfo, DataSetAssertion assertion) {
    final Element rootNode = root(callInfo); 
    final DataSource ds = assertion.getSource();
    write(rootNode, ds);
    final Element saNode = createNode(rootNode, ASSERTION_TAG);
    final List<MetaData.ColumnInfo> mdCols = ds.getMetaData().columns();
    write(saNode, 
          EXPECTED_TAG, 
          mdCols,
          assertion.data(DataSetAssertion.IteratorType.EXPECTED_DATA));
    if (! assertion.passed()) {
      Element errorsNode = createNode(saNode, ERRORS_TAG);
      write(errorsNode, 
            EXPECTED_TAG, 
            mdCols,
            assertion.data(DataSetAssertion.IteratorType.ERRORS_EXPECTED));
      write(errorsNode, 
            ACTUAL_TAG, 
            mdCols,
            assertion.data(DataSetAssertion.IteratorType.ERRORS_ACTUAL));
    }
    flush(rootNode);
  }
  @SuppressWarnings("javadoc")
  private void write(Element parent, String tag, List<MetaData.ColumnInfo> columns, Iterator<Row> itr) {
    int size = 0;
    Element topNode = createNode(parent, tag);
    while (itr.hasNext()) {
      Row r = itr.next();
      Object[] data = r.data();
      Element rowElem = createNode(topNode, ROW_TAG);
      for (int i=0; i < data.length; i++) {
        Element colNode = createNode(rowElem, COLUMN_TAG);
        colNode.setAttribute(LABEL_TAG, columns.get(i).label());
        Object cValue = data[i];
        if (cValue != null) {
          String typeAttr, valueContent;
          Class<?> cClass = cValue.getClass();
          if (cClass.isArray()) {
            typeAttr = cValue.getClass().getTypeName();
            valueContent = arrayAsString(cValue, cClass.getComponentType());
          } else {
            typeAttr = cValue.getClass().getName();
            valueContent = cValue.toString();
          }
          colNode.setAttribute(JAVA_TYPE_TAG, typeAttr);
          colNode.setTextContent(valueContent);
        } else {
          colNode.setTextContent(NULL_VALUE);
        }
      }
      size++;
    }
    topNode.setAttribute(COUNT_TAG,  String.valueOf(size));
  }
  
  @SuppressWarnings("javadoc")
  private Element createNode(Element parent, String tag) {
    Element node = xmlDoc.createElement(tag);
    if (parent != null) {
      parent.appendChild(node);
    }
    return node;
  }

  @SuppressWarnings("javadoc")
  private void simpleNode(Element parent, String tag, String value) {
    Element child = createNode(parent, tag);
    child.setTextContent(value);
  }
  
  @SuppressWarnings("javadoc")
  private String arrayAsString(Object array, Class<?> elemClass) {
    return elemClass.isPrimitive() ?
        ARRAY_STRING_FORMATTERS.get(elemClass).apply(array)
      : Arrays.deepToString((Object[]) array);
  }
  
  /**
   * Document builder factory handle.
   */
  private static final DocumentBuilder XML_DOC_FACTORY;

  /**
   * Transformer handle.
   */
  private static final Transformer XML_TRANSFORMER;

  @SuppressWarnings("javadoc")
  private static final String ROOT_TAG = "jdbdt-log-message";
  @SuppressWarnings("javadoc")
  private static final String VERSION_TAG = "version";
  @SuppressWarnings("javadoc")
  private static final String TIME_TAG = "time";
  @SuppressWarnings("javadoc")
  private static final String DELTA_ASSERTION_TAG = "delta-assertion";
  @SuppressWarnings("javadoc")
  private static final String ASSERTION_TAG = "assertion";
  @SuppressWarnings("javadoc")
  private static final String ERRORS_TAG = "errors";
  @SuppressWarnings("javadoc")
  private static final String EXPECTED_TAG = "expected";
  @SuppressWarnings("javadoc")
  private static final String ACTUAL_TAG = "actual";
  @SuppressWarnings("javadoc")
  private static final String DATA_SET_TAG = "data-set";
  @SuppressWarnings("javadoc")
  private static final String DATA_SOURCE_TAG = "data-source";
  @SuppressWarnings("javadoc")
  private static final String COLUMNS_TAG = "columns";
  @SuppressWarnings("javadoc")
  private static final String OLD_DATA_TAG = "old-data";
  @SuppressWarnings("javadoc")
  private static final String NEW_DATA_TAG = "new-data";
  @SuppressWarnings("javadoc")
  private static final String ROWS_TAG = "rows";
  @SuppressWarnings("javadoc")
  private static final String ROW_TAG = "row";
  @SuppressWarnings("javadoc")
  private static final String SQL_TAG = "sql";
  @SuppressWarnings("javadoc")
  private static final String COLUMN_TAG = "column";
  @SuppressWarnings("javadoc")
  private static final String LABEL_TAG = "label";
  @SuppressWarnings("javadoc")
  private static final String SQL_TYPE_TAG = "sql-type";
  @SuppressWarnings("javadoc")
  private static final String JAVA_TYPE_TAG = "java-type";
  @SuppressWarnings("javadoc")
  private static final String INDEX_TAG = "index";
  @SuppressWarnings("javadoc")
  private static final String COUNT_TAG = "count";
  @SuppressWarnings("javadoc")
  private static final String NULL_VALUE = "NULL";
  @SuppressWarnings("javadoc")
  private static final String CONTEXT_TAG = "context";
  @SuppressWarnings("javadoc")
  private static final String CTX_CALLER_TAG = "caller";
  @SuppressWarnings("javadoc")
  private static final String CTX_API_METHOD_TAG = "api-method";
  @SuppressWarnings("javadoc")
  private static final String CTX_LINE_TAG = "line";
  @SuppressWarnings("javadoc")
  private static final String CTX_FILE_TAG = "file";
  @SuppressWarnings("javadoc")
  private static final String CTX_CLASS_TAG = "class";
  @SuppressWarnings("javadoc")
  private static final String CTX_MESSAGE_TAG = "message";
  @SuppressWarnings("javadoc")
  private static final String CTX_METHOD_TAG = "method";
  
  @SuppressWarnings("javadoc")
  private static final IdentityHashMap<Class<?>, Function<Object, String> > ARRAY_STRING_FORMATTERS;
  
  static {
    try {
      // XML handles
      XML_DOC_FACTORY = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      XML_TRANSFORMER = TransformerFactory.newInstance().newTransformer();
      XML_TRANSFORMER.setOutputProperty(OutputKeys.INDENT, "yes");
      XML_TRANSFORMER.setOutputProperty(OutputKeys.METHOD, "xml");
      XML_TRANSFORMER.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      XML_TRANSFORMER.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      XML_TRANSFORMER.setOutputProperty(OutputKeys.STANDALONE, "no");
      XML_TRANSFORMER.setOutputProperty(OutputKeys.VERSION, "1.0");
      XML_TRANSFORMER.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      // "Array string formatters"
      ARRAY_STRING_FORMATTERS = new IdentityHashMap<>();
      ARRAY_STRING_FORMATTERS.put(Boolean.TYPE, o -> Arrays.toString((boolean[]) o));
      ARRAY_STRING_FORMATTERS.put(Byte.TYPE, o -> Arrays.toString((byte[]) o));
      ARRAY_STRING_FORMATTERS.put(Character.TYPE, o -> Arrays.toString((char[]) o));
      ARRAY_STRING_FORMATTERS.put(Double.TYPE, o -> Arrays.toString((double[]) o));
      ARRAY_STRING_FORMATTERS.put(Float.TYPE, o -> Arrays.toString((float[]) o));
      ARRAY_STRING_FORMATTERS.put(Integer.TYPE, o -> Arrays.toString((int[]) o));
      ARRAY_STRING_FORMATTERS.put(Long.TYPE, o -> Arrays.toString((long[]) o));
      ARRAY_STRING_FORMATTERS.put(Short.TYPE, o -> Arrays.toString((short[]) o));
    } catch (ParserConfigurationException | TransformerConfigurationException
        | TransformerFactoryConfigurationError e) {
      throw new InternalAPIError(e);
    }
  }
  
}
