package org.jdbdt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

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
final class Log {
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
   * Construct a log with an associated output file.
   * @param ouputFile Output file.
   * @throws FileNotFoundException If the file cannot be opened/created. 
   */
  Log(File ouputFile) throws FileNotFoundException {
    this(new PrintStream(new FileOutputStream(ouputFile)), false);
  }
  /**
   * Construct a log with an associated output stream.
   * @param out Output stream.
   */
  Log(PrintStream out) {
    this(out, true);
  }
  
  /**
   * General constructor.
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
  private Element root() {
    Element e = createNode(null, ROOT_TAG);
    e.setAttribute(VERSION_TAG, JDBDT.version());
    e.setAttribute(TIME_TAG, new Timestamp(System.currentTimeMillis()).toString());
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
    catch (Throwable e) {
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
  void close() {
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
    Element rootNode = root(),
            dsNode = createNode(rootNode, DATA_SET_TAG),
            columnsNode = createNode(dsNode, COLUMNS_TAG),
            rowsNode = createNode(dsNode, ROWS_TAG);
    write(rootNode, callInfo);
    write(columnsNode, data.getSource().getMetaData());
    write(rowsNode, data.getSource().getMetaData().columns(), data.getRows().iterator());
    flush(rootNode);
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
  private void write(Element topNode, MetaData md) {
    int index = 1;
    topNode.setAttribute(COUNT_TAG, String.valueOf(md.getColumnCount()));
    for (MetaData.ColumnInfo col : md.columns()) {
      Element colNode = createNode(topNode, COLUMN_TAG);
      colNode.setAttribute(INDEX_TAG, String.valueOf(index++));
      colNode.setAttribute(LABEL_TAG, col.label());
      colNode.setAttribute(SQL_TYPE_TAG, col.type().toString());
    } 
  }
  
  /**
   * Log SQL code.
   * @param sql SQL code.
   */
  void writeSQL(String sql) {
    Element rootNode = root(),
            sqlNode = createNode(rootNode, SQL_TAG);
    sqlNode.appendChild(xmlDoc.createCDATASection(sql));
    flush(rootNode);
  }
  
  /**
   * Write the state of a delta to the log.
   * @param callInfo Call info.
   * @param d Delta instance.
   */
  void write(CallInfo callInfo, Delta d) {
    Element rootNode = root(),
            deltaNode = createNode(rootNode, DELTA_TAG),
            queryNode = createNode(deltaNode, COLUMNS_TAG),
            bSetNode = createNode(queryNode, BEFORE_TAG),
            aSetNode = createNode(queryNode, AFTER_TAG);
    write(rootNode, callInfo);
//    write(queryNode, d.getMetaData());
//    deltaNode.setAttribute(SIZE_TAG, String.valueOf(d.size()));
//    write(bSetNode, d.getMetaData().columns(), d.getIterator(DBDelta.IteratorType.ACTUAL_OLD_DATA));
//    write(aSetNode, d.getMetaData().columns(), d.getIterator(DBDelta.IteratorType.ACTUAL_NEW_DATA));
    flush(rootNode);
  }

  @SuppressWarnings("javadoc")
  private void write(Element topNode, List<MetaData.ColumnInfo> columns, Iterator<Row> itr) {
    int size = 0;
    while (itr.hasNext()) {
      Row r = itr.next();
      Object[] data = r.data();
      Element rowElem = createNode(topNode, ROW_TAG);
      for (int i=0; i < data.length; i++) {
        Element colNode = createNode(rowElem, COLUMN_TAG);
        colNode.setAttribute(LABEL_TAG, columns.get(i).label());
        if (data[i] != null) {
          colNode.setAttribute(JAVA_TYPE_TAG, data[i].getClass().getName());
          colNode.setTextContent(data[i].toString());
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
  private static final String DELTA_TAG = "delta";
  @SuppressWarnings("javadoc")
  private static final String DATA_SET_TAG = "data-set";
  @SuppressWarnings("javadoc")
  private static final String COLUMNS_TAG = "columns";
  @SuppressWarnings("javadoc")
  private static final String BEFORE_TAG = "before";
  @SuppressWarnings("javadoc")
  private static final String AFTER_TAG = "after";
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
  static {
    try {
      XML_DOC_FACTORY = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      XML_TRANSFORMER = TransformerFactory.newInstance().newTransformer();
      XML_TRANSFORMER.setOutputProperty(OutputKeys.INDENT, "yes");
      XML_TRANSFORMER.setOutputProperty(OutputKeys.METHOD, "xml");
      XML_TRANSFORMER.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      XML_TRANSFORMER.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      XML_TRANSFORMER.setOutputProperty(OutputKeys.STANDALONE, "yes");
      XML_TRANSFORMER.setOutputProperty(OutputKeys.VERSION, "1.0");
    } catch (ParserConfigurationException | TransformerConfigurationException
        | TransformerFactoryConfigurationError e) {
      throw new InternalAPIError(e);
    }
  }



  
}
