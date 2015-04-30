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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.io.Resources.asByteSource;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.isDirectory;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toCollection;

import com.google.common.base.CharMatcher;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import com.google.common.reflect.ClassPath;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

/**
 *
 * @author Olivier Grégoire &lt;https://github.com/ogregoire&gt;
 */
public final class ResourceLocator {

  private static final ImmutableSet<String> supportedProtocols = ImmutableSet.of("file", "jar");
  private final ImmutableSet<ClassPath.ResourceInfo> resources;

  private ResourceLocator(ImmutableSet<ClassPath.ResourceInfo> resources) {
    this.resources = resources;
  }

  /**
   * Locates resources matching the glob-pattern <tt>pattern</tt>. Resources are "folder-separated"
   * with <tt>/</tt>.
   *
   * <p>
   * Pattern definition:
   * <ul>
   * <li>Follow the rules of {@link FileSystem#getPathMatcher(java.lang.String) }</li>
   * <li>remove the "glob:" prefix</li>
   * <li>regex is not supported</li>
   * <li>brackets expressions are not supported</li>
   * </ul>
   *
   * <p>
   * <b>Note:</b> the resource names don't start with a <tt>'/'</tt>.
   *
   * <p>
   * Example of usage:
   *
   * <pre>
   * <code>locateResources("**&#47;*.properties") // Finds any resource whose name ends with ".properties".
   * locateResources("**&#47;*.{java,class}") // Finds any resource whose name ends with ".java" or ".class".
   * locateResources("**&#47;*.???") // Finds any resource whose name ends with a dot then three characters.
   * </code>
   * </pre>
   *
   * @param pattern
   * @return
   */
  public Stream<URL> locateResources(String pattern) {
    checkNotNull(pattern);
    Pattern p = Pattern.compile(globToRegex(pattern));
    return doLocateResources((name) -> p.matcher(name).matches());
  }

  public Stream<URL> locateResources(Predicate<String> namePredicate) {
    checkNotNull(namePredicate);
    return doLocateResources(namePredicate);
  }

  private Stream<URL> doLocateResources(Predicate<String> namePredicate) {
    return this.resources.stream()
        .filter(r -> namePredicate.test(r.getResourceName()))
        .map(r -> r.url());
  }

  private static final String SERVICE_PREFIX = "META-INF/services/";

  /* public */ <T> Stream<Class<? extends T>> getServices(Class<T> service) {
    if (1 + 1 != 2) { // Let's hope that no cosmic ray will impact this.
      throw new UnsupportedOperationException("Not implemented yet");
    }
    String serviceResourceName = SERVICE_PREFIX + service.getName();
    // TODO find a way to work similarly to ServiceLoader.

    ImmutableSet<String> serviceNames = ImmutableSet.copyOf(
        this.resources.stream()
        .filter(ri -> serviceResourceName.equals(ri.getResourceName()))
        .flatMap(ResourceLocator::urlToServiceNames)
        .map(s -> s.replace('.', '/') + ".class")
        .iterator()
    );
    if (serviceNames.isEmpty()) {
      return Stream.empty();
    }
    
    return this.resources.stream()
        .filter(ri -> serviceNames.contains(ri.getResourceName())
            && ri instanceof ClassPath.ClassInfo)
        .map(ri -> classInfoToClass((ClassPath.ClassInfo) ri, service));
  }

