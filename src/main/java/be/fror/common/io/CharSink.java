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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;

/**
 *
 * @author Olivier Grégoire
 */
public abstract class CharSink {

  protected CharSink() {
  }

  public final Writer openStream() throws UncheckedIOException {
    try {
      return doOpenStream();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  protected abstract Writer doOpenStream() throws IOException;
}
