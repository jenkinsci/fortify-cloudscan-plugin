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

import java.io.Serializable;

/**
 * This class is called by the FortifyCloudScanBuilder (the Jenkins build-step plugin).
 *
 * @author Steve Springett (steve.springett@owasp.org)
 */
public class FortifyCloudScanExecutor implements Serializable {

    private static final long serialVersionUID = 4781360460201081295L;

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
     * Performs a fortifycloudscan analysis build.
     *
     * @return a boolean value indicating if the build was successful or not. A
     * successful build is not determined by the ability to analyze dependencies,
     * rather, simply to determine if errors were encountered during the execution.
     */
    public boolean perform() {
        return false;
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
