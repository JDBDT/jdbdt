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
public final class Log {
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
  public Log(File ouputFile) throws FileNotFoundException {
    this(new PrintStream(new FileOutputStream(ouputFile)), false);
  }
  /**
   * Construct a log with an associated output stream.
   * @param out Output stream.
   */
  public Log(PrintStream out) {
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
    Element e = createNode(ROOT_TAG);
    e.setAttribute(VERSION_ATTR, JDBDT.version());
    e.setAttribute(TIME_ATTR, new Timestamp(System.currentTimeMillis()).toString());
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
  public void write(CallInfo callInfo, DataSet data) {
    Element rootNode = root(),
            dsNode = createNode(DATA_SET_TAG),
            columnsNode = createNode(COLUMNS_TAG),
            rowsNode = createNode(ROWS_TAG);
    writeContext(rootNode, callInfo);
    rootNode.appendChild(dsNode);
    dsNode.appendChild(columnsNode);
    dsNode.appendChild(rowsNode);
    write(columnsNode, data.getSource().getMetaData());
    write(rowsNode, data.getSource().getMetaData().columns(), data.getRows().iterator());
    flush(rootNode);
  }

  @SuppressWarnings("javadoc")
  private void writeContext(Element root, CallInfo info) {
    Element ctxNode = createNode(CONTEXT_TAG);
    if (info.getMessage().length() > 0) {
      simpleNode(ctxNode, CTX_MESSAGE_TAG, info.getMessage());
    }
    writeMethodInfo(ctxNode, CTX_CALLER_TAG, info.getCallerMethodInfo());
    writeMethodInfo(ctxNode, CTX_API_METHOD_TAG, info.getAPIMethodInfo());
    root.appendChild(ctxNode);
  }
  
  @SuppressWarnings("javadoc")
  private void writeMethodInfo
  (Element ctxNode, String tag, MethodInfo mi) {
     Element miNode = createNode(tag);
     simpleNode(miNode, CTX_CLASS_TAG, mi.getClassName());
     simpleNode(miNode, CTX_METHOD_TAG, mi.getMethodName());
     simpleNode(miNode, CTX_FILE_TAG, mi.getFileName());
     simpleNode(miNode, CTX_LINE_TAG, String.valueOf(mi.getLineNumber()));
     ctxNode.appendChild(miNode);
  }
  
  @SuppressWarnings("javadoc")
  private void write(Element topNode, MetaData md) {
    int index = 1;
    topNode.setAttribute(COUNT_ATTR, String.valueOf(md.getColumnCount()));
    for (MetaData.ColumnInfo col : md.columns()) {
      Element colNode = createNode(COLUMN_TAG);
      colNode.setAttribute(INDEX_ATTR, String.valueOf(index++));
      colNode.setAttribute(LABEL_ATTR, col.label());
      colNode.setAttribute(SQL_TYPE_ATTR, col.type().toString());
      topNode.appendChild(colNode);
    } 
  }
  
  /**
   * Log SQL code.
   * @param sql SQL code.
   */
  public void writeSQL(String sql) {
    Element rootNode = root(),
            sqlNode = createNode(SQL_TAG);
    rootNode.appendChild(sqlNode);
    sqlNode.appendChild(xmlDoc.createCDATASection("\n" + sql + "\n"));
    flush(rootNode);
  }
  
  /**
   * Write the state of a delta to the log.
   * @param callInfo Call info.
   * @param d Delta instance.
   */
  public void write(CallInfo callInfo, Delta d) {
    Element rootNode = root(),
            deltaNode = createNode(DELTA_TAG),
            queryNode = createNode(COLUMNS_TAG),
            bSetNode = createNode(BEFORE_TAG),
            aSetNode = createNode(AFTER_TAG);
    writeContext(rootNode, callInfo);
    rootNode.appendChild(deltaNode);
    deltaNode.appendChild(queryNode);
    deltaNode.appendChild(bSetNode);
    deltaNode.appendChild(aSetNode);
    write(queryNode, d.getMetaData());
    deltaNode.setAttribute(SIZE_ATTR, String.valueOf(d.size()));
    write(bSetNode, d.getMetaData().columns(), d.bIterator());
    write(aSetNode, d.getMetaData().columns(), d.aIterator());
    flush(rootNode);
  }

  @SuppressWarnings("javadoc")
  private void write(Element topNode, List<MetaData.ColumnInfo> columns, Iterator<Row> itr) {
    int size = 0;
    while (itr.hasNext()) {
      Row r = itr.next();
      Object[] data = r.data();
      Element rowElem = createNode(ROW_TAG);
      for (int i=0; i < data.length; i++) {
        Element colNode = createNode(COLUMN_TAG);
        colNode.setAttribute(LABEL_ATTR, columns.get(i).label());
        if (data[i] != null) {
          colNode.setAttribute(JAVA_TYPE_ATTR, data[i].getClass().getName());
          colNode.setTextContent(data[i].toString());
        } else {
          colNode.setTextContent(NULL_VALUE);
        }
        rowElem.appendChild(colNode);
      }
      topNode.appendChild(rowElem);
      size++;
    }
    topNode.setAttribute(COUNT_ATTR,  String.valueOf(size));
  }
  
  @SuppressWarnings("javadoc")
  private Element createNode(String tag) {
    return xmlDoc.createElement(tag);
  }

  @SuppressWarnings("javadoc")
  private void simpleNode(Element node, String tag, String value) {
    Element child = createNode(tag);
    child.setTextContent(value);
    node.appendChild(child);
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
  private static final String VERSION_ATTR = "version";
  @SuppressWarnings("javadoc")
  private static final String TIME_ATTR = "time";
  @SuppressWarnings("javadoc")
  private static final String DELTA_TAG = "delta";
  @SuppressWarnings("javadoc")
  private static final String DATA_SET_TAG = "data-set";
  @SuppressWarnings("javadoc")
  private static final String SIZE_ATTR = "size";
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
  private static final String LABEL_ATTR = "label";
  @SuppressWarnings("javadoc")
  private static final String SQL_TYPE_ATTR = "sql-type";
  @SuppressWarnings("javadoc")
  private static final String JAVA_TYPE_ATTR = "java-type";
  @SuppressWarnings("javadoc")
  private static final String INDEX_ATTR = "index";
  @SuppressWarnings("javadoc")
  private static final String COUNT_ATTR = "count";
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
