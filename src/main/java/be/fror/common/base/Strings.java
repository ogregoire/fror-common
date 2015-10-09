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
package be.fror.common.base;

import static be.fror.common.base.Preconditions.checkArgument;
import static be.fror.common.base.Preconditions.checkNotNull;

import java.text.Normalizer;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

/**
 * Tools for String that are not present in Guava.
 *
 * @author Olivier Grégoire
 */
public final class Strings {

  private Strings() {
  }

  public static String nullToEmpty(@Nullable String string) {
    return string == null ? "" : string;
  }

  @Nullable
  public static String emptyToNull(@Nullable String string) {
    return isNullOrEmpty(string) ? null : string;
  }

  public static boolean isNullOrEmpty(@Nullable String string) {
    return string == null || string.isEmpty();
  }

  public static String repeat(String string, int count) {
    checkNotNull(string);
    if (count <= 1) {
      checkArgument(count >= 0, "Invalid count: %s", count);
      return count == 0 ? "" : string;
    }
    final int len = string.length();
    final long longSize = (long) len * (long) count;
    final int size = (int) longSize;
    if (size != longSize) {
      throw new ArrayIndexOutOfBoundsException("Required array size too large: " + longSize);
    }
    final char[] array = new char[size];
    string.getChars(0, len, array, 0);
    int n;
    for (n = len; n < size - n; n <<= 1) {
      System.arraycopy(array, 0, array, n, n);
    }
    System.arraycopy(array, 0, array, n, size - n);
    return new String(array);
  }

  private static final Pattern MULTIPLE_IN_COMBINING_DIACRITICAL_MARKS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

  /**
   * Removes diacritical marks from strings.
   *
   * <p>
   * Examples:
   * <pre>
   * <code>String cote = MoreStrings.removeDiacriticalMarks("côté"); // returns "cote"
   * String espana = MoreStrings.removeDiacriticalMarks("España"); // returns "espana"
   * String muller = MoreStrings.removeDiacriticalMarks("Müller"); // returns "muller", not "mueller"
   * </code>
   * </pre>
   *
   * @param str
   * @return
   */
  public static String removeDiacriticalMarks(String str) {
    checkNotNull(str, "str must not be null");
    String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFKD);
    return MULTIPLE_IN_COMBINING_DIACRITICAL_MARKS_PATTERN.matcher(nfdNormalizedString).replaceAll("");
  }
}
