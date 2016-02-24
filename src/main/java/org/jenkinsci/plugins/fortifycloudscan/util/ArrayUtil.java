/*
 * This file is part of Fortify CloudScan Jenkins plugin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jenkinsci.plugins.fortifycloudscan.util;

public class ArrayUtil {

    private ArrayUtil() {}

    public static String[] merge(String[]... arrays) {
        int count = 0;
        for (String[] array: arrays) {
            count += array.length;
        }
        String[] mergedArray = (String[]) java.lang.reflect.Array.newInstance(arrays[0][0].getClass(), count);
        int start = 0;
        for (String[] array: arrays) {
            System.arraycopy(array, 0, mergedArray, start, array.length);
            start += array.length;
        }
        return mergedArray;
    }

}
