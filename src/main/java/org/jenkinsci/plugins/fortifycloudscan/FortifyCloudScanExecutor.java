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
import org.jenkinsci.plugins.fortifycloudscan.util.CommandUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is called by FortifyCloudScanBuilder (the Jenkins build-step plugin).
 * Performs the external execution of the cloudscan command line interface.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 */
public class FortifyCloudScanExecutor implements Serializable {

    private static final long serialVersionUID = 3595913479313812273L;

    private ConsoleLogger logger;
    private Options options;

    /**
     * Constructs a new FortifyCloudScanExecutor object.
     *
     * @param listener BuildListener object to interact with the current build
     * @param options The options that will be used during execution
     */
    public FortifyCloudScanExecutor(BuildListener listener, Options options) {
        this.logger = new ConsoleLogger(listener);
        this.options = options;
    }

    /**
     * Executes cloudscan with configured arguments
     *
     * @return a boolean value indicating if the command was executed successfully or not.
     */
    public boolean perform() {
    //public boolean perform(Map envVars, String[] command, String[] rules, String[] scanOpts, String workspace) {
        String[] versionCommand = {options.getCommand(), "-version"};
        logCommand(versionCommand);
        execute(options.getEnvVars(), versionCommand);

        // Generate a list of Strings representing the entire command to execute
        ArrayList<String> mergedCommand = new ArrayList<String>();
        mergedCommand.add(options.getCommand());
        mergedCommand.addAll(options.getArgs());
        mergedCommand.addAll(processRules(options.getRules(), options.getWorkspace()));
        mergedCommand.addAll(options.getScanOpts());

        // Convert/cast to a String[] so that it can be logged and executed
        String[] mergedCommandArray = mergedCommand.toArray(new String[mergedCommand.size()]);
        logCommand(mergedCommandArray);
        return execute(options.getEnvVars(), mergedCommandArray);
    }

    /**
     * Process the rule arguments by resolving (and optionally downloading) rulepacks
     * @param rules the string array of rulepack locations
     * @return the command arguments containing resolved rulepack locations
     */
    private List<String> processRules(List<String> rules, String workspace) {
        List<String> command = new ArrayList<String>();
        RulepackResolver resolver = new RulepackResolver(logger);
        //todo: need to make this configurable for workspace or any other user-defined directory
        //resolver.setTempDir(workspace);
        for (String rule : rules) {
            File file = resolver.resolve(rule);
            if (file != null) {
                CommandUtil.append(command, file.getAbsolutePath(), "-rules");
            }
        }
        return command;
    }

    /**
     * Executes the external cloudscan process sending stderr and stdout to the logger
     */
    private boolean execute(Map envVars, String[] command) {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.environment().putAll(envVars);
        try {
            Process process = pb.start();

            new StreamLogger(process.getErrorStream()).start();
            new StreamLogger(process.getInputStream()).start();

            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (InterruptedException e) {
            logger.log(Messages.Executor_Failure() + ": " + e.getMessage());
        } catch (IOException e) {
            logger.log(Messages.Executor_Failure() + ": " + e.getMessage());
        }
        return false;
    }

    private void logCommand(String[] command) {
        String cmd = Messages.Executor_Display_Options() + ": ";
        for (String param : command) {
            cmd = cmd + param + " ";
        }
        logger.log(cmd);
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
                    logger.log(line);
                }
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

}
