/*
 * Copyright 2015 Olivier Grégoire <ogregoire@users.noreply.github.com>.
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
package be.fror.common.random;

import static be.fror.common.base.Preconditions.checkArgument;
import static java.lang.Math.max;

import java.util.Random;

/**
 *
 * @author Olivier Grégoire <ogregoire@users.noreply.github.com>
 */
public final class MersenneTwister extends Random {

  private static final int N = 624;
  private static final int M = 397;
  private static final int UPPER_MASK = 0x80000000;
  private static final int LOWER_MASK = 0x7fffffff;

  private static final int[] ZERO_OR_MATRIX = {0x0, 0x9908b0df};

  private static final int MASK_A = 0x9d2c5680;
  private static final int MASK_B = 0xefc60000;

  private int[] MT;
  private int index;

  public MersenneTwister() {
  }

  public MersenneTwister(long seed) {
    setSeed(seed);
  }

  public MersenneTwister(int[] seed) {
    setSeed(seed);
  }

  @Override
  public synchronized void setSeed(long seed) {
    MT = new int[N];
    MT[0] = (int) seed;
    for (int i = 1; i < N; i++) {
      MT[i] = (0x6c078965 * (MT[i - 1] ^ (MT[i - 1] >>> 30)) + i);
    }
    index = N;
  }

  public synchronized void setSeed(int[] seed) {
    checkArgument(seed.length != 0);
    setSeed(0x12bd6aaL);
    int i = 1;
    for (int seedIndex = 0, k = max(N, seed.length); k != 0; k--) {
      MT[i] = (MT[i] ^ ((MT[i - 1] ^ (MT[i - 1] >>> 30)) * 0x19660d)) + seed[seedIndex] + seedIndex;
      if (++i == N) {
        MT[0] = MT[N - 1];
        i = 1;
      }
      if (++seedIndex == seed.length) {
        seedIndex = 0;
      }
    }
    for (int k = N - 1; k != 0; k--) {
      MT[i] = (MT[i] ^ ((MT[i - 1] ^ (MT[i - 1] >>> 30)) * 0x5d588b65)) - i;
      if (++i >= N) {
        MT[0] = MT[N - 1];
        i = 1;
      }
    }
    MT[0] = 0x80000000;
  }

  @Override
  protected int next(int bits) {
    if (index == N) {
      final int[] mt = MT;
      final int[] zom = ZERO_OR_MATRIX;
      final int mid = N - M;
      for (int n = 0; n < mid; n++) {
        int y = (mt[n] & UPPER_MASK) | (mt[n + 1] & LOWER_MASK);
        mt[n] = mt[n + M] ^ (y >>> 1) ^ zom[y & 0x1];
      }
      for (int n = mid; n < N - 1; n++) {
        int y = (mt[n] & UPPER_MASK) | (mt[n + 1] & LOWER_MASK);
        mt[n] = mt[n - mid] ^ (y >>> 1) ^ zom[y & 0x1];
      }
      int y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
      mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ zom[y & 0x1];
      index = 0;
    }
    int y = MT[index++];
    y ^= y >>> 11;
    y ^= (y << 7) & MASK_A;
    y ^= (y << 15) & MASK_B;
    y ^= y >>> 18;
    return y >>> (32 - bits);
  }

}
