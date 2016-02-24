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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Options implements Serializable {

    private static final long serialVersionUID = 4339433732415820879L;

    private Map envVars;
    private String command;
    private List<String> args;
    private List<String> rules;
    private List<String> scanOpts;
    private String workspace;


    public Map getEnvVars() {
        return envVars;
    }

    public void setEnvVars(Map envVars) {
        this.envVars = envVars;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public List<String> getRules() {
        return rules;
    }

    public void setRules(List<String> rules) {
        this.rules = rules;
    }

    public List<String> getScanOpts() {
        return scanOpts;
    }

    public void setScanOpts(List<String> scanOpts) {
        this.scanOpts = scanOpts;
    }

    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

}
