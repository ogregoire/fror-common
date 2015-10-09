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
package be.fror.common.resource;

import static be.fror.common.base.Preconditions.checkArgument;
import static be.fror.common.base.Preconditions.checkNotNull;
import static be.fror.common.base.Preconditions.checkState;
import static be.fror.common.base.Throwables.propagate;
import static be.fror.common.io.Resources.asByteSource;
import static be.fror.common.io.Resources.asCharSource;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.isDirectory;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.reflect.ClassPath;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.concurrent.ThreadSafe;

/**
 *
 * @author Olivier Grégoire
 */
@ThreadSafe
public final class ResourceLocator {

  // Notes:
  // * http://docs.spring.io/spring/docs/current/spring-framework-reference/html/resources.html
  private static final Set<String> supportedProtocols = Collections.unmodifiableSet(new LinkedHashSet<>(asList("file", "jar")));

  private final ImmutableMultimap<ClassLoader, ClassPath.ResourceInfo> resources;

  private ResourceLocator(ImmutableMultimap<ClassLoader, ClassPath.ResourceInfo> resources) {
    this.resources = resources;
  }

  /**
   * Locates resources matching the glob-pattern {@code namePattern}. Resources are
   * "folder-separated" with {@code /}.
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
   * <b>Note:</b> the resource names don't start with {@code /}.
   *
   * <p>
   * Example of usage:
   *
   * <pre>{@code
   * locateResources("**&#47;*.properties") // Finds any resource whose name ends with ".properties".
   * locateResources("**&#47;*.{java,class}") // Finds any resource whose name ends with ".java" or ".class".
   * locateResources("**&#47;*.???") // Finds any resource whose name ends with a dot then three characters.
   * }</pre>
   *
   * @param namePattern
   * @return
   */
  public Stream<URL> locateResources(String namePattern) {
    checkNotNull(namePattern);
    Pattern p = Pattern.compile(globToRegex(namePattern));
    return doLocateResources((name) -> p.matcher(name).matches());
  }

  public Stream<URL> locateResources(Predicate<String> namePredicate) {
    checkNotNull(namePredicate);
    return doLocateResources(namePredicate);
  }

  /**
   * Returns a stream of resources which names match {@code namePattern} and that are transformed
   * into {@code T}s using {@code loader}.
   *
   * <p>
   * Using a terminal operation on the stream may throw an {@link UncheckedIOException} wrapping an
   * {@link IOException} thrown by {@code loader}.
   *
   * @param <T>
   * @param namePattern
   * @param loader
   * @return
   */
  public <T> Stream<T> loadResources(String namePattern, ResourceLoader<T> loader) {
    return locateResources(namePattern).map(url -> loader.uncheckedLoad(asByteSource(url)));
  }

  /**
   * Returns a stream of resources which names match {@code namePredicate} and that are transformed
   * into {@code T}s using {@code loader}.
   *
   * <p>
   * Using a terminal operation on the stream may throw an {@link UncheckedIOException} wrapping an
   * {@link IOException} thrown by {@code loader}.
   *
   * @param <T>
   * @param namePredicate
   * @param loader
   * @return
   */
  public <T> Stream<T> loadResources(Predicate<String> namePredicate, ResourceLoader<T> loader) {
    return locateResources(namePredicate).map(url -> loader.uncheckedLoad(asByteSource(url)));
  }

  /**
   *
   * @param <T>
   * @param namePattern
   * @param loader
   * @return
   */
  public <T> Stream<Resource<T>> getResources(String namePattern, ResourceLoader<T> loader) {
    return locateResources(namePattern).map(url -> new Resource<>(asByteSource(url), loader));
  }

  /**
   *
   * @param <T>
   * @param namePredicate
   * @param loader
   * @return
   */
  public <T> Stream<Resource<T>> getResources(Predicate<String> namePredicate, ResourceLoader<T> loader) {
    return locateResources(namePredicate).map(url -> new Resource<>(asByteSource(url), loader));
  }

  private Stream<URL> doLocateResources(Predicate<String> namePredicate) {
    return resources.values().stream()
        .filter(r -> namePredicate.test(r.getResourceName()))
        .map(ClassPath.ResourceInfo::url);
  }

