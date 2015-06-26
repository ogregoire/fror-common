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
import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.ORDERED;

import com.google.common.collect.Multiset;

import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.Spliterators;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.concurrent.ThreadSafe;

/**
 * A tool to randomly select elements from collections.
 *
 * <p>
 * Example usages:
 *
 * <pre><code>
 * Random random = ...
 * ImmutableMultiset&lt;String&gt; weightedStrings = ImmutableMultiset.&lt;String&gt;builder()
 *   .addCopies("a", 4)
 *   .addCopies("b", 3)
 *   .addCopies("c", 12)
 *   .addCopies("d", 1)
 *   .build();
 * RandomSelector&lt;String&gt; selector = RandomSelector.weighted(weightedElements);
 * List&lt;String&gt; selection = new ArrayList&lt;&gt;();
 * for (int i = 0; i &lt; 10; i++) {
 *   selection.add(selector.next(random));
 * }
 * </code></pre>
 *
 *
 * @author Olivier Grégoire &lt;https://github.com/ogregoire&gt;
 * @param <T>
 */
@ThreadSafe
public final class RandomSelector<T> {

  /**
   * Creates a new random selector based on a uniform distribution.
   *
   * <p>
   * A copy of <tt>elements</tt> is kept, so any modification to <tt>elements</tt> will not be
   * reflected in returned values.
   * 
   * @param <T>
   * @param elements
   * @return
   * @throws IllegalArgumentException if <tt>elements</tt> is empty.
   */
  public static <T> RandomSelector<T> uniform(final Collection<T> elements)
      throws IllegalArgumentException {
    checkNotNull(elements, "collection must not be null");
    checkArgument(!elements.isEmpty(), "collection must not be empty");

    final int size = elements.size();
    final T[] els = elements.toArray((T[]) new Object[size]);

    return new RandomSelector<>(els, (random) -> random.nextInt(size));
  }

  /**
   * Creates a random selector among <tt>elements</tt> where the elements have a weight defined by
   * their number of occurrences in <tt>elements</tt>.
   *
   * <p>
   * A copy of <tt>elements</tt> is kept, so any modification to <tt>elements</tt> will not be
   * reflected in returned values.
   * 
   * <p>
   * This is actually a memory optimization of <tt>{@link #uniform(java.util.Collection) }</tt> for
   * {@link Multiset multisets}. Use <tt>{@link #uniform(java.util.Collection) }</tt> if you need a
   * faster next implementation for a worse memory usage.
   *
   * @param <T>
   * @param elements
   * @return
   * @throws IllegalArgumentException if <tt>elements</tt> is empty.
   */
  public static <T> RandomSelector<T> weightedByCount(final Multiset<T> elements)
      throws IllegalArgumentException {
    checkNotNull(elements, "elements must not be null");
    checkArgument(!elements.isEmpty(), "elements must not be empty");

    final Set<Multiset.Entry<T>> entries = elements.entrySet();
    final double totalSize = elements.size();
    final int entriesSize = entries.size();
    final T[] els = (T[]) new Object[entriesSize];
    final double[] discreteProbabilities = new double[entriesSize];
    int i = 0;
    for (final Multiset.Entry<T> entry : entries) {
      els[i] = entry.getElement();
      discreteProbabilities[i] = entry.getCount() / totalSize;
      i++;
    }
    return new RandomSelector<>(els, new RandomWeightedSelection(discreteProbabilities));
  }

  /**
   * Creates a random selector among <tt>elements</tt> where the elements have a weight defined by
   * <tt>weighter</tt>.
   *
   * <p>
   * A copy of <tt>elements</tt> is kept, so any modification to <tt>elements</tt> will not be
   * reflected in returned values.
   * 
   * @param <T>
   * @param elements
   * @param weighter
   * @return
   * @throws IllegalArgumentException if <tt>elements</tt> is empty or if <tt>weighter</tt> returns
   * a negative value or <tt>0</tt>.
   */
  public static <T> RandomSelector<T> weighted(
      final Collection<T> elements,
      final ToDoubleFunction<? super T> weighter)
      throws IllegalArgumentException {
    checkNotNull(elements, "elements must not be null");
    checkNotNull(weighter, "weighter must not be null");
    checkArgument(!elements.isEmpty(), "elements must not be empty");

    final int size = elements.size();
    final T[] elementArray = elements.toArray((T[]) new Object[size]);

    double totalWeight = 0d;
    final double[] discreteProbabilities = new double[size];
    for (int i = 0; i < size; i++) {
      final double weight = weighter.applyAsDouble(elementArray[i]);
      checkArgument(weight > 0d, "weighter returned a negative number");
      discreteProbabilities[i] = weight;
      totalWeight += weight;
    }
    for (int i = 0; i < size; i++) {
      discreteProbabilities[i] /= totalWeight;
    }
    return new RandomSelector<>(elementArray, new RandomWeightedSelection(discreteProbabilities));
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
    return StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(
            new BaseIterator(random),
            IMMUTABLE | ORDERED
        ),
        false
    );
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

    RandomWeightedSelection(final double[] probabilities) {
      final int size = probabilities.length;

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

      final double[] pr = new double[size];
      final int[] al = new int[size];
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
