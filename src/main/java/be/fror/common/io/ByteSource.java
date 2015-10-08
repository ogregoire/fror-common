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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;

/**
 *
 * @author Olivier Grégoire
 */
public abstract class ByteSource {

  protected ByteSource() {
  }

  public final InputStream openStream() throws UncheckedIOException {
    try {
      return doOpenStream();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  protected abstract InputStream doOpenStream() throws IOException;

  public CharSource asCharSource(Charset charset) {
    return new AsCharSource(checkNotNull(charset));
  }

  private class AsCharSource extends CharSource {

    private final Charset charset;

    private AsCharSource(Charset charset) {
      this.charset = charset;
    }

    @Override
    protected Reader doOpenStream() throws IOException {
      return new InputStreamReader(ByteSource.this.doOpenStream(), charset);
    }

    @Override
    public String toString() {
      return ByteSource.this.toString() + ".asCharSource(" + charset + ")";
    }

  }

  public long copyTo(ByteSink sink) throws UncheckedIOException {
    try (InputStream in = doOpenStream();
        OutputStream out = sink.doOpenStream()) {
      return ByteStreams.copy(in, out);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

  }

  public byte[] read() throws UncheckedIOException {
    try (InputStream in = doOpenStream()) {
      return ByteStreams.toByteArray(in);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static ByteSource wrap(byte[] bytes) {
    return new ByteArrayByteSource(checkNotNull(bytes));
  }

  private static class ByteArrayByteSource extends ByteSource {

    private final byte[] bytes;

    public ByteArrayByteSource(byte[] bytes) {
      this.bytes = bytes;
    }

    @Override
    protected InputStream doOpenStream() throws IOException {
      return new ByteArrayInputStream(bytes);
    }

    @Override
    public String toString() {
      return "ByteSource.wrap(" + "bytes" + ")";
    }

  }

  public static ByteSource empty() {
    return EmptyByteSource.INSTANCE;
  }

  private static final class EmptyByteSource extends ByteArrayByteSource {

    private static final EmptyByteSource INSTANCE = new EmptyByteSource();

    private EmptyByteSource() {
      super(new byte[0]);
    }

    @Override
    public String toString() {
      return "ByteSource.empty()";
    }
  }

}
