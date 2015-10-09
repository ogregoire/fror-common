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

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Random;

/**
 *
 * @author Olivier Grégoire
 */
public class RandomDistributionTest {

  public RandomDistributionTest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  private static final int DEFAULT_RUNS = 100000;
  
  // Allow 1% deviation
  private static final double DEFAULT_EPSILON = 0.01;

  private RandomDistribution instance;

  @Before
  public void setUp() {
    instance = RandomDistribution.using(new Random(0L));
  }

  @After
  public void tearDown() {
    instance = null;
  }

  /**
   * Test of using method, of class RandomDistribution.
   */
  @Test
  public void testUsing() {
  }

  /**
   * Test of uniform method, of class RandomDistribution.
   */
  @Test
  public void testUniform_0args() {
    for (int i = 0; i < DEFAULT_RUNS; i++) {
      assertThat(instance.uniform(), inRangeClosedOpen(0.0, 1.0));
    }
  }

  /**
   * Test of uniform method, of class RandomDistribution.
   */
  @Test
  public void testUniform_int() {
    for (int i = 0; i < DEFAULT_RUNS; i++) {
      assertThat(instance.uniform(1), is(0));
      assertThat(instance.uniform(2), is(inRangeClosedOpen(0, 2)));
      assertThat(instance.uniform(10), is(inRangeClosedOpen(0, 10)));
    }
  }

  /**
   * Test of uniform method, of class RandomDistribution.
   */
  @Test
  public void testUniform_int_int() {
    for (int i = 0; i < DEFAULT_RUNS; i++) {
      assertThat(instance.uniform(10, 11), is(10));
      assertThat(instance.uniform(10, 12), is(inRangeClosedOpen(10, 12)));
      assertThat(instance.uniform(10, 20), is(inRangeClosedOpen(10, 20)));
    }
  }

  /**
   * Test of uniform method, of class RandomDistribution.
   */
  @Test
  public void testUniform_double_double() {
    for (int i = 0; i < DEFAULT_RUNS; i++) {
      assertThat(instance.uniform(-1.0d, 1.0d), inRangeClosedOpen(-1.0d, 1.0d));
    }
  }

  /**
   * Test of bernoulli method, of class RandomDistribution.
   */
  @Test
  public void testBernoulli_double() {

    int runs = DEFAULT_RUNS;
    double distribution = 0.1;
    double epsilon = DEFAULT_EPSILON;

    int count = 0;
    for (int i = 0; i < runs; i++) {
      if (instance.bernoulli(distribution)) {
        count++;
      }
    }
    assertThat((double) count / runs, closeTo(distribution, epsilon));
  }

  /**
   * Test of bernoulli method, of class RandomDistribution.
   */
  @Test
  public void testBernoulli_0args() {

    int runs = DEFAULT_RUNS;
    double epsilon = DEFAULT_EPSILON;

    int count = 0;
    for (int i = 0; i < runs; i++) {
      if (instance.bernoulli()) {
        count++;
      }
    }
    assertThat((double) count / runs, closeTo(0.5, epsilon));
  }

  /**
   * Test of gaussian method, of class RandomDistribution.
   */
  @Test
  public void testGaussian_0args() {
  }

  /**
   * Test of gaussian method, of class RandomDistribution.
   */
  @Test
  public void testGaussian_double_double() {
  }

  /**
   * Test of geometric method, of class RandomDistribution.
   */
  @Test
  public void testGeometric() {
  }

  /**
   * Test of poisson method, of class RandomDistribution.
   */
  @Test
  public void testPoisson() {
  }

  /**
   * Test of pareto method, of class RandomDistribution.
   */
  @Test
  public void testPareto() {
  }

  /**
   * Test of cauchy method, of class RandomDistribution.
   */
  @Test
  public void testCauchy() {
  }

  /**
   * Test of discrete method, of class RandomDistribution.
   */
  @Test
  public void testDiscrete_doubleArr() {
  }

  /**
   * Test of discrete method, of class RandomDistribution.
   */
  @Test
  public void testDiscrete_doubleArr_double() {
  }

  /**
   * Test of exp method, of class RandomDistribution.
   */
  @Test
  public void testExponential() {
  }

  private static Matcher<Double> inRangeClosedOpen(double min, double max) {
    return allOf(greaterThanOrEqualTo(min), lessThan(max));
  }

  private static Matcher<Integer> inRangeClosedOpen(int min, int max) {
    return allOf(greaterThanOrEqualTo(min), lessThan(max));
  }

}
