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

import static be.fror.common.base.Preconditions.checkNotNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;

/**
 *
 * @author Olivier Grégoire
 */
public final class Throwables {

  private Throwables() {
  }

  public static <X extends Throwable> void propagateIfInstanceOf(
      @Nullable Throwable throwable, Class<X> declaredType) throws X {
    if (throwable != null && declaredType.isInstance(throwable)) {
      throw declaredType.cast(throwable);
    }
  }

  public static void propagateIfPossible(@Nullable Throwable throwable) {
    propagateIfInstanceOf(throwable, Error.class);
    propagateIfInstanceOf(throwable, RuntimeException.class);
  }

  public static <X extends Throwable> void propagateIfPossible(
      @Nullable Throwable throwable, Class<X> declaredType) throws X {
    propagateIfInstanceOf(throwable, declaredType);
    propagateIfPossible(throwable);
  }

  public static <X1 extends Throwable, X2 extends Throwable> void propagateIfPossible(
      @Nullable Throwable throwable, Class<X1> declaredType1, Class<X2> declaredType2)
      throws X1, X2 {
    checkNotNull(declaredType2);
    propagateIfInstanceOf(throwable, declaredType1);
    propagateIfPossible(throwable, declaredType2);
  }

  public static RuntimeException propagate(Throwable throwable) {
    propagateIfPossible(checkNotNull(throwable));
    throw new RuntimeException(throwable);
  }

  @CheckReturnValue
  public static Throwable getRootCause(Throwable throwable) {
    Throwable cause;
    while ((cause = throwable.getCause()) != null) {
      throwable = cause;
    }
    return throwable;
  }

  @CheckReturnValue
  public static List<Throwable> getCausalChain(Throwable throwable) {
    checkNotNull(throwable);
    List<Throwable> causes = new ArrayList<>(4);
    while (throwable != null) {
      causes.add(throwable);
      throwable = throwable.getCause();
    }
    return Collections.unmodifiableList(causes);
  }

  @CheckReturnValue
  public static String getStackTraceAsString(Throwable throwable) {
    StringWriter stringWriter = new StringWriter();
    throwable.printStackTrace(new PrintWriter(stringWriter));
    return stringWriter.toString();
  }

}
