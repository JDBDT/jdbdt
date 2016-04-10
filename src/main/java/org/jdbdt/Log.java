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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * XML logging facility.
 * 
 * <p>
 * A log may be used for test reports and debugging.
 * It is created using {@link JDBDT#log(PrintStream)} or {@link JDBDT#log(File)}.
 * After creation, the available variants of the 
 * {@code write} method may be used to log information
 * for several types of JDBDT objects, e.g., data sets or deltas.
 * The output has an XML format.
 * </p>
 * 
 * <p><b>Illustration</b></p>
 * <blockquote><pre>
 * import static org.jdbdt.JDBDT.*;
 * import org.jdbdt.Log;
 * import org.jdbdt.DataSet;
 * import org.jdbdt.Delta;
 * ...
 * DataSet ds = ...; 
 * log(System.out).write(ds);
 * ...
 * Log flog = log(new File("out.xml"));
 * Delta d = ...;
 * flog.write(d);
 * flog.write(ds);
 * flog.close();
 * </pre></blockquote>
 * 
 * @see JDBDT#log(File)
 * @see JDBDT#log(PrintStream)
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
    Element e = xmlDoc.createElement(ROOT_TAG);
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
      throw new JDBDTInternalError(e);
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
   * @param data Data set.
   */
  public void write(DataSet data) {
    Element rootNode = root(),
            dsNode = xmlDoc.createElement(DATA_TAG),
            tableNode = xmlDoc.createElement(SOURCE_TAG),
            rowsNode = xmlDoc.createElement(ROWS_TAG);
    rootNode.appendChild(dsNode);
    dsNode.appendChild(tableNode);
    dsNode.appendChild(rowsNode);
    write(tableNode, data.getSource().getMetaData());
    write(rowsNode, data.getSource().getMetaData().columns(), data.iterator());
    flush(rootNode);
  }

  @SuppressWarnings("javadoc")
  private void write(Element topNode, MetaData md) {
    int index = 1;
    topNode.setAttribute(COLUMNS_ATTR, String.valueOf(md.getColumnCount()));
    for (MetaData.ColumnInfo col : md.columns()) {
      Element colNode = xmlDoc.createElement(COLUMN_TAG);
      colNode.setAttribute(INDEX_ATTR, String.valueOf(index++));
      colNode.setAttribute(LABEL_ATTR, col.label());
      colNode.setAttribute(TYPE_ATTR, col.type().toString());
      topNode.appendChild(colNode);
    } 
  }
  
  /**
   * Log SQL code.
   * @param sql SQL code.
   */
  public void writeSQL(String sql) {
    Element rootNode = root(),
            sqlNode = xmlDoc.createElement(SQL_TAG);
    rootNode.appendChild(sqlNode);
    sqlNode.appendChild(xmlDoc.createCDATASection("\n" + sql + "\n"));
    flush(rootNode);
  }
  
  /**
   * Write the state of a delta to the log.
   * @param d Delta instance.
   */
  public void write(Delta d) {
    Element rootNode = root(),
            deltaNode = xmlDoc.createElement(DELTA_TAG),
            queryNode = xmlDoc.createElement(SOURCE_TAG),
            bSetNode = xmlDoc.createElement(BEFORE_TAG),
            aSetNode = xmlDoc.createElement(AFTER_TAG);
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
      Element rowElem = xmlDoc.createElement(ROW_TAG);
      for (int i=0; i < data.length; i++) {
        Element colNode = xmlDoc.createElement(COLUMN_TAG);
        colNode.setAttribute(LABEL_ATTR, columns.get(i).label());
        colNode.setTextContent(data[i].toString());
        rowElem.appendChild(colNode);
      }
      topNode.appendChild(rowElem);
      size++;
    }
    topNode.setAttribute(SIZE_ATTR,  String.valueOf(size));
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
  private static final String DATA_TAG = "data-set";
  @SuppressWarnings("javadoc")
  private static final String SIZE_ATTR = "size";
  @SuppressWarnings("javadoc")
  private static final String SOURCE_TAG = "data-source";
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
  private static final String TYPE_ATTR = "type";
  @SuppressWarnings("javadoc")
  private static final String INDEX_ATTR = "index";
  @SuppressWarnings("javadoc")
  private static final String COLUMNS_ATTR = "columns";

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
      throw new JDBDTInternalError(e);
    }
  }



  
}
