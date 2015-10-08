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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 *
 * @author Olivier Grégoire
 */
public abstract class ByteSink {

  protected ByteSink() {
  }

  public final OutputStream openStream() throws UncheckedIOException {
    try {
      return doOpenStream();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  protected abstract OutputStream doOpenStream() throws IOException;

  public CharSink asCharSink(Charset charset) {
    return new AsCharSink(checkNotNull(charset));
  }

  private class AsCharSink extends CharSink {

    private final Charset charset;

    private AsCharSink(Charset charset) {
      this.charset = charset;
    }

    @Override
    protected Writer doOpenStream() throws IOException {
      return new OutputStreamWriter(ByteSink.this.doOpenStream(), charset);
    }

    @Override
    public String toString() {
      return ByteSink.this.toString() + ".asCharSink(" + charset + ")";
    }

  }
}
