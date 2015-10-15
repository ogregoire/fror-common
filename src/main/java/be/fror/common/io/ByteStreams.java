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

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Olivier Grégoire
 */
public final class ByteStreams {

  private static final int BUFFER_SIZE = 8192;

  private ByteStreams() {
  }

  static long copy(InputStream from, OutputStream to) throws IOException {
    byte[] buf = new byte[BUFFER_SIZE];
    long total = 0;
    while (true) {
      int r = from.read(buf);
      if (r == -1) {
        break;
      }
      to.write(buf, 0, r);
      total += r;
    }
    return total;
  }

  static byte[] toByteArray(InputStream in) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    copy(in, out);
    return out.toByteArray();
  }

  public static byte[] readFully(InputStream in, byte[] bytes) throws IOException {
    return readFully(in, bytes, 0, bytes.length);
  }

  public static byte[] readFully(InputStream in, byte[] bytes, int off, int len) throws IOException {
    int read = read(in, bytes, off, len);
    if (read != len) {
      String message = String.format("reached end of stream after reading %d bytes; %d bytes expected", read, len);
      throw new EOFException(message);
    }
    return bytes;
  }

  public static int read(InputStream in, byte[] bytes, int off, int len)
      throws IOException {
    checkNotNull(in);
    checkNotNull(bytes);
    if (len < 0) {
      throw new IndexOutOfBoundsException("len is negative");
    }
    int total = 0;
    while (total < len) {
      int result = in.read(bytes, off + total, len - total);
      if (result == -1) {
        break;
      }
      total += result;
    }
    return total;
  }
}
