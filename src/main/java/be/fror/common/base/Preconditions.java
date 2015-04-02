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

import java.util.function.Predicate;

import javax.annotation.Nullable;

/**
 *
 * @author Olivier Grégoire <https://github.com/fror>
 */
public final class Preconditions {

  private Preconditions() {
  }

  /**
   * 
   * @param <T>
   * @param reference
   * @return 
   */
  public static <T> T checkNotNull(T reference) {
    if (reference == null) {
      throw new NullPointerException();
    }
    return reference;
  }

  /**
   * 
   * @param <T>
   * @param reference
   * @param message
   * @return <tt>reference</tt>
   */
  public static <T> T checkNotNull(T reference, @Nullable Object message) {
    if (reference == null) {
      throw new NullPointerException(String.valueOf(message));
    }
    return reference;
  }

  /**
   * 
   * @param <T>
   * @param reference
   * @param messageFormat
   * @param messageParameters
   * @return 
   */
  public static <T> T checkNotNull(T reference, @Nullable String messageFormat, @Nullable Object... messageParameters) {
    if (reference == null) {
      throw new NullPointerException(String.format(messageFormat, messageParameters));
    }
    return reference;
  }

  /**
   * 
   * @param expression 
   */
  public static void checkArgument(boolean expression) {
    if (!expression) {
      throw new IllegalArgumentException();
    }
  }

  /**
   * 
   * @param <T>
   * @param object
   * @param predicate
   * @return 
   */
  public static <T> T checkArgument(@Nullable T object, Predicate<T> predicate) {
    if (!predicate.test(object)) {
      throw new IllegalArgumentException();
    }
    return object;
  }

  /**
   * 
   * @param expression
   * @param message 
   */
  public static void checkArgument(boolean expression, Object message) {
    if (!expression) {
      throw new IllegalArgumentException(String.valueOf(message));
    }
  }

  /**
   * 
   * @param <T>
   * @param object
   * @param predicate
   * @param message
   * @return 
   */
  public static <T> T checkArgument(@Nullable T object, Predicate<T> predicate, @Nullable Object message) {
    if (!predicate.test(object)) {
      throw new IllegalArgumentException(String.valueOf(message));
    }
    return object;
  }

  /**
   * 
   * @param expression
   * @param messageFormat
   * @param messageParameters 
   */
  public static void checkArgument(boolean expression, @Nullable String messageFormat, @Nullable Object... messageParameters) {
    if (!expression) {
      throw new IllegalArgumentException(String.format(messageFormat, messageParameters));
    }
  }

  /**
   * 
   * @param <T>
   * @param object
   * @param predicate
   * @param messageFormat
   * @param messageParameters
   * @return 
   */
  public static <T> T checkArgument(@Nullable T object, Predicate<T> predicate, @Nullable String messageFormat, @Nullable Object... messageParameters) {
    if (!predicate.test(object)) {
      throw new IllegalArgumentException(String.format(messageFormat, messageParameters));
    }
    return object;
  }

  /**
   * 
   * @param expression 
   */
  public static void checkState(boolean expression) {
    if (!expression) {
      throw new IllegalStateException();
    }
  }

  /**
   * 
   * @param <T>
   * @param object
   * @param predicate
   * @return 
   */
  public static <T> T checkState(@Nullable T object, Predicate<T> predicate) {
    if (!predicate.test(object)) {
      throw new IllegalStateException();
    }
    return object;
  }

  /**
   * 
   * @param expression
   * @param message 
   */
  public static void checkState(boolean expression, @Nullable Object message) {
    if (!expression) {
      throw new IllegalStateException(String.valueOf(message));
    }
  }

  /**
   * 
   * @param <T>
   * @param object
   * @param predicate
   * @param message
   * @return 
   */
  public static <T> T checkState(@Nullable T object, Predicate<T> predicate, @Nullable Object message) {
    if (!predicate.test(object)) {
      throw new IllegalStateException(String.valueOf(message));
    }
    return object;
  }

  /**
   * 
   * @param expression
   * @param messageFormat
   * @param messageParameters 
   */
  public static void checkState(boolean expression, @Nullable String messageFormat, @Nullable Object... messageParameters) {
    if (!expression) {
      throw new IllegalStateException(String.format(messageFormat, messageParameters));
    }
  }

  /**
   * 
   * @param <T>
   * @param object
   * @param predicate
   * @param messageFormat
   * @param messageParameters
   * @return 
   */
  public static <T> T checkState(@Nullable T object, Predicate<T> predicate, @Nullable String messageFormat, @Nullable Object... messageParameters) {
    if (!predicate.test(object)) {
      throw new IllegalStateException(String.format(messageFormat, messageParameters));
    }
    return object;
  }
}
