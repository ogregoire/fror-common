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
package be.fror.common.base;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.ceil;
import static java.lang.Math.exp;
import static java.lang.Math.log;
import static java.lang.Math.pow;
import static java.lang.Math.tan;

import java.util.Random;

/**
 *
 * @author Olivier Grégoire &lt;https://github.com/ogregoire&gt;
 */
public class RandomDistribution {

  /**
   *
   * @param random
   * @return
   * @throws NullPointerException if <tt>random</tt> is <tt>null</tt>.
   */
  public static RandomDistribution using(Random random) {
    checkNotNull(random);
    return new RandomDistribution(random);
  }

  private final Random random;

  private RandomDistribution(Random random) {
    this.random = random;
  }

  /**
   * @return a real number uniformly in <tt>[0, 1)</tt>.
   */
  public double uniform() {
    return random.nextDouble();
  }

  /**
   * @param n
   * @return an integer uniformly in <tt>[0, n)</tt>
   * @throws IllegalArgumentException if <tt>n &lt;= 0</tt>
   */
  public int uniform(int n) {
    checkArgument(n > 0, "n must be positive");
    return random.nextInt(n);
  }

  /**
   * @param a
   * @param b
   * @return an integer uniformly in <tt>[a, b)</tt>
   * @throws IllegalArgumentException if <tt>a &gt;= b</tt> or <tt>b - a &gt;=
   * Integer.MAX_VALUE</tt>
   */
  public int uniform(int a, int b) {
    checkArgument(a < b && ((long) b - a < Integer.MAX_VALUE), "invalid range");
    return a + uniform(b - a);
  }

  /**
   *
   * @param a
   * @param b
   * @return a real uniformly in <tt>[a, b)</tt>
   */
  public double uniform(double a, double b) {
    checkArgument(a < b, "invalid range");
    return a + uniform() * (b - a);
  }

  /**
   *
   * @param p
   * @return a boolean <tt>true</tt> with probability <tt>p</tt>, <tt>false</tt> otherwise
   * @throws IllegalArgumentException if <tt>p &lt; 0 || p &gt; 1</tt>
   */
  public boolean bernoulli(double p) {
    checkArgument(0.0d <= p && p <= 1.0d, "p must be between 0 and 1");
    return uniform() < p;
  }

  /**
   *
   * @return a boolean <tt>true</tt> with probability <tt>0.5</tt>, <tt>false</tt> otherwise
   */
  public boolean bernoulli() {
    return bernoulli(0.5);
  }

  /**
   *
   * @return a real number with a standard Gaussian distribution.
   */
  public double gaussian() {
    return random.nextGaussian();
  }

  /**
   *
   * @param mean
   * @param standardDeviation
   * @return a real number from a Gaussian distribution with given <tt>mean</tt> and
   * <tt>standardDeviation</tt>
   */
  public double gaussian(double mean, double standardDeviation) {
    return mean + standardDeviation * gaussian();
  }

  /**
   *
   * @param p
   * @return an integer with a geometric distribution with mean 1/p.
   * @throws IllegalArgumentException if <tt>p &lt; 0</tt> or <tt>p &gt;= 1</tt>
   */
  public int geometric(double p) {
    checkArgument(0.0d <= p && p <= 1.0d, "p must be between 0 and 1");
    return (int) ceil(log(uniform()) / log(1.0 - p));
  }

  /**
   *
   * @param lambda
   * @return
   * @throws IllegalArgumentException if <tt>lambda &lt;= 0</tt> or if
   * <tt>Double.isFinite(lambda)</tt> returns <tt>false</tt>
   */
  public int poisson(double lambda) {
    checkArgument(0.0d < lambda && Double.isFinite(lambda));
    int k = 0;
    double p = 1.0;
    double L = exp(-lambda);
    do {
      k++;
      p *= uniform();
    } while (p >= L);
    return k - 1;
  }

  /**
   *
   * @param alpha
   * @return
   * @throws IllegalArgumentException if <tt>alpha &lt;= 0</tt>
   */
  public double pareto(double alpha) {
    checkArgument(0.0d < alpha);
    return pow(1 - uniform(), -1.0 / alpha) - 1.0;
  }

  /**
   *
   * @return
   */
  public double cauchy() {
    return tan(PI * (uniform() - 0.5));
  }

  /**
   * This method is equivalent to
   *
   * <pre><code>discrete(a, 1e-14)</code></pre>
   *
   * @param a
   * @return
   * @throws IllegalArgumentException if <tt>a[<i>n</i>] &lt; 0</tt> or if the sum of all elements
   * of a is not <tt>1</tt> with a margin of <tt>1e-14</tt>.
   */
  public int discrete(double[] a) {
    return discrete(a, 1e-14);
  }

  /**
   *
   * @param a
   * @param epsilon
   * @return
   * @throws IllegalArgumentException if <tt>a[<i>n</i>] &lt; 0</tt> or if the sum of all elements
   * of a is not <tt>1</tt> with a margin of <tt>epsilon</tt>.
   */
  public int discrete(double[] a, double epsilon) {
    double sum = 0.0;
    for (int i = 0; i < a.length; i++) {
      checkArgument(0.0d <= a[i]);
      sum = sum + a[i];
    }
    checkArgument(abs(sum - 1.0d) < epsilon);
    while (true) {
      double r = uniform();
      sum = 0.0;
      for (int i = 0; i < a.length; i++) {
        sum = sum + a[i];
        if (sum > r) {
          return i;
        }
      }
    }
  }

  /**
   *
   * @param lambda
   * @return
   * @throws IllegalArgumentException if <tt>lambda &lt;= 0</tt>
   */
  public double exponential(double lambda) {
    checkArgument(0.0d < lambda);
    return -log(1 - uniform()) / lambda;
  }
}
