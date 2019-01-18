/*
 * The MIT License
 *
 * Copyright (c) 2016-2019 Eduardo R. B. Marques
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.jdbdt;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;
import java.util.Map.Entry;


/**
 * Delta between data sets.
 * 
 * @since 1.0
 *
 */
final class Delta {
  /**
   * Record of differences. 
   * A map is used to keep row counts because data sets are in fact multi-sets.
   */
  private final LinkedHashMap<Row, Integer> diff = new LinkedHashMap<>();
  
  /**
   * Constructs a new delta.
   * @param ref Reference data set.
   * @param upd Updated data set.
   */
  Delta(DataSet ref, DataSet upd) {
    this(ref.getRows().iterator(), upd.getRows().iterator());
  }
  
  /**
   * Constructs a new delta.
   * @param a Iterator for 1st row set. 
   * @param b Iterator for 2nd row set.
   */
  Delta(Iterator<Row> a, Iterator<Row> b) {
    boolean done = false;
    while (!done) {
      if (!a.hasNext()) {
        while (b.hasNext()) {
          update(b.next(), +1);
        }
        done = true;
      }
      else if (!b.hasNext()) {
        while (a.hasNext()) {
          update(a.next(), -1);
        }
        done = true;
      } 
      else {
        update(a.next(), -1);
        update(b.next(), +1);
      }
    }
  }

  @SuppressWarnings("javadoc")
  private void 
  update(Row r, int d) {
    int n = diff.getOrDefault(r, 0) + d;
    if (n == 0) {
      diff.remove(r);
    }
    else {
      diff.put(r, n);
    } 
  }
  
  /**
   * Check if the delta is empty, i.e., if there were no differences found.
   * @return <code>true</code> if the delta is empty.
   */
  boolean isEmpty() {
    return diff.isEmpty();
  }
  
  /**
   * Get iterator for deleted data.
   * @return Iterator that allows the traversal of deleted rows.
   */
  Iterator<Row> deleted() {
    return new DeltaIterator(diff.entrySet().iterator(), DELETED_FILTER);
  }
  
  /**
   * Get iterator for inserted data.
   * @return Iterator that allows the traversal of deleted rows.
   */
  Iterator<Row> inserted() {
    return new DeltaIterator(diff.entrySet().iterator(), INSERTED_FILTER);
  }
  
  @SuppressWarnings("javadoc")
  @FunctionalInterface
  private interface IteratorFilter {
    int eval(int v);    
  }
  
  @SuppressWarnings("javadoc")
  private static final IteratorFilter DELETED_FILTER = n -> n < 0 ? -n : 0;
 
  @SuppressWarnings("javadoc")
  private static final IteratorFilter INSERTED_FILTER = n -> n > 0 ? n : 0;

  @SuppressWarnings("javadoc")
  private static final
  class DeltaIterator implements Iterator<Row> {
    final Iterator<Entry<Row,Integer>> mapItr;
    final IteratorFilter filter;
    Entry<Row,Integer> nextEntry = null;
    int repeat = 0;

    DeltaIterator(Iterator<Entry<Row,Integer>> itr, IteratorFilter f) {
      mapItr = itr;
      filter = f;
      advance();
    }

    private void advance() {
      if (repeat == 0) {
        nextEntry = null;
        while (mapItr.hasNext()) {
          Entry<Row,Integer> entry = mapItr.next();
          int v = filter.eval(entry.getValue());
          if (v != 0) {
            repeat = v - 1;
            nextEntry = entry; 
            break;
          }
        }
      } else {
        repeat--;
      }
    }

    @Override
    public boolean hasNext() {
      return nextEntry != null;
    }

    @Override
    public Row next() {
      if (nextEntry == null) {
        throw new NoSuchElementException();
      }
      Row r = nextEntry.getKey();
      advance();
      return r;
    }
  }
}
