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

import hudson.model.TaskListener;
import org.jenkinsci.plugins.fortifycloudscan.util.CommandUtil;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is called by FortifyCloudScanBuilder (the Jenkins build-step plugin).
 * Prepares the command for execution (does not actually execute cloudscan)
 *
 * @author Steve Springett (steve.springett@owasp.org)
 */
public class FortifyCloudScanExecutor implements Serializable {

    private static final long serialVersionUID = 3595913479313812273L;

    private final ConsoleLogger logger;
    private final Options options;

    /**
     * Constructs a new FortifyCloudScanExecutor object.
     *
     * @param listener BuildListener object to interact with the current build
     * @param options The options that will be used during execution
     */
    public FortifyCloudScanExecutor(TaskListener listener, Options options) {
        this.logger = new ConsoleLogger(listener);
        this.options = options;
    }

    /**
     * Given the specified options, this method will dynamically construct the
     * full command line syntax necessary to execute cloudscan.
     */
    public String prepare() {
        // Generate a list of Strings representing the entire command to execute
        ArrayList<String> mergedCommand = new ArrayList<String>();
        mergedCommand.add(options.getCommand());
        mergedCommand.addAll(options.getArgs());
        mergedCommand.addAll(processRules(options.getRules(), options.getWorkspace()));
        mergedCommand.addAll(options.getScanOpts());

        // Convert/cast to a String[] so that it can be logged and executed
        String[] command = mergedCommand.toArray(new String[mergedCommand.size()]);
        return CommandUtil.generateShellCommand(command);
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

}
