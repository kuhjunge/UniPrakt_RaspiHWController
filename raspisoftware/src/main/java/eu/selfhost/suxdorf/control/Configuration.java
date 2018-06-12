/*******************************************************************************
 * Copyright (C) 2017 Chris Deter
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package eu.selfhost.suxdorf.control;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Klasse die die Einstellung der Software von der Festplatte läd und dort
 * wieder speichert.
 *
 * @author Chris Deter
 *
 */
class Configuration {
  private static final String CONF_LOC = "APPDATA";
  private static final String EMPTYSTRING = "";
  private static final String CONFIGFILEENDING = ".conf";
  private static final String CONF_ERR = "Deleted faulty Config!";
  private static final String ERROR_FOLDER = "Failed to create Folder!";
  private static final String SEMICOLON = ";";

  private static final Logger LOG = Logger.getLogger(Configuration.class.getName());
  private File configPath = null;
  private File configFile = null;
  private final Map<String, String> data = new HashMap<>();
  private String softwarename = "";

  /**
   * Konstruktor
   *
   * @param softwarename
   * @param save
   */
  public Configuration(final String softwarename) {
    this.softwarename = softwarename;
    configPath = new File(System.getenv(Configuration.CONF_LOC) + File.separatorChar + softwarename);
  }
  
  /**
   * Konstruktor
   *
   * @param softwarename
   * @param save
   */
  public Configuration(final String softwarename, final String path) {
    this.softwarename = softwarename;
    configPath = new File(path);
  }

  /**
   * gibt den Pfad des Ordners zurück in dem die Config gespeichert wird
   *
   * @return
   */
  public String getConfigPath() {
    return configPath.getAbsolutePath();
  }

  /**
   * Hilfsfunktion, welche Arrays zu dem String "Element 1;Element 2;Element 3"
   * formatiert. Wird benötigt um das Array mit den Typen von Feldern zu
   * speichern.
   *
   * @param input
   *          ein Array mit Werten
   * @return kommagetrennte Array Elemente
   */
  public static String arrayToString(final String[] input) {
    final StringBuilder builder = new StringBuilder();
    for (final String value : input) {
      builder.append(Configuration.SEMICOLON + value.trim());
    }
    builder.deleteCharAt(0);
    return builder.toString();
  }

  /**
   * Wandelt einen String in einen Array um
   *
   * @param input
   *          String
   * @return Array
   */
  public static String[] stringToArray(final String input) {
    final String temp = input.substring(1, input.length() - 2);
    return Arrays.stream(temp.split(Configuration.SEMICOLON)).map(String::trim).toArray(String[]::new);
  }

  /**
   * Läd einen Wert
   *
   * @param key
   *          des Wertes
   * @return der Wert
   */
  public String getValue(final String key) {
    return data.get(key);
  }

  /**
   * initialisiert das Objekt und läd die Config von der Festplatte.
   *
   * @throws Exception
   *           Speichern fehlgeschlagen
   */
  public void init() {
    // ist der Pfad vorhanden
    if (!configPath.exists() && !configPath.mkdirs()) {
      Configuration.LOG.log(Level.SEVERE, Configuration.ERROR_FOLDER);
    }
    // die Config Datei
    configFile = new File(
        configPath.getAbsolutePath() + File.separatorChar + softwarename + Configuration.CONFIGFILEENDING);
    // ist die Config Datei vorhanden - dann lade die daten
    if (configFile.exists()) {
      try (FileInputStream fis = new FileInputStream(configFile)) {
        loadProperties(fis);
      } catch (final IOException e) {
        if (configFile.exists()) {
          Configuration.LOG.log(Level.SEVERE, e.toString(), e);
          // Lösche Config, wenn ein Fehler aufgetreten ist in der Config
          if (configFile.delete()) {
            Configuration.LOG.log(Level.WARNING, Configuration.CONF_ERR, e);
          }
        }
      }
    }
  }

  /**
   * Läd die Werte von der Festplatte.
   *
   * @param fis
   *          der File Input Stream
   * @throws IOException
   *           Wenn ein Fehler beim laden auftritt
   */
  private void loadProperties(final FileInputStream fis) throws IOException {
    final java.util.Properties prop = new java.util.Properties();
    prop.load(fis);
    for (final String k : data.keySet()) {
      data.put(k, prop.getProperty(k, Configuration.EMPTYSTRING));
    }
  }

  /**
   * Speichert die Config File auf die Festplatte.
   */
  public void save() {
    try (FileOutputStream fos = new FileOutputStream(configFile)) {
      saveProperties(fos);
    } catch (final IOException e) {
      Configuration.LOG.log(Level.SEVERE, e.toString(), e);
    }
  }

  /**
   * Speichert die Werte auf der Festplatte.
   *
   * @param fos
   *          der File Output Stream
   * @throws IOException
   *           Wenn ein Fehler beim Speichern auftritt
   */
  private void saveProperties(final FileOutputStream fos) throws IOException {
    final java.util.Properties prop = new java.util.Properties();
    for (final Entry<String, String> k : data.entrySet()) {
      prop.put(k.getKey(), k.getValue());
    }
    prop.store(fos, softwarename);
    fos.flush();
  }

  /**
   * Setzt einen neuen Wert
   *
   * @param key
   *          der Key
   * @param value
   *          der Wert
   */
  public void setValue(final String key, final String value) {
    data.put(key, value);
  }
}