  private static Stream<String> urlToServiceNames(ClassPath.ResourceInfo ri) {
    try {
      return Resources.asCharSource(ri.url(), UTF_8)
          .readLines().stream()
          .filter(s -> !s.isEmpty());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static <T> Class<? extends T> classInfoToClass(ClassPath.ClassInfo ci, Class<T> service) {
    // TODO: Which ClassLoader to use?
    // - Thread.currentThread().getContextClassLoader()
    // - ClassLoader.getSystemClassLoader()
    // - A specific one given as parameter
    // - service's ClassLoader
    // - The ClassLoaders potentially passed in the builder
    // - Create a new one with ClassLoader.getSystemClassLoader() as parent
    // I'm lost...
    try {
      return (Class<? extends T>) Class.forName(ci.getName(), false, Thread.currentThread().getContextClassLoader());
    } catch (ClassNotFoundException ex) {
      throw Throwables.propagate(ex);
    }
  }

  /**
   * Returns a stream of resources which names match <tt>pattern</tt> and that are transformed into
   * <tt>T</tt>s using <tt>loader</tt>.
   *
   * <p>
   * Using a terminal operation on the stream may throw an {@link UncheckedIOException} wrapping an
   * {@link IOException} thrown by <tt>loader</tt>.
   *
   * @param <T>
   * @param pattern
   * @param loader
   * @return
   */
  public <T> Stream<T> getResources(String pattern, ResourceLoader<T> loader) {
    return locateResources(pattern).map(url -> loader.uncheckedLoad(asByteSource(url)));
  }

  /**
   * Returns a stream of resources which names match <tt>namePredicate</tt> and that are transformed
   * into <tt>T</tt>s using <tt>loader</tt>.
   *
   * <p>
   * Using a terminal operation on the stream may throw an {@link UncheckedIOException} wrapping an
   * {@link IOException} thrown by <tt>loader</tt>.
   *
   * @param <T>
   * @param namePredicate
   * @param loader
   * @return
   */
  public <T> Stream<T> getResources(Predicate<String> namePredicate, ResourceLoader<T> loader) {
    return locateResources(namePredicate).map(url -> loader.uncheckedLoad(asByteSource(url)));
  }

  public static class Builder {

    // Order matters? Let's assume yes.
    private final Set<URL> urls = new LinkedHashSet<>();

    public Builder() {
    }

    public Builder addClassLoader(URLClassLoader classLoader) {
      checkNotNull(classLoader, "classLoader must not be null");
      Set<URL> localUrls = asList(classLoader.getURLs()).stream()
          .filter((url) -> supportedProtocols.contains(url.getProtocol()))
          .collect(toCollection(LinkedHashSet::new));
      checkArgument(!localUrls.isEmpty(), "No URL in URLClassLoader are usable");
      this.urls.addAll(localUrls);
      return this;
    }

    public Builder addJar(Path jarSource) {
      checkNotNull(jarSource);
      try {
        new JarFile(jarSource.toFile()); // Check that it's a valid jar file.
        URL url = jarSource.toUri().toURL();
        try {
          url = new URL("jar", "", -1, url.toString() + "!/");
        } catch (MalformedURLException unused) {
        }
        this.urls.add(url);
      } catch (IOException unused) {
        checkArgument(false, "jarSource doesn't refer to a valid jar file");
      }
      return this;
    }

    public Builder addDirectory(Path directorySource) {
      checkNotNull(directorySource);
      checkArgument(isDirectory(directorySource), "directorySource is not a directory");
      try {
        this.urls.add(directorySource.toUri().toURL());
      } catch (IOException unused) {
        checkArgument(false, "directorySource cannot be mapped to a URL");
      }
      return this;
    }

    public ResourceLocator build() throws IOException {
      checkState(!this.urls.isEmpty(), "At least one source must be added");
      URL[] urlArray = this.urls.toArray(new URL[this.urls.size()]);
      URLClassLoader classLoader = new URLClassLoader(urlArray, null);
      ImmutableSet<ClassPath.ResourceInfo> resources = ImmutableSet.copyOf(
          ClassPath.from(classLoader).getResources().stream()
          .filter(ri -> isResourceInfoUrlCorrect(ri))
          .iterator()
      );
      return new ResourceLocator(resources);
    }

    private static boolean isResourceInfoUrlCorrect(ClassPath.ResourceInfo ri) {
      try {
        ri.url();
        return true;
      } catch (NullPointerException | NoSuchElementException e) {
        return false;
      }
    }
  }

  private static final CharMatcher REGEX_META = CharMatcher.anyOf(".^$+{[]|()");
  private static final CharMatcher GLOB_META = CharMatcher.anyOf("\\*?{");

  static String globToRegex(final String glob) {

    CharIterator chars = new CharIterator(glob);
    boolean inGroup = false;
    StringBuilder regex = new StringBuilder("^");

    while (chars.hasNext()) {
      char c = chars.next();
      switch (c) {
        case '\\':
          if (!chars.hasNext()) {
            throw new PatternSyntaxException("No character to escape", glob, chars.index - 1);
          }
          char next = chars.next();
          if (GLOB_META.matches(next) || REGEX_META.matches(next)) {
            regex.append('\\');
          }
          regex.append(next);
          break;
        case '/':
          regex.append('/');
          break;
        case '{':
          if (inGroup) {
            throw new PatternSyntaxException("Cannot nest groups", glob, chars.index - 1);
          }
          regex.append("(?:(?:");
          inGroup = true;
          break;
        case '}':
          if (inGroup) {
            regex.append("))");
            inGroup = false;
          } else {
            regex.append('}');
          }
          break;
        case ',':
          if (inGroup) {
            regex.append(")|(?:");
          } else {
            regex.append(',');
          }
          break;
        case '*':
          if (chars.peekNext() == '*') {
            regex.append(".*");
            chars.next();
          } else {
            regex.append("[^/]*");
          }
          break;
        case '?':
          regex.append("[^/]");
          break;
        default:
          if (REGEX_META.matches(c)) {
            regex.append('\\');
          }
          regex.append(c);
      }
    }
    if (inGroup) {
      throw new PatternSyntaxException("Missing '}", glob, chars.index - 1);
    }
    return regex.append('$').toString();
  }

  private static class CharIterator {

    char[] chars;
    int index = 0;

    CharIterator(String s) {
      this.chars = s.toCharArray();
    }

    boolean hasNext() {
      return index < chars.length;
    }

    char next() {
      return chars[index++];
    }

    char peekNext() {
      return index + 1 < chars.length ? chars[index] : (char) 0xFFFF;
    }
  }
}
