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
package be.fror.common.resource2;

import com.google.common.io.ByteSource;

import java.lang.ref.SoftReference;
import java.util.function.Supplier;

/**
 *
 * @author Olivier Grégoire <https://github.com/ogregoire>
 */
public final class Resource<T> implements Supplier<T>, AutoCloseable {

  private final ByteSource source;
  private final ResourceLoader<T> loader;
  private SoftReference<T> reference;
  private final Object lock = new Object();

  Resource(ByteSource source, ResourceLoader<T> loader) {
    this.source = source;
    this.loader = loader;
    this.reference = new SoftReference<>(null);
  }

  @Override
  public T get() {
    T object = this.reference.get();
    if (object == null) {
      synchronized (this.lock) {
        object = this.reference.get();
        if (object == null) {
          object = this.loader.uncheckedLoad(this.source);
          this.reference = new SoftReference<>(object);
        }
      }
    }
    return object;
  }

  @Override
  public void close() {
    synchronized (this.lock) {
      this.reference = new SoftReference<>(null);
    }
  }
}