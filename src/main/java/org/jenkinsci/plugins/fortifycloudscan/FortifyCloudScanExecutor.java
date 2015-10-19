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

import java.io.IOException;
import java.io.Serializable;

/**
 * This class is called by the FortifyCloudScanBuilder (the Jenkins build-step plugin).
 *
 * @author Steve Springett (steve.springett@owasp.org)
 */
public class FortifyCloudScanExecutor implements Serializable {

    private static final long serialVersionUID = 3595913479313812273L;

    private String args;
    private BuildListener listener;

    /**
     * Constructs a new FortifyCloudScanExecutor object.
     *
     * @param args Options to be used for execution
     * @param listener BuildListener object to interact with the current build
     */
    public FortifyCloudScanExecutor(String args, BuildListener listener) {
        this.args = args;
        this.listener = listener;
    }

    /**
     * Executes cloudscan with configured arguments
     *
     * @return a boolean value indicating if the command was executed successfully or not.
     */
    public boolean perform() {
        Process process;
        try {
            // Java exec requires that commands containing spaces be in an array
            process = Runtime.getRuntime().exec(args);

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return false;
            }
        } catch (InterruptedException e) {
            log("Could not execute job: " + e.getMessage());
        } catch (IOException e) {
            log("Could not execute job: " + e.getMessage());
        }
        return true;
    }

    /**
     * Log messages to the builds console.
     * @param message The message to log
     */
    private void log(String message) {
        final String outtag = "[" + FortifyCloudScanPlugin.PLUGIN_NAME + "] ";
        listener.getLogger().println(outtag + message.replaceAll("\\n", "\n" + outtag));
    }
}
