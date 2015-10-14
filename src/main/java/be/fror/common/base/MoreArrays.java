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

import static be.fror.common.base.Preconditions.checkArgument;

import java.lang.reflect.Array;

/**
 *
 * @author Olivier Grégoire
 */
public final class MoreArrays {

  private MoreArrays() {
  }

  public static <T> T[] append(T[] array, int currentSize, T element) {
    checkArgument(currentSize <= array.length);
    if (currentSize == array.length) {
      T[] copy = newArray(array, growSize(currentSize));
      System.arraycopy(array, 0, copy, 0, currentSize);
      array = copy;
    }
    array[currentSize] = element;
    return array;
  }

  public static int[] append(int[] array, int currentSize, int element) {
    checkArgument(currentSize <= array.length);
    if (currentSize == array.length) {
      int[] copy = new int[growSize(currentSize)];
      System.arraycopy(array, 0, copy, 0, currentSize);
      array = copy;
    }
    array[currentSize] = element;
    return array;
  }

  public static long[] append(long[] array, int currentSize, long element) {
    checkArgument(currentSize <= array.length);
    if (currentSize == array.length) {
      long[] copy = new long[growSize(currentSize)];
      System.arraycopy(array, 0, copy, 0, currentSize);
      array = copy;
    }
    array[currentSize] = element;
    return array;
  }

  public static boolean[] append(boolean[] array, int currentSize, boolean element) {
    checkArgument(currentSize <= array.length);
    if (currentSize == array.length) {
      boolean[] copy = new boolean[growSize(currentSize)];
      System.arraycopy(array, 0, copy, 0, currentSize);
      array = copy;
    }
    array[currentSize] = element;
    return array;
  }

  public static <T> T[] insert(T[] array, int currentSize, int index, T element) {
    checkArgument(currentSize <= array.length);
    if (currentSize < array.length) {
      System.arraycopy(array, index, array, index + 1, currentSize - index);
      array[index] = element;
      return array;
    }
    T[] newArray = newArray(array, growSize(currentSize));
    System.arraycopy(array, 0, newArray, 0, index);
    System.arraycopy(array, index, newArray, index + 1, array.length - index);
    newArray[index] = element;
    return newArray;
  }

  public static int[] insert(int[] array, int currentSize, int index, int element) {
    checkArgument(currentSize <= array.length);
    if (currentSize < array.length) {
      System.arraycopy(array, index, array, index + 1, currentSize - index);
      array[index] = element;
      return array;
    }
    int[] newArray = new int[growSize(currentSize)];
    System.arraycopy(array, 0, newArray, 0, index);
    System.arraycopy(array, index, newArray, index + 1, array.length - index);
    newArray[index] = element;
    return newArray;
  }

  public static long[] insert(long[] array, int currentSize, int index, long element) {
    checkArgument(currentSize <= array.length);
    if (currentSize < array.length) {
      System.arraycopy(array, index, array, index + 1, currentSize - index);
      array[index] = element;
      return array;
    }
    long[] newArray = new long[growSize(currentSize)];
    System.arraycopy(array, 0, newArray, 0, index);
    System.arraycopy(array, index, newArray, index + 1, array.length - index);
    newArray[index] = element;
    return newArray;
  }

  public static boolean[] insert(boolean[] array, int currentSize, int index, boolean element) {
    checkArgument(currentSize <= array.length);
    if (currentSize < array.length) {
      System.arraycopy(array, index, array, index + 1, currentSize - index);
      array[index] = element;
      return array;
    }
    boolean[] newArray = new boolean[growSize(currentSize)];
    System.arraycopy(array, 0, newArray, 0, index);
    System.arraycopy(array, index, newArray, index + 1, array.length - index);
    newArray[index] = element;
    return newArray;
  }

  private static <T> T[] newArray(T[] array, int newSize) {
    return (T[]) Array.newInstance(array.getClass().getComponentType(), newSize);
  }

  private static int growSize(int currentSize) {
    return currentSize <= 8 ? 16 : Integer.highestOneBit(currentSize) << 1;
  }
}
