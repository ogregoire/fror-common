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
package be.fror.common.resource;

import static be.fror.common.resource.ResourceLoaders.propertiesLoader;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Olivier Grégoire &lt;https://github.com/ogregoire&gt;
 */
public class ResourceLocatorTest {

  public ResourceLocatorTest() {
  }

  @BeforeClass
  public static void setUpClass() {
    if (!(ResourceLocatorTest.class.getClassLoader() instanceof URLClassLoader)) {
      fail("ResourceLocatorTest.class.getClassLoader() is not a URLClassLoader");
    }
  }

  @AfterClass
  public static void tearDownClass() {
  }

  private ResourceLocator instance;

  @Before
  public void setUp() throws IOException {
    instance = new ResourceLocator.Builder()
        .addClassLoader((URLClassLoader) ResourceLocatorTest.class.getClassLoader())
        .build();
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of locateResources method, of class ResourceLocator.
   */
  @Test
  public void testLocateResources_String() {
    List<URL> urls = instance.locateResources("books/*.properties").collect(toList());
    assertThat(urls.size(), is(2));
  }

  /**
   * Test of locateResources method, of class ResourceLocator.
   */
  @Test
  public void testLocateResources_Predicate() {

  }

  @Test
  public void testGetResources_String() {
    Properties agot = instance
        .loadResources("books/a_game_of_thrones.properties", propertiesLoader(UTF_8))
        .findFirst()
        .get();
    assertThat(agot.getProperty("title"), is(equalTo("A Game of Thrones")));
  }

  @Test
  public void testLocateServices() {
    AtomicInteger i = new AtomicInteger(0);
    instance.getServices(TestService.class).forEach(testClass -> {
      assertThat(testClass, isAssignableFrom(TestService.class));
      i.incrementAndGet();
    });
    assertThat(i.get(), is(2));
  }

  private static Matcher<Class<?>> isAssignableFrom(Class<?> type) {
    return new BaseMatcher<Class<?>>() {

      @Override
      public boolean matches(Object item) {
        return item instanceof Class && type.isAssignableFrom((Class<?>) item);
      }

      @Override
      public void describeTo(Description description) {
        description.appendValue(type);
      }
    };
  }
}
