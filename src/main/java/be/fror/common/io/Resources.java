/*
 * Copyright 2015 Olivier Grégoire.
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
package be.fror.common.io;

import static be.fror.common.base.Preconditions.checkArgument;
import static be.fror.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Optional;

/**
 *
 * @author Olivier Grégoire
 */
public final class Resources {

  private Resources() {
  }

  public static ByteSource asByteSource(URL url) {
    return new UrlByteSource(checkNotNull(url));
  }

  private static final class UrlByteSource extends ByteSource {

    private final URL url;

    private UrlByteSource(URL url) {
      this.url = url;
    }

    @Override
    public InputStream doOpenStream() throws IOException {
      return url.openStream();
    }

    @Override
    public String toString() {
      return "Resources.asByteSource(" + url + ")";
    }
  }

  public static CharSource asCharSource(URL url, Charset charset) {
    return asByteSource(url)
        .asCharSource(charset);
  }

  public static URL getResource(String resourceName) {
    ClassLoader loader = Optional
        .ofNullable(Thread.currentThread().getContextClassLoader())
        .orElseGet(Resources.class::getClassLoader);
    URL url = loader.getResource(resourceName);
    checkArgument(url != null, "resource %s not found.", resourceName);
    return url;
  }

  public static URL getResource(Class<?> contextClass, String resourceName) {
    URL url = contextClass.getResource(resourceName);
    checkArgument(url != null, "resource %s relative to %s not found.", resourceName, contextClass.getName());
    return url;
  }
}
