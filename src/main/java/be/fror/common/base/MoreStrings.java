/*
 * Copyright 2015 Olivier Grégoire <https://github.com/ogregoire>.
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

import static com.google.common.base.Preconditions.checkNotNull;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Tools for String that are not present in Guava.
 *
 * @author Olivier Grégoire &lt;https://github.com/ogregoire&gt;
 */
public final class MoreStrings {

  private MoreStrings() {
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
