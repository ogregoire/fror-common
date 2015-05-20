/*
 * Copyright 2015 Olivier Grégoire <https://github.com/ogregoire>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.fror.common.resource;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_16;
import static java.nio.charset.StandardCharsets.UTF_16BE;
import static java.nio.charset.StandardCharsets.UTF_16LE;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.io.ByteSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Tools for {@link ResourceLoader}, including static factory methods.
 *
 * @author Olivier Grégoire &lt;https://github.com/ogregoire&gt;
 */
public final class ResourceLoaders {

  private ResourceLoaders() {
  }

  /**
   * Returns a {code ResourceLoader} able to load {@link Properties} using
   * {@link Properties#load(java.io.InputStream)}
   *
   * @return
   */
  public static ResourceLoader<Properties> propertiesLoader() {
    return InputStreamPropertiesLoader.PROPERTIES;
  }

  /**
   * Returns a {code ResourceLoader} able to load {@link Properties} using
   * {@link Properties#loadFromXML(java.io.InputStream)}
   *
   * @return
   */
  public static ResourceLoader<Properties> xmlPropertiesLoader() {
    return InputStreamPropertiesLoader.XML;
  }

  private static interface PropertiesLoader extends ResourceLoader<Properties> {

    @Override
    public default Properties load(ByteSource source) throws IOException {
      Properties properties = new Properties();
      load(properties, source);
      return properties;
    }

    public void load(Properties properties, ByteSource source) throws IOException;

  }

  private enum InputStreamPropertiesLoader implements PropertiesLoader {

    PROPERTIES {
          @Override
          public void load(Properties properties, ByteSource source) throws IOException {
            try (InputStream in = source.openStream()) {
              properties.load(in);
            }
          }
        },
    XML {
          @Override
          public void load(Properties properties, ByteSource source) throws IOException {
            try (InputStream in = source.openStream()) {
              properties.loadFromXML(in);
            }
          }
        };
  }

  /**
   * Returns a {@code ResourceLoader} able to load {@link Properties} using
   * {@link Properties#load(java.io.Reader)}
   *
   * @param charset the charset to use while loading properties
   * @return
   */
  public static ResourceLoader<Properties> propertiesLoader(Charset charset) {
    return charsetPropertiesLoaders.computeIfAbsent(charset, (c) -> new CharsetPropertiesLoader(c));
  }

  private static final Map<Charset, ResourceLoader<Properties>> charsetPropertiesLoaders;

  static {
    Map<Charset, ResourceLoader<Properties>> map = new HashMap<>();
    for (Charset charset : Arrays.asList(US_ASCII, ISO_8859_1, UTF_8, UTF_16BE, UTF_16LE, UTF_16)) {
      map.put(charset, new CharsetPropertiesLoader(charset));
    }
    charsetPropertiesLoaders = map;
  }

  private static class CharsetPropertiesLoader implements PropertiesLoader {

    private final Charset charset;

    private CharsetPropertiesLoader(Charset charset) {
      this.charset = charset;
    }

    @Override
    public void load(Properties properties, ByteSource source) throws IOException {
      try (Reader reader = source.asCharSource(this.charset).openStream()) {
        properties.load(reader);
      }
    }
  }
}
