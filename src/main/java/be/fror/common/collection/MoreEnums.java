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

import java.util.Arrays;
import java.util.stream.Stream;

/**
 *
 * @author Olivier Grégoire <https://github.com/ogregoire>
 */
public final class MoreEnums {

  /**
   * Creates a {@link Stream} from all the values of an enum class.
   * 
   * @param <T>
   * @param enumType
   * @return 
   */
  public static <T extends Enum<T>> Stream<T> stream(Class<T> enumType) {
    return Arrays.stream(enumType.getEnumConstants());
  }
}
