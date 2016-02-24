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
package org.jenkinsci.plugins.fortifycloudscan;

import hudson.model.BuildListener;


public class ConsoleLogger {

    private BuildListener listener;

    public ConsoleLogger(BuildListener listener) {
        this.listener = listener;
    }

    /**
     * Log messages to the builds console.
     * @param message The message to log
     */
    protected void log(String message) {
        final String outtag = "[" + FortifyCloudScanPlugin.PLUGIN_NAME + "] ";
        listener.getLogger().println(outtag + message.replaceAll("\\n", "\n" + outtag));
    }

}
