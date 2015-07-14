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

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Olivier Grégoire &lt;https://github.com/ogregoire&gt;
 */
public class RandomSelectorTest {

  public RandomSelectorTest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testUniform_next() {
    List<String> elements = Arrays.asList("a", "b", "c", "d");
    Random random = new Random(0);

    RandomSelector<String> selector = RandomSelector.uniform(elements);
    Multiset<String> selectedElements = selectNext(selector, random, 1_000_000);

    for (Multiset.Entry<String> entry : selectedElements.entrySet()) {
      assertThat((double) entry.getCount() / 1_000_000, is(closeTo(0.25d, 0.01d)));
    }

  }

  @Test
  public void testWeightedByCount_next() {

    ImmutableMultiset<String> weightedElements = ImmutableMultiset.<String>builder()
        .addCopies("a", 4)
        .addCopies("b", 3)
        .addCopies("c", 12)
        .addCopies("d", 1)
        .build();
    Random random = new Random(0);

    RandomSelector<String> selector = RandomSelector.weightedByCount(weightedElements);
    Multiset<String> selectedElements = selectNext(selector, random, 1_000_000);

    for (Multiset.Entry<String> entry : weightedElements.entrySet()) {
      double expectedRatio = (double) entry.getCount() / weightedElements.size();
      double actualRatio = (double) selectedElements.count(entry.getElement()) / selectedElements.size();
      assertThat(actualRatio, is(closeTo(expectedRatio, 0.01)));
    }

  }

  private <T extends Comparable<?>> Multiset<T> selectNext(RandomSelector<T> selector, Random random, int size) {
    Multiset<T> selectedElements = TreeMultiset.create();
    for (int i = 0; i < size; i++) {
      selectedElements.add(selector.next(random));
    }
    return selectedElements;
  }

  @Test
  public void testWeightedByCount_stream() {

    final long randomSeed = 0;
    final int elements = 1_000_000;

    ImmutableMultiset<String> weightedElements = ImmutableMultiset.<String>builder()
        .addCopies("a", 4)
        .addCopies("b", 3)
        .addCopies("c", 12)
        .addCopies("d", 1)
        .build();

    RandomSelector<String> selector = RandomSelector.weightedByCount(weightedElements);

    List<String> streamed = selector.stream(new Random(randomSeed))
        .limit(elements)
        .collect(toList());

    Random random = new Random(randomSeed);
    List<String> nexted = new ArrayList<>();
    for (int i = 0; i < elements; i++) {
      nexted.add(selector.next(random));
    }

    assertThat(streamed, is(equalTo(nexted)));
  }

}
