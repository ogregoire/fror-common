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
import java.nio.CharBuffer;

/**
 *
 * @author Olivier Grégoire
 */
class CharStreams {

  private static final int BUF_SIZE = 0x800; // 2K chars (4K bytes)

  private CharStreams() {
  }

  static long copy(Readable from, Appendable to) throws IOException {
    CharBuffer buf = CharBuffer.allocate(BUF_SIZE);
    long total = 0;
    while (from.read(buf) != -1) {
      buf.flip();
      to.append(buf);
      total += buf.remaining();
      buf.clear();
    }
    return total;
  }

  static String toString(Readable r) throws IOException {
    return toStringBuilder(r).toString();
  }

  private static StringBuilder toStringBuilder(Readable r) throws IOException {
    StringBuilder sb = new StringBuilder();
    copy(r, sb);
    return sb;
  }
}
