/*
 * Copyright 2015 Olivier Grégoire <ogregoire@users.noreply.github.com>.
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

import java.util.function.Predicate;

import javax.annotation.Nullable;

/**
 *
 * @author Olivier Grégoire <ogregoire@users.noreply.github.com>
 */
public class MorePreconditions {

  private MorePreconditions() {
  }
  
  /**
   * 
   * @param <T>
   * @param value
   * @param predicate
   * @return 
   * @throws IllegalArgumentException if {@code value} doesn't match {@code predicate}
   */
  public static <T> T checkArgument(@Nullable T value, Predicate<? super T> predicate) {
    if (predicate.test(value)) {
      return value;
    } else {
      throw new IllegalArgumentException();
    }
  }
  
  /**
   * 
   * @param <T>
   * @param value
   * @param predicate
   * @param message
   * @return 
   * @throws IllegalArgumentException if {@code value} doesn't match {@code predicate}
   */
  public static <T> T checkArgument(@Nullable T value, Predicate<? super T> predicate, @Nullable Object message) {
    if (predicate.test(value)) {
      return value;
    } else {
      throw new IllegalArgumentException(String.valueOf(message));
    }
  }
  
  /**
   * 
   * @param <T>
   * @param value
   * @param predicate
   * @param messageFormat
   * @param messageParameters
   * @return 
   * @throws IllegalArgumentException if {@code value} doesn't match {@code predicate}
   */
  public static <T> T checkArgument(@Nullable T value, Predicate<? super T> predicate, String messageFormat, Object... messageParameters) {
    if (predicate.test(value)) {
      return value;
    } else {
      throw new IllegalArgumentException(String.format(messageFormat, messageParameters));
    }
  }
}
