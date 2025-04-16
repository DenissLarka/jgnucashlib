package org.gnucash.read.impl;

//other imports

//automatically created logger for debug and error -output

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.gnucash.generated.ObjectFactory;
import org.gnucash.generated.Slot;
import org.gnucash.generated.SlotValue;
import org.gnucash.generated.SlotsType;
import org.gnucash.messages.ApplicationMessages;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper-Class used to implement functions all gnucash-objects support.
 */
public class GnucashObjectImpl implements GnucashObject {

  private static final Logger LOGGER = LoggerFactory.getLogger(GnucashObjectImpl.class);
  private static ApplicationMessages bundle;

  /**
   * the user-defined values.
   */
  private SlotsType mySlots;

  /**
   * The file we belong to.
   */
  private final GnucashFile myFile;

  // -----------------------------------------------------------------

  public GnucashObjectImpl(final GnucashFile myFile) {
    super();

    this.myFile = myFile;
  }

  /**
   * @param slots    ${@link #mySlots}
   * @param gcshFile The file we belong to
   */
  @SuppressWarnings("exports")
  public GnucashObjectImpl(final SlotsType slots, final GnucashFile gcshFile) {
    super();

    this.myFile = gcshFile;
    setSlots(slots);
    // System.err.println("Slots:");
    // for ( Slot slot : getSlots().getSlot() )
    // System.err.println(" - " + slot);
  }

  // -----------------------------------------------------------------

  /**
   * @return Returns the slots.
   * @link #mySlots
   */
  @SuppressWarnings("exports")
  public SlotsType getSlots() {
    return mySlots;
  }

  /**
   * @param slots The slots to set.
   * @link #mySlots
   */
  @SuppressWarnings("exports")
  public void setSlots(final SlotsType slots) {
    if (slots == null) {
      throw new IllegalArgumentException("null 'slots' given!");
    }

    SlotsType oldSlots = mySlots;
    if (oldSlots == slots) {
      return; // nothing has changed
    }
    // ::TODO Check with equals as well
    mySlots = slots;

    // we have an xsd-problem saving empty slots so we add a dummy-value
    if (slots.getSlot().isEmpty()) {
      ObjectFactory objectFactory = new ObjectFactory();

      SlotValue value = objectFactory.createSlotValue();
      value.setType("string");
      value.getContent().add("dummy");

      Slot slot = objectFactory.createSlot();
      slot.setSlotKey("dummy");
      slot.setSlotValue(value);

      slots.getSlot().add(slot);
    }

    // <<insert code to react further to this change here
    PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
    if (propertyChangeFirer != null) {
      propertyChangeFirer.firePropertyChange("slots", oldSlots, slots);
    }
  }

  /**
   * @return Returns the file.
   * @link #myFile
   */
  public GnucashFile getGnucashFile() {
    return myFile;
  }

  // public void setGnucashFile(GnucashFile gcshFile) {
  // this.myFile = gcshFile;
  // }

  // -----------------------------------------------------------------

  /**
   * @return all keys that can be used with ${@link #getUserDefinedAttribute(String)}}.
   */
  public Collection<String> getUserDefinedAttributeKeys() {
    List<Slot> slots = getSlots().getSlot();
    List<String> retval = new ArrayList<String>(slots.size());

    for (Slot slot : slots) {
      retval.add(slot.getSlotKey());
    }

    return retval;
  }

  /**
   * @param name the name of the user-defined attribute
   * @return the value or null if not set
   */
  public String getUserDefinedAttribute(final String name) {

    List<Slot> slots = getSlots().getSlot();
    for (Slot slot : slots) {
      if (slot.getSlotKey().equals(name)) {
        Object value = slot.getSlotValue().getContent().get(0);
        if (value == null) {
          return null;
        }
        if (!(value instanceof String)) {
          LOGGER.error(bundle.getMessage("Err_UsrDefAttWrgTyp", name, value.getClass().getName()));
        }
        return value.toString();
      }
    }

    return null;
  }

  // ------------------------ support for propertyChangeListeners
  // ------------------

  /**
   * support for firing PropertyChangeEvents. (gets initialized only if we really have listeners)
   */
  private volatile PropertyChangeSupport myPropertyChange = null;

  /**
   * Returned value may be null if we never had listeners.
   *
   * @return Our support for firing PropertyChangeEvents
   */
  protected PropertyChangeSupport getPropertyChangeSupport() {
    return myPropertyChange;
  }

  /**
   * Add a PropertyChangeListener to the listener list. The listener is registered for all properties.
   *
   * @param listener The PropertyChangeListener to be added
   */
  @SuppressWarnings("exports")
  public final void addPropertyChangeListener(final PropertyChangeListener listener) {
    if (myPropertyChange == null) {
      myPropertyChange = new PropertyChangeSupport(this);
    }
    myPropertyChange.addPropertyChangeListener(listener);
  }

  /**
   * Add a PropertyChangeListener for a specific property. The listener will be invoked only when a call on
   * firePropertyChange names that specific property.
   *
   * @param propertyName The name of the property to listen on.
   * @param listener     The PropertyChangeListener to be added
   */
  @SuppressWarnings("exports")
  public final void addPropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
    if (myPropertyChange == null) {
      myPropertyChange = new PropertyChangeSupport(this);
    }
    myPropertyChange.addPropertyChangeListener(propertyName, listener);
  }

  /**
   * Remove a PropertyChangeListener for a specific property.
   *
   * @param propertyName The name of the property that was listened on.
   * @param listener     The PropertyChangeListener to be removed
   */
  @SuppressWarnings("exports")
  public final void removePropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
    if (myPropertyChange != null) {
      myPropertyChange.removePropertyChangeListener(propertyName, listener);
    }
  }

  /**
   * Remove a PropertyChangeListener from the listener list. This removes a PropertyChangeListener that was registered
   * for all properties.
   *
   * @param listener The PropertyChangeListener to be removed
   */
  @SuppressWarnings("exports")
  public synchronized void removePropertyChangeListener(final PropertyChangeListener listener) {
    if (myPropertyChange != null) {
      myPropertyChange.removePropertyChangeListener(listener);
    }
  }

  // -------------------------------------------------------

  /**
   * Just an overridden ToString to return this classe's name and hashCode.
   *
   * @return className and hashCode
   */
  @Override
  public String toString() {
    return "GnucashObjectImpl@" + hashCode();
  }

}
