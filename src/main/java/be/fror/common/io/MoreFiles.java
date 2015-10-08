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

import static be.fror.common.base.Preconditions.checkNotNull;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import be.fror.common.base.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.List;

/**
 *
 * @author Olivier Grégoire
 */
public final class MoreFiles {

  private MoreFiles() {
  }

  public static ByteSource asByteSource(Path path, OpenOption... options) {
    return new PathByteSource(
        checkNotNull(path),
        stream(checkNotNull(options))
          .map(Preconditions::checkNotNull)
          .collect(toList())
    );
  }

  private static class PathByteSource extends ByteSource {

    private final Path path;
    private final List<OpenOption> options;

    private PathByteSource(Path path, List<OpenOption> options) {
      this.path = path;
      this.options = options;
    }

    @Override
    protected InputStream doOpenStream() throws IOException {
      return newInputStream(
          path,
          options.toArray(new OpenOption[options.size()])
      );
    }

    @Override
    public String toString() {
      return "MoreFiles.asByteSource(" + path + ")";
    }
  }

  public static ByteSink asByteSink(Path path, OpenOption... options) {
    return new PathByteSink(
        checkNotNull(path),
        stream(checkNotNull(options))
          .map(Preconditions::checkNotNull)
          .collect(toList())
    );
  }

  private static class PathByteSink extends ByteSink {

    private final Path path;
    private final List<OpenOption> options;

    public PathByteSink(Path path, List<OpenOption> options) {
      this.path = path;
      this.options = options;
    }

    @Override
    protected OutputStream doOpenStream() throws IOException {
      return newOutputStream(
          path,
          options.toArray(new OpenOption[options.size()])
      );
    }

    @Override
    public String toString() {
      return "MoreFiles.asByteSink(" + path + ", " + options + ")";
    }

  }

  public static CharSource asCharSource(Path path, Charset charset, OpenOption... options) {
    return asByteSource(path, options).asCharSource(charset);
  }

  public static CharSink asCharSink(Path path, Charset charset, OpenOption... options) {
    return asByteSink(path, options).asCharSink(charset);
  }
}
