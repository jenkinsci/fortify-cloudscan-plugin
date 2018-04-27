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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import java.util.List;

public class CommandUtil {

    private CommandUtil() {}

    public static void append(List<String> command, Object confItem, String arg) {
        append(command, confItem, arg, false);
    }

    /**
     * Add arguments to the stack based on the type of parameter being added.
     */
    public static void append(List<String> command, Object confItem, String arg, boolean concat) {
        if (confItem == null && arg != null) {
            command.add(arg);
        }
        if (confItem instanceof String) {
            String value = (String)confItem;
            if (StringUtils.isNotBlank(value)) {
                if (value.contains(" ")) {
                    // Surround the value in quotes
                    value = "\"" + value + "\"";
                }
                if (concat) {
                    command.add(arg + value);
                } else {
                    command.add(arg);
                    command.add(value);
                }
            }
        } else if (confItem instanceof Boolean) {
            boolean value = (Boolean)confItem;
            if (value) {
                command.add(arg);
            }
        }
    }

    public static String toString(String[] stringArray) {
        final StringBuilder sb = new StringBuilder();
        for (String string : stringArray) {
            sb.append(string).append(" ");
        }
        return sb.toString().trim();
    }

    public static String generateShellCommand(String[] command) {
        final String shellCommand;
        if (SystemUtils.IS_OS_WINDOWS) {
            shellCommand = "cmd /c " + CommandUtil.toString(command);
        } else {
            shellCommand =  "sh -c '" + CommandUtil.toString(command) + "'";
        }
        return shellCommand;
    }

}
