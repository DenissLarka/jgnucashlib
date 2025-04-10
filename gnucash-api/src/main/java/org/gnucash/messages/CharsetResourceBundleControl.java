package org.gnucash.messages;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class CharsetResourceBundleControl extends ResourceBundle.Control {
  private final Charset charset;

  public CharsetResourceBundleControl(Charset charset) {
    this.charset = charset;
  }

  @Override
  public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
      throws IllegalAccessException, InstantiationException, IOException {

    String bundleName = toBundleName(baseName, locale);
    String resourceName = toResourceName(bundleName, "properties");

    try (InputStream stream = loader.getResourceAsStream(resourceName);
        InputStreamReader reader = new InputStreamReader(stream, charset)) {
      return new PropertyResourceBundle(reader);
    }
  }
}