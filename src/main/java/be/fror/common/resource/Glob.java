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
package be.fror.common.resource;

import com.google.common.base.CharMatcher;

import java.util.regex.PatternSyntaxException;

/**
 *
 * @author Olivier Grégoire <https://github.com/fror>
 */
class Glob {

  private static final CharMatcher REGEX_META = CharMatcher.anyOf(".^$+{[]|()");
  private static final CharMatcher GLOB_META = CharMatcher.anyOf("\\*?[{");

  private static final char EOL = '\0';

  private static char next(String pattern, int pos) {
    if (pos < pattern.length()) {
      return pattern.charAt(pos);
    }
    return EOL;
  }

  static String toRegexPattern(String glob) {
    boolean inGroup = false;
    StringBuilder regex = new StringBuilder("^");
    final int length = glob.length();
    int index = 0;
    while (index < length) {
      char c = glob.charAt(index++);
      switch (c) {
        case '\\':
          if (index == length) {
            throw new PatternSyntaxException("No character to escape", glob, index - 1);
          }
          char next = glob.charAt(index++);
          if (GLOB_META.matches(next) || REGEX_META.matches(next)) {
            regex.append('\\');
          }
          regex.append(next);
          break;
        case '/':
          regex.append(c);
          break;
        case '{':
          if (inGroup) {
            throw new PatternSyntaxException("Cannot nest groups", glob, index - 1);
          }
          regex.append("(?:(?:");
          inGroup = true;
          break;
        case '}':
          if (inGroup) {
            regex.append("))");
            inGroup = false;
          } else {
            regex.append('}');
          }
          break;
        case ',':
          if (inGroup) {
            regex.append(")|(?:");
          } else {
            regex.append(',');
          }
          break;
        case '*':
          if (next(glob, index) == '*') {
            regex.append(".*");
            index++;
          } else {
            regex.append("[^/]*");
          }
          break;
        case '?':
          regex.append("[^/]");
          break;
        default:
          if (REGEX_META.matches(c)) {
            regex.append('\\');
          }
          regex.append(c);
      }
    }
    if (inGroup) {
      throw new PatternSyntaxException("Missing '}", glob, index - 1);
    }
    return regex.append('$').toString();
  }
}
