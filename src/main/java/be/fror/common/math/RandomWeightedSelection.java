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
package be.fror.common.math;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Multiset;

import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

/**
 *
 * @author Olivier Grégoire &lt;https://github.com/ogregoire&gt;
 * @param <T>
 */
public final class RandomWeightedSelection<T> {

  public static interface Algorithm {

    public void init(double[] probabilities);

    public int next(Random random);
  }

  public static <T> RandomWeightedSelection<T> from(Multiset<T> probabilities) {
    return from(probabilities, VoseAliasMethod::new);
  }

  public static <T> RandomWeightedSelection<T> from(Multiset<T> probabilities, Supplier<? extends Algorithm> algorithmSupplier) {
    checkNotNull(probabilities, "probabilities must not be null");
    checkNotNull(algorithmSupplier, "algorithmSupplier must not be null");
    checkArgument(!probabilities.isEmpty(), "probabilities must not be empty");
    Algorithm algorithm = algorithmSupplier.get();
    checkNotNull(algorithm, "algorithmSupplier must not supply null");

    Set<Multiset.Entry<T>> entries = probabilities.entrySet();
    final double totalSize = probabilities.size();
    final int entriesSize = entries.size();
    T[] e = (T[])new Object[entriesSize];
    double[] p = new double[entriesSize];
    int i = 0;
    for (Multiset.Entry<T> entry : entries) {
      e[i] = entry.getElement();
      p[i] = entry.getCount() / totalSize;
      i++;
    }
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

}
