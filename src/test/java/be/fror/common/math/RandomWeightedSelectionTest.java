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

import com.google.common.collect.ImmutableMultiset;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;

import java.util.Random;

/**
 *
 * @author Olivier Grégoire &lt;https://github.com/ogregoire&gt;
 */
public class RandomWeightedSelectionTest {

  public RandomWeightedSelectionTest() {
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

  /**
   * Test of next method, of class RandomWeightedSelection.
   */
  @Test
  public void testNext() {
    RandomWeightedSelection<String> selection2 = RandomWeightedSelection.from(ImmutableMultiset.<String>builder()
        .addCopies("a", 4)
        .addCopies("b", 3)
        .addCopies("c", 12)
        .addCopies("d", 1)
        .build()
    );

    Random random2 = new Random(0);
    Multiset<String> result2 = TreeMultiset.create();
    for (int i = 0; i < 100000; i++) {
      result2.add(selection2.next(random2));
    }
    System.out.println(result2);

  }

}
