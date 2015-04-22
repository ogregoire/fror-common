/*
 * Copyright 2015 Olivier Grégoire <https://github.com/fror>.
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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 *
 * @author Olivier Grégoire &lt;https://github.com/fror&gt;
 */
public class ResourceLocator {

  private final ImmutableSet<URL> urls;

  private ResourceLocator(Builder builder) {
    this.urls = ImmutableSet.copyOf(builder.urls);
  }

  public Stream<URL> locateResource(String uri) {
    checkNotNull(uri);
    return doLocateResource((name) -> uri.equals(name));
  }

  public Stream<URL> locateResourceMatching(String pattern) {
    checkNotNull(pattern);
    Pattern p = Pattern.compile(Glob.toRegexPattern(pattern));
    return doLocateResource((name) -> p.matcher(name).matches());
  }

  public Stream<URL> locateResource(Predicate<String> namePredicate) {
    checkNotNull(namePredicate);
    return doLocateResource(namePredicate);
  }

  private Stream<URL> doLocateResource(Predicate<String> namePredicate) {
    Iterator<URL> iterator = new ResourceLocatorIterator(namePredicate);
    return stream(spliteratorUnknownSize(iterator, ORDERED), false);
  }

  private class ResourceLocatorIterator extends AbstractIterator<URL> {

    private final Predicate<String> predicate;

    private ResourceLocatorIterator(Predicate<String> predicate) {
      this.predicate = predicate;
    }

    @Override
    protected URL computeNext() {
      return null;
    }
  }

  public static class Builder {

    // Order matters? Let's assume yes.
    private final Set<URL> urls = new LinkedHashSet<>();

    public Builder() {
    }

    public Builder addClassLoader(URLClassLoader classLoader) {
      checkNotNull(classLoader, "classLoader must not be null");
      Set<URL> localUrls = new LinkedHashSet<>();
      for (URL url : classLoader.getURLs()) {
        String protocol = url.getProtocol();
        if ("jar".equals(protocol) || "file".equals(protocol)) {
          localUrls.add(url);
        }
      }
      checkArgument(!localUrls.isEmpty(), "No URL in URLClassLoader are usable");
      this.urls.addAll(localUrls);
      return this;
    }

    public Builder addJar(Path jarSource) {
      checkNotNull(jarSource);
      try {
        new JarFile(jarSource.toFile());
        URL url = jarSource.toUri().toURL();
        try {
          url = new URL("jar", "", -1, url.toString() + "!/");
        } catch (MalformedURLException e) {
        }
        this.urls.add(url);
      } catch (IOException unused) {
        checkArgument(false, "jarSource doesn't refer to a valid jar file");
      }
      return this;
    }

    public Builder addDirectory(Path directorySource) {
      checkNotNull(directorySource);
      checkArgument(Files.isDirectory(directorySource), "directorySource is not a directory");
      try {
        this.urls.add(directorySource.toUri().toURL());
      } catch (IOException unused) {
        checkArgument(false, "directorySource cannot be mapped to a URL");
      }
      return this;
    }

    public ResourceLocator build() {
      return new ResourceLocator(this);
    }
  }
}
