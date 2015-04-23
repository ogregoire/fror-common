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
import static java.nio.file.Files.isDirectory;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toCollection;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

/**
 *
 * @author Olivier Grégoire &lt;https://github.com/fror&gt;
 */
public final class ResourceLocator {
  
  private static final ImmutableSet<String> supportedProtocols = ImmutableSet.of("file", "jar");
  private final ImmutableSet<ClassPath.ResourceInfo> resources;
  
  private ResourceLocator(ImmutableSet<ClassPath.ResourceInfo> resources) {
    this.resources = resources;
  }
  
  public Stream<URL> locateResource(String uri) {
    checkNotNull(uri);
    return doLocateResource((name) -> uri.equals(name));
  }
  
  public Stream<URL> locateResourceMatching(String pattern) {
    checkNotNull(pattern);
    Pattern p = Pattern.compile(globToRegex(pattern));
    return doLocateResource((name) -> p.matcher(name).matches());
  }
  
  public Stream<URL> locateResource(Predicate<String> namePredicate) {
    checkNotNull(namePredicate);
    return doLocateResource(namePredicate);
  }
  
  private Stream<URL> doLocateResource(Predicate<String> namePredicate) {
    return this.resources.stream()
        .filter(r -> namePredicate.test(r.getResourceName()))
        .map(r -> r.url());
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
        new JarFile(jarSource.toFile());
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
      // TODO rewrite so that ClassPath is not used (because it loads the parents by default).
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
      } catch (NullPointerException e) {
        return false;
      }
    }
  }
  
  private static final CharMatcher REGEX_META = CharMatcher.anyOf(".^$+{[]|()");
  private static final CharMatcher GLOB_META = CharMatcher.anyOf("\\*?{");
  
  private static char nextChar(String pattern, int pos) {
    if (pos < pattern.length()) {
      return pattern.charAt(pos);
    }
    return '\0';
  }
  
  private static String globToRegex(String glob) {
    boolean inGroup = false;
    StringBuilder regex = new StringBuilder("^");
    final int length = glob.length();
    int index = 0;
    while (index < length) {
      char c = glob.charAt(index++);
      switch (c) {
        case '\\':
          if (index == length) {
            throw new PatternSyntaxException("No character to escape", glob, index - 1);
          }
          char next = glob.charAt(index++);
          if (GLOB_META.matches(next) || REGEX_META.matches(next)) {
            regex.append('\\');
          }
          regex.append(next);
          break;
        case '/':
          regex.append(c);
          break;
        case '{':
          if (inGroup) {
            throw new PatternSyntaxException("Cannot nest groups", glob, index - 1);
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
          if (nextChar(glob, index) == '*') {
            regex.append(".*");
            index++;
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
      throw new PatternSyntaxException("Missing '}", glob, index - 1);
    }
    return regex.append('$').toString();
  }
}
