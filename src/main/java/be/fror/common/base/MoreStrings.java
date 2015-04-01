/*
 * Copyright 2015 Olivier Grégoire <https://github.com/fror>.
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

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 *
 * @author Olivier Grégoire &lt;https://github.com/fror&gt;
 */
public class MoreStrings {

  private MoreStrings() {
  }

  private static final Pattern MULTIPLE_IN_COMBINING_DIACRITICAL_MARKS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

  /**
   *
   *
   * @param str
   * @return
   */
  public static String removeDiacriticalMarks(String str) {
    if (str == null) {
      throw new NullPointerException("str must not be null");
    }
    String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFKD);
    return MULTIPLE_IN_COMBINING_DIACRITICAL_MARKS_PATTERN.matcher(nfdNormalizedString).replaceAll("");
  }
}
