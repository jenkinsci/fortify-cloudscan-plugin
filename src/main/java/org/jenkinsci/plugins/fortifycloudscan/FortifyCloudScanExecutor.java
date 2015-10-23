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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;

/**
 * This class is called by FortifyCloudScanBuilder (the Jenkins build-step plugin).
 * Performs the external execution of the cloudscan command line interface.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 */
public class FortifyCloudScanExecutor implements Serializable {

    private static final long serialVersionUID = 3595913479313812273L;

    private String[] command;
    private BuildListener listener;

    /**
     * Constructs a new FortifyCloudScanExecutor object.
     *
     * @param command A String array of the command and options to use
     * @param listener BuildListener object to interact with the current build
     */
    public FortifyCloudScanExecutor(String[] command, BuildListener listener) {
        this.command = command;
        this.listener = listener;
    }

    /**
     * Executes cloudscan with configured arguments
     *
     * @return a boolean value indicating if the command was executed successfully or not.
     */
    public boolean perform() {
        String[] versionCommand = {command[0], "-version"};
        execute(versionCommand);
        logCommand();
        return execute(command);
    }

    /**
     * Executes the external cloudscan process sending stderr and stdout to the logger
     */
    private boolean execute(String[] command) {
        Process process;
        try {
            // Java exec requires that commands containing spaces be in an array
            process = Runtime.getRuntime().exec(command);

            // Redirect error and output to console
            StreamLogger erroutLogger = new StreamLogger(process.getErrorStream());
            StreamLogger stdoutLogger = new StreamLogger(process.getInputStream());
            erroutLogger.start();
            stdoutLogger.start();

            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (InterruptedException e) {
            log(Messages.Executor_Failure() + ": " + e.getMessage());
        } catch (IOException e) {
            log(Messages.Executor_Failure() + ": " + e.getMessage());
        }
        return false;
    }

    /**
     * Log messages to the builds console.
     * @param message The message to log
     */
    protected void log(String message) {
        final String outtag = "[" + FortifyCloudScanPlugin.PLUGIN_NAME + "] ";
        listener.getLogger().println(outtag + message.replaceAll("\\n", "\n" + outtag));
    }

    private void logCommand() {
        String cmd = Messages.Executor_Display_Options() + ": ";
        for (String param : command) {
            cmd = cmd + param + " ";
        }
        log(cmd);
    }

    private class StreamLogger extends Thread {
        InputStream is;

        private StreamLogger(InputStream is) {
            this.is = is;
        }

        @Override
        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line;
                while ((line = br.readLine()) != null) {
                    log(line);
                }
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

}
