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
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;

/**
 *
 * @author Olivier Grégoire
 */
public abstract class CharSource {

  protected CharSource() {
  }

  public final Reader openStream() throws UncheckedIOException {
    try {
      return doOpenStream();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  protected abstract Reader doOpenStream() throws IOException;

  public long copyTo(CharSink sink) throws UncheckedIOException {
    try (Reader in = doOpenStream();
        Writer out = sink.doOpenStream()) {
      return CharStreams.copy(in, out);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public String read() throws UncheckedIOException {
    try (Reader in = doOpenStream()) {
      return CharStreams.toString(in);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static CharSource wrap(CharSequence charSequence) {
    return new CharSequenceCharSource(checkNotNull(charSequence));
  }

  private static class CharSequenceCharSource extends CharSource {

    private final CharSequence seq;

    private CharSequenceCharSource(CharSequence seq) {
      this.seq = seq;
    }

    @Override
    protected Reader doOpenStream() throws IOException {
      return new CharSequenceReader(seq);
    }

    @Override
    public String toString() {
      return "CharSource.wrap(" + "seq" + ")";
    }

  }

  public static CharSource empty() {
    return EmptyCharSource.INSTANCE;
  }

  private static final class EmptyCharSource extends CharSequenceCharSource {

    private static final EmptyCharSource INSTANCE = new EmptyCharSource();

    private EmptyCharSource() {
      super("");
    }

    @Override
    public String toString() {
      return "CharSource.empty()";
    }
  }

}
