/**
 * ComplexCurrencyTable.java
 * created: 28.08.2005 15:04:10
 * (c) 2005 by <a href="http://Wolschon.biz">Wolschon Softwaredesign und Beratung</a>
 */
package org.gnucash.currency;

//automatically created logger for debug and error -output

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashFile;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * (c) 2005 by <a href="http://Wolschon.biz>Wolschon Softwaredesign und
 * Beratung</a>.<br/>
 * Project: gnucashReader<br/>
 * ComplexCurrencyTable.java<br/>
 * created: 28.08.2005 15:04:10 <br/>
 * <br/>
 * Currency-Table that can work with multiple namespaces.<br/>
 * By default "ISO4217"-GnucashFile.getDefaultCurrencyID() is added with the
 * value 1. (to be used as a base.currency)
 *
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 * @see GnucashFile#getDefaultCurrencyID()
 */
public class ComplexCurrencyTable extends SimpleCurrencyTable implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 7318173535959109553L;

  public interface ComplexCurrencyTableChangeListener {
    void conversionFactorChanged(final String namespace, final String currency, final FixedPointNumber factor);
  }

  private transient volatile List<ComplexCurrencyTableChangeListener> listeners = null;

  public void addComplexCurrencyTableChangeListener(final ComplexCurrencyTableChangeListener listener) {
    if (listeners == null) {
      listeners = new LinkedList<>();
    }
    listeners.add(listener);
  }

  public void removeComplexCurrencyTableChangeListener(final ComplexCurrencyTableChangeListener listener) {
    if (listeners == null) {
      listeners = new LinkedList<>();
    }
    listeners.remove(listener);
  }

  protected void fireCurrencyTableChanged(final String namespace, final String currency,
      final FixedPointNumber factor) {
    if (listeners != null) {
      for (ComplexCurrencyTableChangeListener listener : listeners) {
        listener.conversionFactorChanged(namespace, currency, factor);
      }
    }
  }

  /**
   * Namespace is e.g. "ISO4217" or "FUND"
   */
  private Map<String, SimpleCurrencyTable> namespace2CurrencyTable;

  // ------------------------ support for propertyChangeListeners
  // ------------------
  //
  /// **
  // * support for firing PropertyChangeEvents
  // * (gets initialized only if we really have listeners)
  // */
  // protected volatile PropertyChangeSupport propertyChange = null;
  //
  /// **
  // * Add a PropertyChangeListener to the listener list.
  // * The listener is registered for all properties.
  // *
  // * @param listener The PropertyChangeListener to be added
  // */
  // public final void addPropertyChangeListener(
  // final PropertyChangeListener listener) {
  // if (propertyChange == null) {
  // propertyChange = new PropertyChangeSupport(this);
  // }
  // propertyChange.addPropertyChangeListener(listener);
  // }
  //
  /// **
  // * Add a PropertyChangeListener for a specific property. The listener
  // * will be invoked only when a call on firePropertyChange names that
  // * specific property.
  // *
  // * @param propertyName The name of the property to listen on.
  // * @param listener The PropertyChangeListener to be added
  // */
  // public final void addPropertyChangeListener(
  // final String propertyName,
  // final PropertyChangeListener listener) {
  // if (propertyChange == null) {
  // propertyChange = new PropertyChangeSupport(this);
  // }
  // propertyChange.addPropertyChangeListener(propertyName, listener);
  // }
  //
  /// **
  // * Remove a PropertyChangeListener for a specific property.
  // *
  // * @param propertyName The name of the property that was listened on.
  // * @param listener The PropertyChangeListener to be removed
  // */
  // public final void removePropertyChangeListener(
  // final String propertyName,
  // final PropertyChangeListener listener) {
  // if (propertyChange != null) {
  // propertyChange.removePropertyChangeListener(propertyName, listener);
  // }
  // }
  //
  /// **
  // * Remove a PropertyChangeListener from the listener list.
  // * This removes a PropertyChangeListener that was registered
  // * for all properties.
  // *
  // * @param listener The PropertyChangeListener to be removed
  // */
  // public synchronized void removePropertyChangeListener(
  // final PropertyChangeListener listener) {
  // if (propertyChange != null) {
  // propertyChange.removePropertyChangeListener(listener);
  // }
  // }
  //
  // -------------------------------------------------------

  /**
   * Just an overridden ToString to return this classe's name and hashCode.
   *
   * @return className and hashCode
   */
  @Override
  public String toString() {
    return "ComplexCurrencyTable@" + hashCode();
  }

  /**
   * Add a new namespace with no conversion-factors.<br/>
   * Will not overwrite an existing namespace.
   *
   * @param namespace the new namespace to add.
   */
  public void addNameSpace(final String namespace) {
    if (getNamespace(namespace) != null) {
      return;
    }

    SimpleCurrencyTable currencyTable = new SimpleCurrencyTable();
    currencyTable.clear();
    addNameSpace(namespace, currencyTable);
  }

  /**
   * Add a new namespace with an initial set of conversion-factors.
   *
   * @param namespace the new namespace to add.
   * @param values    an initial set of conversion-factors.
   */
  public void addNameSpace(final String namespace, final SimpleCurrencyTable values) {
    if (namespace2CurrencyTable == null) {
      namespace2CurrencyTable = new HashMap<String, SimpleCurrencyTable>();
    }
    namespace2CurrencyTable.put(namespace, values);
  }

  /**
   * @see SimpleCurrencyTable#clear()
   */
  @Override
  public void clear() {
    super.clear();
    if (namespace2CurrencyTable == null) {
      namespace2CurrencyTable = new HashMap();
    }
    namespace2CurrencyTable.clear();
  }

  /**
   * @see SimpleCurrencyTable#convertFromBaseCurrency(FixedPointNumber,
   *      java.lang.String)
   */
  @Override
  public boolean convertFromBaseCurrency(FixedPointNumber pValue, String pIso4217CurrencyCode) {
    if (pIso4217CurrencyCode == null) {
      throw new IllegalArgumentException("null currency-id given!");
    }

    return convertFromBaseCurrency("ISO4217", pValue, pIso4217CurrencyCode);
  }

  /**
   * @param namespace e.g. "ISO4217"
   * @see SimpleCurrencyTable#convertFromBaseCurrency(FixedPointNumber, String)
   */
  public boolean convertToBaseCurrency(final String namespace, final FixedPointNumber pValue,
      final String pIso4217CurrencyCode) {

    if (namespace == null) {
      throw new IllegalArgumentException("null namepace given!");
    }
    if (pIso4217CurrencyCode == null) {
      throw new IllegalArgumentException("null currency-id given!");
    }

    SimpleCurrencyTable table = getNamespace(namespace);

    if (table == null) {
      return false;
    }

    return table.convertToBaseCurrency(pValue, pIso4217CurrencyCode);
  }

  /**
   * @param namespace e.g. "ISO4217"
   * @see SimpleCurrencyTable#convertFromBaseCurrency(FixedPointNumber, String)
   */
  public boolean convertFromBaseCurrency(final String namespace, final FixedPointNumber pValue,
      final String pIso4217CurrencyCode) {

    if (namespace == null) {
      throw new IllegalArgumentException("null namepace given!");
    }
    if (pIso4217CurrencyCode == null) {
      throw new IllegalArgumentException("null currency-id given!");
    }

    SimpleCurrencyTable table = getNamespace(namespace);

    if (table == null) {
      return false;
    }

    return table.convertFromBaseCurrency(pValue, pIso4217CurrencyCode);
  }

  /**
   * @see SimpleCurrencyTable#convertToBaseCurrency(FixedPointNumber,
   *      java.lang.String)
   */
  @Override
  public boolean convertToBaseCurrency(final FixedPointNumber pValue, final String pIso4217CurrencyCode) {
    if (pIso4217CurrencyCode == null) {
      throw new IllegalArgumentException("null currency-id given!");
    }
    return convertToBaseCurrency("ISO4217", pValue, pIso4217CurrencyCode);
  }

  /**
   * @see SimpleCurrencyTable#getConversionFactor(java.lang.String)
   */
  @Override
  public FixedPointNumber getConversionFactor(final String pIso4217CurrencyCode) {
    if (pIso4217CurrencyCode == null) {
      throw new IllegalArgumentException("null currency-id given!");
    }
    return getConversionFactor("ISO4217", pIso4217CurrencyCode);
  }

  /**
   * @see SimpleCurrencyTable#setConversionFactor(java.lang.String,
   *      FixedPointNumber)
   */
  @Override
  public void setConversionFactor(final String pIso4217CurrencyCode, final FixedPointNumber pFactor) {

    if (pIso4217CurrencyCode == null) {
      throw new IllegalArgumentException("null currency-id given!");
    }
    if (pFactor == null) {
      throw new IllegalArgumentException("null conversion-factor given!");
    }

    setConversionFactor("ISO4217", pIso4217CurrencyCode, pFactor);

    fireCurrencyTableChanged("ISO4217", pIso4217CurrencyCode, pFactor);
  }

  /**
   * If the namespace does not exist yet, it is created.
   *
   * @see SimpleCurrencyTable#setConversionFactor(java.lang.String,
   *      FixedPointNumber)
   */
  public void setConversionFactor(final String namespace, final String pIso4217CurrencyCode,
      final FixedPointNumber pFactor) {

    if (namespace == null) {
      throw new IllegalArgumentException("null namepace given!");
    }
    if (pIso4217CurrencyCode == null) {
      throw new IllegalArgumentException("null currency-id given!");
    }
    if (pFactor == null) {
      throw new IllegalArgumentException("null conversion-factor given!");
    }

    SimpleCurrencyTable table = getNamespace(namespace);
    if (table == null) {
      addNameSpace(namespace);
      table = getNamespace(namespace);
    }

    table.setConversionFactor(pIso4217CurrencyCode, pFactor);

    fireCurrencyTableChanged(namespace, pIso4217CurrencyCode, pFactor);
  }

  /**
   * @see SimpleCurrencyTable#setConversionFactor(java.lang.String,
   *      FixedPointNumber)
   */
  public FixedPointNumber getConversionFactor(final String namespace, final String pIso4217CurrencyCode) {

    if (pIso4217CurrencyCode == null) {
      throw new IllegalArgumentException("null currency-id given!");
    }

    SimpleCurrencyTable table = getNamespace(namespace);
    if (table == null) {
      return null;
    }

    return table.getConversionFactor(pIso4217CurrencyCode);
  }

  public Collection<String> getNameSpaces() {
    if (namespace2CurrencyTable == null) {
      namespace2CurrencyTable = new HashMap<String, SimpleCurrencyTable>();
    }

    return namespace2CurrencyTable.keySet();
  }

  /**
   * @param namespace
   * @return
   */
  protected SimpleCurrencyTable getNamespace(String namespace) {
    if (namespace == null) {
      throw new IllegalArgumentException("null namepace given!");
    }

    if (namespace2CurrencyTable == null) {
      namespace2CurrencyTable = new HashMap<String, SimpleCurrencyTable>();
    }

    return namespace2CurrencyTable.get(namespace);
  }

  /**
   *
   */
  public ComplexCurrencyTable() {
    super();

    addNameSpace("ISO4217", new SimpleCurrencyTable());
  }

  /**
   * @param pNamespace
   */
  public Collection<String> getCurrencies(final String pNamespace) {
    SimpleCurrencyTable namespace = getNamespace(pNamespace);
    if (namespace == null) {
      return new HashSet<String>();
    }
    return namespace.getCurrencies();
  }
}