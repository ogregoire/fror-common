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
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;

import com.google.common.collect.Multiset;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.concurrent.ThreadSafe;

/**
 *
 * @author Olivier Grégoire &lt;https://github.com/ogregoire&gt;
 * @param <T>
 */
@ThreadSafe
public final class RandomSelector<T> {

  /**
   * Creates a new random selector based on a uniform distribution.
   *
   * @param <T>
   * @param collection
   * @return
   */
  public static <T> RandomSelector uniform(final Collection<T> collection) {
    checkNotNull(collection, "collection must not be null");
    checkArgument(!collection.isEmpty(), "collection must not be empty");

    final int size = collection.size();
    final T[] elements = collection.toArray((T[]) new Object[size]);

    return new RandomSelector<>(elements, (r) -> r.nextInt(size));
  }

  /**
   * Creates a new random selector based on a weighted distribution.
   *
   * @param <T>
   * @param probabilities
   * @return
   */
  public static <T> RandomSelector weighted(final Multiset<T> probabilities) {
    checkNotNull(probabilities, "probabilities must not be null");
    checkArgument(!probabilities.isEmpty(), "probabilities must not be empty");

    final Set<Multiset.Entry<T>> entries = probabilities.entrySet();
    final double totalSize = probabilities.size();
    final int entriesSize = entries.size();
    final T[] elements = (T[]) new Object[entriesSize];
    final double[] discreteProbabilities = new double[entriesSize];
    int i = 0;
    for (Multiset.Entry<T> entry : entries) {
      elements[i] = entry.getElement();
      discreteProbabilities[i] = entry.getCount() / totalSize;
      i++;
    }
    return new RandomSelector<>(elements, new RandomWeightedSelection(discreteProbabilities));
  }

  private final T[] elements;
  private final Selection selection;

  RandomSelector(final T[] elements, final Selection selection) {
    this.elements = elements;
    this.selection = selection;
  }

  /**
   * Returns the next element using <tt>random</tt>.
   *
   * @param random
   * @return
   */
  public T next(final Random random) {
    return this.elements[this.selection.next(random)];
  }

  /**
   * Returns a stream of elements using <tt>random</tt>. The stream must use a terminal operation to
   * become closed and free the resources it's been using.
   *
   * <p>
   * Even though this instance is thread-safe and for performance reasons, it is recommended to use
   * a different stream per thread given that Random has performance drawbacks in multi-threaded
   * environments.
   *
   * @param random
   * @return
   */
  public Stream<T> stream(final Random random) {
    checkNotNull(random, "random must not be null");
    return StreamSupport.stream(spliteratorUnknownSize(new BaseIterator(random), ORDERED), false);
  }

  private class BaseIterator implements Iterator<T> {

    private final Random random;

    BaseIterator(final Random random) {
      this.random = random;
    }

    @Override
    public boolean hasNext() {
      return true;
    }

    @Override
    public T next() {
      return RandomSelector.this.next(this.random);
    }
  }

  static interface Selection {

    int next(final Random random);
  }

  private static class RandomWeightedSelection implements Selection {
    // Alias method implementation O(1)
    // using Vose's algorithm to initialize O(n)

    private final double[] probabilities;
    private final int[] alias;

    RandomWeightedSelection(double[] probabilities) {
      final int size = probabilities.length;

      // Defensive copy, though it's not really necessary as long this class stays package private
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

      double[] pr = new double[size];
      int[] al = new int[size];
      this.probabilities = pr;
      this.alias = al;

      while (largeSize != 0 && smallSize != 0) {
        final int less = small[--smallSize];
        final int more = large[--largeSize];
        pr[less] = probabilities[less] * size;
        al[less] = more;
        probabilities[more] += probabilities[less] - average;
        if (probabilities[more] < average) {
          small[smallSize++] = more;
        } else {
          large[largeSize++] = more;
        }
      }
      while (smallSize != 0) {
        pr[small[--smallSize]] = 1d;
      }
      while (largeSize != 0) {
        pr[large[--largeSize]] = 1d;
      }
    }

    @Override
    public int next(final Random random) {
      final int column = random.nextInt(this.probabilities.length);
      return random.nextDouble() < this.probabilities[column]
          ? column
          : this.alias[column];
    }
  }
}
