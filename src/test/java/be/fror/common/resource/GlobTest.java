/*
 * Copyright 2015 Olivier Grégoire <https://github.com/fror>.
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

import static org.hamcrest.Matchers.*;

import org.junit.After;
import org.junit.AfterClass;

import static org.junit.Assert.*;

import com.sun.glass.ui.SystemClipboard;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * @author Olivier Grégoire <https://github.com/fror>
 */
public class GlobTest {

  public GlobTest() {
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
  public void testToRegexPattern() throws IOException {
    String[] paths = {
      "/META-INF/MANIFEST.MF",
      "/META-INF/services/com.example.Plugin",
      "/com/example/Main.java",
      "/com/example/Main.class",
      "/com/example/service/Service.java",
      "/com/example/service/Service.class",
      "/com/example/otherservice/Service.java",
      "/com/example/otherservice/Service.class",
      "/com/example/service/ServiceTest.java",
      "/com/example/service/ServiceTest.class",
      "/assets/img/logo.png",
      "/assets/text/en/Message.properties",
      "/assets/text/en/Messager.properties",
      "/assets/text/en/Messages.properties",
      "/assets/text/fr/Messages.properties"
    };
    testPattern("/META-INF/*", paths, 0); // Stay in the specified folder
    testPattern("/**/Service.java", paths, 4, 6); // Look in any folder
    testPattern("/com/example/Main.{java,class}", paths, 2, 3); // Any of
    testPattern("/**/*.{java,class}", paths, 2, 3, 4, 5, 6, 7, 8, 9); // Mix of all previous
    testPattern("/assets/**/*.properties", paths, 11, 12, 13, 14);
    testPattern("/assets/text/en/Message?.properties", paths, 12, 13); // One mandatory differing character
  }

  private void testPattern(String pattern, String[] paths, Integer... matchingPathIndices) {
    List<Integer> matchingPaths = Arrays.asList(matchingPathIndices);
    Pattern p = Glob.toRegexPattern(pattern);
    for (int i = 0; i < paths.length; i++) {
      String path = paths[i];
      String msg = String.format("%s matching %s", pattern, path);
      assertThat(msg, p.matcher(path).matches(), is(equalTo(matchingPaths.contains(i))));
    }
  }

}
