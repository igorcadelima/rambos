/*******************************************************************************
 * MIT License
 *
 * Copyright (c) Igor Conrado Alves de Lima <igorcadelima@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package rambos;

import java.util.HashMap;
import java.util.Map;

/**
 * @author igorcadelima
 *
 */
public enum State {

  ENABLED, DISABLED;

  @Override
  public String toString() {
    return name().toLowerCase();
  }

  private static final Map<String, State> stringToEnum = new HashMap<String, State>();
  static { // Initialise map from constant name to enum constant
    for (State s : values()) {
      String str = s.toString();
      stringToEnum.put(str, s);
      stringToEnum.put(str.toLowerCase(), s);
    }
  }

  /**
   * Return state for string.
   * 
   * The only accepted strings are: {@code enabled}, {@code ENABLED}, {@code disabled}, and
   * {@code DISABLED}.
   * 
   * @param s string
   * @return State for string, or {@code null} if string is invalid
   */
  public static State fromString(String s) {
    return stringToEnum.get(s);
  }

  /**
   * Try to return State for string.
   * 
   * @param s string to be parsed
   * @param defaultValue State to be returned if {@code s} can not be parsed
   * @return state State for string, or {@code defaultValue} if string can not be parsed
   */
  public static State tryParse(String s, State defaultValue) {
    if (s == null)
      return defaultValue;

    State state = State.fromString(s);
    if (state == null)
      return defaultValue;
    return state;
  }
}
