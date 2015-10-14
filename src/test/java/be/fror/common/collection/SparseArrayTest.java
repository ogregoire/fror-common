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

import static org.hamcrest.Matchers.is;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Olivier Grégoire
 */
public class SparseArrayTest {
  
  public SparseArrayTest() {
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
   * Test of create method, of class SparseArray.
   */
  @Test
  public void testCreate() {
  }

  /**
   * Test of isEmpty method, of class SparseArray.
   */
  @Test
  public void testIsEmpty() {
    SparseArray<String> array = SparseArray.create();
    assertThat(array.isEmpty(), is(true));
    array.put(1000, "a");
    assertThat(array.isEmpty(), is(false));
    array.remove(1000);
    assertThat(array.isEmpty(), is(true));
  }

  /**
   * Test of size method, of class SparseArray.
   */
  @Test
  public void testSize() {
  }

  /**
   * Test of get method, of class SparseArray.
   */
  @Test
  public void testGet_int() {
  }

  /**
   * Test of get method, of class SparseArray.
   */
  @Test
  public void testGet_int_GenericType() {
  }

  /**
   * Test of remove method, of class SparseArray.
   */
  @Test
  public void testRemove() {
  }

  /**
   * Test of put method, of class SparseArray.
   */
  @Test
  public void testPut() {
  }

  /**
   * Test of clear method, of class SparseArray.
   */
  @Test
  public void testClear() {
  }
  
}