  private static final String SERVICE_PREFIX = "META-INF/services/";

  public <T> Stream<Class<? extends T>> getServices(Class<T> service) {
    return getImplementationNames(service.getName()).entrySet().stream()
        .flatMap(e -> toClasses(e, service));
  }

  private Map<ClassLoader, List<String>> getImplementationNames(String serviceName) {
    final String resourceName = SERVICE_PREFIX + serviceName;
    Map<ClassLoader, List<String>> implementationNames = new LinkedHashMap<>();
    resources.asMap().keySet().stream().forEach((classLoader) -> {
      URL url = classLoader.getResource(resourceName);
      if (url != null) {
        implementationNames.computeIfAbsent(classLoader, (cl) -> new ArrayList<>()).addAll(urlToServiceNames(url));
      }
    });
    return implementationNames;
  }

  private static List<String> urlToServiceNames(URL url) {
    // ServiceLoader's doc says that the service provider file is UTF-8-encoded.
    return asCharSource(url, UTF_8).readLines()
        .map(ResourceLocator::removeCommentAndTrim)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toList());
  }

  private static String removeCommentAndTrim(String line) {
    int sharpPos = line.indexOf('#');
    if (sharpPos >= 0) {
      line = line.substring(0, sharpPos);
    }
    return line.trim();
  }

  private static <T> Stream<Class<? extends T>> toClasses(Map.Entry<ClassLoader, List<String>> entry, Class<T> service) {
    return entry.getValue().stream().map(value -> toClass(value, service, entry.getKey()));
  }

  private static <T> Class<? extends T> toClass(String name, Class<T> service, ClassLoader loader) {
    try {
      return (Class<? extends T>) Class.forName(name, false, loader);
    } catch (ClassNotFoundException e) {
      throw propagate(e);
    }
  }

  /**
   *
   */
  public static class Builder {

    private final Set<URLClassLoader> classLoaders = new LinkedHashSet<>();

    public Builder() {
    }

    /**
     *
     * @param classLoader
     * @return
     * @throws IllegalArgumentException
     */
    public Builder addClassLoader(URLClassLoader classLoader) {
      checkNotNull(classLoader, "classLoader must not be null");
      stream(classLoader.getURLs())
          .filter((url) -> supportedProtocols.contains(url.getProtocol()))
          .findFirst()
          .orElseThrow(() -> new IllegalArgumentException("No URL in URLClassLoader are usable"));
      classLoaders.add(classLoader);
      return this;
    }

    /**
     *
     * @param jarSource
     * @return throws IllegalArgumentException
     */
    public Builder addJar(Path jarSource) {
      checkNotNull(jarSource);
      try {
        new JarFile(jarSource.toFile()); // Check that it's a valid jar file.
        URL url = jarSource.toUri().toURL();
        try {
          url = new URL("jar", "", -1, url.toString() + "!/");
        } catch (MalformedURLException unused) {
        }
        classLoaders.add(new URLClassLoader(new URL[]{url}, null));
      } catch (IOException unused) {
        checkArgument(false, "jarSource doesn't refer to a valid jar file");
      }
      return this;
    }

    /**
     *
     * @param directorySource
     * @return
     * @throws IllegalArgumentException
     */
    public Builder addDirectory(Path directorySource) {
      checkNotNull(directorySource);
      checkArgument(isDirectory(directorySource), "directorySource is not a directory");
      try {
        classLoaders.add(new URLClassLoader(new URL[]{directorySource.toUri().toURL()}, null));
      } catch (IOException unused) {
        checkArgument(false, "directorySource cannot be mapped to a URL");
      }
      return this;
    }

    /**
     *
     * @return @throws IOException
     * @throws IllegalStateException
     */
    public ResourceLocator build() throws IOException {
      checkState(!classLoaders.isEmpty(), "At least one source must be added");
      ImmutableMultimap.Builder<ClassLoader, ClassPath.ResourceInfo> builder = ImmutableMultimap.builder();
      for (ClassLoader cl : classLoaders) {
        builder.putAll(cl, ClassPath.from(cl).getResources().stream()
            .filter(Builder::isResourceInfoUrlCorrect)
            .collect(toList()));
      }
      return new ResourceLocator(builder.build());
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
      chars = s.toCharArray();
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
