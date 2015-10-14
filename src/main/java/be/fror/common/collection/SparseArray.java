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
package be.fror.common.collection;

import be.fror.common.base.MoreArrays;

import java.util.Arrays;

/**
 *
 * @author Olivier Grégoire
 */
public class SparseArray<E> {

  private static final Object DELETED = new Object();

  private int[] keys;
  private Object[] values;
  private int size;
  private boolean dirty = false;

  public static <T> SparseArray<T> create() {
    return new SparseArray(16);
  }

  private SparseArray(int initialCapacity) {
    keys = new int[initialCapacity];
    values = new Object[initialCapacity];
    size = 0;
  }

  public boolean isEmpty() {
    return size() == 0;
  }

  public int size() {
    if (dirty) {
      cleanup();
    }
    return size;
  }

  public E get(int key) {
    return get(key, null);

  }

  public E get(int key, E defaultValue) {
    int i = Arrays.binarySearch(keys, 0, size, key);
    if (i < 0 || values[i] == DELETED) {
      return defaultValue;
    } else {
      return (E) values[i];
    }
  }

  public void remove(int key) {
    int i = Arrays.binarySearch(keys, 0, size, key);
    if (i >= 0) {
      if (values[i] != DELETED) {
        values[i] = DELETED;
        dirty = true;
      }
    }
  }

  public void put(int key, E value) {
    int i = Arrays.binarySearch(keys, 0, size, key);
    if (i >= 0) {
      values[i] = value;
    } else {
      i = ~i;
      if (i < size && values[i] == DELETED) {
        keys[i] = key;
        values[i] = value;
        return;
      }
      if (dirty && size >= keys.length) {
        cleanup();
        i = ~Arrays.binarySearch(keys, 0, size, key);
      }
      keys = MoreArrays.insert(keys, size, i, key);
      values = MoreArrays.insert(values, size, i, value);
      size++;
    }
  }

  public void clear() {
    int sz = size;
    Object[] vals = values;
    Arrays.fill(vals, 0, sz, null);
    size = 0;
    dirty = false;
  }

  private void cleanup() {
    int sz = size;
    int o = 0;
    int[] ks = keys;
    Object[] vals = values;
    for (int i = 0; i < sz; i++) {
      Object val = vals[i];
      if (val != DELETED) {
        if (i != o) {
          ks[o] = ks[i];
          vals[o] = val;
          vals[i] = null;
        }
        o++;
      }
    }
    dirty = false;
    size = o;
  }
}
