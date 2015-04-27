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
package be.fror.common.collection;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Multiset;

import java.util.Arrays;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author Olivier Grégoire &lt;https://github.com/ogregoire&gt;
 * @param <T>
 */
public final class RandomWeightedSelection<T> {

  static interface Algorithm {

    public void init(double[] probabilities);

    public int next(Random random);
  }

  public static <T> RandomWeightedSelection<T> from(Multiset<T> probabilities) {
    checkNotNull(probabilities, "probabilities must not be null");
    checkArgument(!probabilities.isEmpty(), "probabilities must not be empty");

    Set<Multiset.Entry<T>> entries = probabilities.entrySet();
    final double totalSize = probabilities.size();
    final int entriesSize = entries.size();
    T[] e = (T[]) new Object[entriesSize];
    double[] p = new double[entriesSize];
    int i = 0;
    for (Multiset.Entry<T> entry : entries) {
      e[i] = entry.getElement();
      p[i] = entry.getCount() / totalSize;
      i++;
    }
    Algorithm algorithm = new VoseAliasMethod();
    algorithm.init(p);
    return new RandomWeightedSelection<>(e, algorithm);
  }

  private final T[] elements;
  private final Algorithm algorithm;

  private RandomWeightedSelection(T[] elements, Algorithm algorithm) {
    this.elements = elements;
    this.algorithm = algorithm;
  }

  public T next(Random random) {
    return this.elements[this.algorithm.next(random)];
  }

  private static class VoseAliasMethod implements Algorithm {

    private double[] probabilities;
    private int[] alias;

    @Override
    public void init(double[] probabilities) {
      final int size = probabilities.length;

      probabilities = Arrays.copyOf(probabilities, size);

      final double average = 1d / size;
      final int[] small = new int[size];
      int smallSize = 0;
      final int[] large = new int[size];
      int largeSize = 0;

      for (int i = 0; i < size; i++) {
        if (probabilities[i] < average) {
          small[smallSize++] = i;
        } else {
          large[largeSize++] = i;
        }
      }

      this.probabilities = new double[size];
      this.alias = new int[size];

      while (largeSize != 0 && smallSize != 0) {
        int less = small[--smallSize];
        int more = large[--largeSize];
        this.probabilities[less] = probabilities[less] * size;
        this.alias[less] = more;
        probabilities[more] += probabilities[less] - average;
        if (probabilities[more] < average) {
          small[smallSize++] = more;
        } else {
          large[largeSize++] = more;
        }
      }
      while (smallSize != 0) {
        this.probabilities[small[--smallSize]] = 1d;
      }
      while (largeSize != 0) {
        this.probabilities[large[--largeSize]] = 1d;
      }
    }

    @Override
    public int next(Random random) {
      final int column = random.nextInt(this.probabilities.length);
      return random.nextDouble() < this.probabilities[column]
          ? column
          : this.alias[column];
    }
  }

}
