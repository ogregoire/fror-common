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

import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 *
 * @author Olivier Grégoire
 */
public class SparseArray<E> {

  private static final Object DELETED = new Object();

  private int[] keys;
  private Object[] values;
  private int size;

  private SparseArray(int initialCapacity) {
  }

  public E get(int key) {
    int i = Arrays.binarySearch(keys, 0, size, key);
    if (i < 0 || values[i] == DELETED) {
      throw new NoSuchElementException();
    } else {
      return (E)values[i];
    }
  }
  
  public void remove(int key) {
    int i = Arrays.binarySearch(keys, 0, size, key);
    if (i >= 0) {
      if (values[i] != DELETED) {
        values[i] = DELETED;
      }
    }
  }
  
  public void removeAt(int index) {
    
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
      keys = ;
      values = ;
      size++;
    }
  }
}
