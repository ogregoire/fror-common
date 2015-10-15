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
package be.fror.common.primitives;

import static be.fror.common.base.Preconditions.checkArgument;

/**
 *
 * @author Olivier Grégoire
 */
public final class Ints {

  private Ints() {
  }

  public static int fromBigEndianByteArray(byte[] bytes) {
    checkArgument(bytes.length >= Integer.BYTES, "array too small: %s < %s", bytes.length, Integer.BYTES);
    return fromBytes(bytes[0], bytes[1], bytes[2], bytes[3]);
  }
  
  public static int fromLittleEndianByteArray(byte[] bytes) {
    checkArgument(bytes.length >= Integer.BYTES, "array too small: %s < %s", bytes.length, Integer.BYTES);
    return fromBytes(bytes[3], bytes[2], bytes[1], bytes[0]);
  }

  public static int fromBytes(byte b1, byte b2, byte b3, byte b4) {
    return b1 << 24 | (b2 & 0xFF) << 16 | (b3 & 0xFF) << 8 | (b4 & 0xFF);
  }

}
