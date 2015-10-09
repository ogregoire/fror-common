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
package be.fror.common.resource;

import be.fror.common.io.ByteSource;

import java.io.UncheckedIOException;
import java.lang.ref.SoftReference;
import java.util.function.Supplier;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Loads a resource and returns it or a cached instance of it.
 * 
 * @author Olivier Grégoire
 * @param <T>
 */
@ThreadSafe
public final class Resource<T> implements Supplier<T> {

  private final ByteSource source;
  private final ResourceLoader<T> loader;

  private volatile SoftReference<T> reference;

  private final Object lock = new Object();

  Resource(ByteSource source, ResourceLoader<T> loader) {
    this.source = source;
    this.loader = loader;
    this.reference = new SoftReference<>(null);
  }

  /**
   * @return the cached resource
   * @throws UncheckedIOException 
   */
  @Override
  public T get() {
    T object = reference.get();
    if (object == null) {
      synchronized (lock) {
        object = reference.get();
        if (object == null) {
          object = loader.uncheckedLoad(source);
          reference = new SoftReference(object);
        }
      }
    }
    return object;
  }
}
