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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Olivier Grégoire &lt;https://github.com/ogregoire&gt;
 */
public class MoreStringsTest {

  public MoreStringsTest() {
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
   * Test of removeDiacriticalMarks method, of class MoreStrings.
   */
  @Test
  public void testRemoveDiacriticalMarks() {
    String[][] pairs = {
      // Acute accent latin
      { "ÁáẤấẮắǺǻǼǽĆćḈḉÉéẾếḖḗǴǵÍíḮḯḰḱĹĺḾḿŃńÓóỐốṌṍṒṓǾǿṔṕŔŕŚśṤṥÚúǗǘỨứṸṹẂẃÝýŹź",
        "AaAaAaAaÆæCcCcEeEeEeGgIiIiKkLlMmNnOoOoOoOoØøPpRrSsSsUuUuUuUuWwYyZz" },
      // Acute accent greek
      { "ΆάΈέΉήΊίΌόΎύΏώ",
        "ΑαΕεΗηΙιΟοΥυΩω" },
      // Acute accent cyrillic
      { "ЃѓЌќ",
        "ГгКк" },
      // Double acute accent latin
      { "ŐőŰű",
        "OoUu" },
      // Double acute accent cyrillic
      { "Ӳӳ",
        "Уу" },
      // Grave accent latin
      { "ÀàẦầẰằÈèỀềḔḕÌìǸǹÒòỒồṐṑÙùǛǜỪừẀẁỲỳ",
        "AaAaAaEeEeEeIiNnOoOoOoUuUuUuWwYy" },
      // Double grave accent latin
      { "ȀȁȄȅȈȉȌȍȐȑȔȕ",
        "AaEeIiOoRrUu" },
      { "Breve Latin",
        "ĂăĔĕĬĭŎŏŬŭ",
        "AaEeIiOoUu" },
      { "Breve Azerbaijani, Tatar, Turkish",
        "Ğğ",
        "Gg" },
      { "Breve Vietnamese",
        "ẮắẰằẲẳẴẵẶặ",
        "AaAaAaAaAa" },
      { "Breve Cyrillic",
        "ЙйЎўӐӑӖӗ",
        "ИиУуАаЕе" },
      { "Breve Greek",
        "ᾸᾰῘῐῨῠ",
        "ΑαΙιΥυ" },
      { "Breve Arabic, Hittite, Akkadian, Egyptian transliteration",
        "Ḫḫ",
        "Hh" },
      { "Inverted Breve Latin",
        "ȂȃȆȇȊȋȎȏȒȓȖȗ",
        "AaEeIiOoRrUu" },
      { "Caron Latin",
        "ǍǎČčĎďǅǄǆĚěǦǧȞȟǏǐǨǩĽľŇňǑǒŘřŠšṦṧŤťǓǔǙǚŽž",
        "AaCcDdDzDZdzEeGgHhIiKkLlNnOoRrSsSsTtUuUuZz" },
      { "Caron Uralic",
        "Ǯǯ",
        "Ʒʒ" },
    };
    for (String[] pair : pairs) {
      if (pair.length == 3) {
        assertThat(pair[0], MoreStrings.removeDiacriticalMarks(pair[1]), is(equalTo(pair[2])));
      } else {
        assertThat(MoreStrings.removeDiacriticalMarks(pair[0]), is(equalTo(pair[1])));
      }
    }
  }
}