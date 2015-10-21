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

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import jenkins.security.MasterToSlaveCallable;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The fortifycloudscan builder class provides the ability to invoke a fortifycloudscan build as
 * a Jenkins build step. This class takes the configuration from the UI, creates options from
 * them and passes them to the FortifyCloudScanExecutor for the actual execution of the
 * fortifycloudscan Engine and ReportGenerator.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 */
@SuppressWarnings("unused")
public class FortifyCloudScanBuilder extends Builder implements Serializable {

    private static final long serialVersionUID = 5441945995905689815L;

    private final String buildId;
    private final String xmx;
    private final String buildLabel;
    private final String buildProject;
    private final String buildVersion;
    private final boolean useSsc;
    private final String sscToken;
    private final String upToken;
    private final String versionId;
    private final String scanArgs;
    private final String filter;
    private final boolean noDefaultRules;
    private final boolean disableSourceRendering;
    private final boolean quick;
    private final String rules;
    private final String workers;


    @DataBoundConstructor // Fields in config.jelly must match the parameter names
    public FortifyCloudScanBuilder(String buildId, String xmx, String buildLabel, String buildProject,
                                   String buildVersion, Boolean useSsc, String sscToken, String upToken,
                                   String versionId, String scanArgs, String filter, Boolean noDefaultRules,
                                   Boolean disableSourceRendering, Boolean quick, String rules, String workers) {

        this.buildId = buildId;
        this.xmx = xmx;
        this.buildLabel = buildLabel;
        this.buildProject = buildProject;
        this.buildVersion = buildVersion;
        this.useSsc = (useSsc != null) && useSsc;
        this.sscToken = sscToken;
        this.upToken = upToken;
        this.versionId = versionId;
        this.scanArgs = scanArgs;
        this.filter = filter;
        this.noDefaultRules = (noDefaultRules != null) && noDefaultRules;
        this.disableSourceRendering = (disableSourceRendering != null) && disableSourceRendering;
        this.quick = (quick != null) && quick;
        this.rules = rules;
        this.workers = workers;
    }

    /**
     * Retrieves the build id. This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public String getBuildId() {
        return buildId;
    }

    /**
     * Retrieves the XMX. This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public String getXmx() {
        return xmx;
    }

    /**
     * Retrieves the build label. This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public String getBuildLabel() {
        return buildLabel;
    }

    /**
     * Retrieves the build project. This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public String getBuildProject() {
        return buildProject;
    }

    /**
     * Retrieves the build version. This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public String getBuildVersion() {
        return buildVersion;
    }

    /**
     * Retrieves if SSC should be used. This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public boolean getUseSsc() {
        return useSsc;
    }

    /**
     * Retrieves the SSC Token will use. This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public String getSscToken() {
        return sscToken;
    }

    /**
     * Retrieves the Uptoken. This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public String getUpToken() {
        return upToken;
    }

    /**
     * Retrieves the project version id. This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public String getVersionId() {
        return versionId;
    }

    /**
     * Retrieves the scan arguments. This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public String getScanArgs() {
        return scanArgs;
    }

    /**
     * Retrieves the filter. This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public String getFilter() {
        return filter;
    }

    /**
     * Retrieves value of no default rules. This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public Boolean getNoDefaultRules() {
        return noDefaultRules;
    }

    /**
     * Retrieves value of disable source rendering. This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public Boolean getDisableSourceRendering() {
        return disableSourceRendering;
    }

    /**
     * Retrieves value of quick scan. This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public Boolean getQuick() {
        return quick;
    }

    /**
     * Retrieves the rules. This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public String getRules() {
        return rules;
    }

    /**
     * Retrieves the number of workers. This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public String getWorkers() {
        return workers;
    }

    /**
     * This method is called whenever the build step is executed.
     *
     * @param build    A Build object
     * @param launcher A Launcher object
     * @param listener A BuildListener object
     * @return A true or false value indicating if the build was successful or if it failed
     */
   @Override
    public boolean perform(final AbstractBuild build, final Launcher launcher, final BuildListener listener)
            throws InterruptedException, IOException {

       final String[] args = generateArgs(build, listener);

       return launcher.getChannel().call(new MasterToSlaveCallable<Boolean, IOException>() {
           public Boolean call() throws IOException {
               final FortifyCloudScanExecutor executor = new FortifyCloudScanExecutor(args, listener);
               return executor.perform();
           }
       });
    }

    /**
     * Generate Options from build configuration preferences that will be passed to
     * the build step in fortifycloudscan
     * @param build an AbstractBuild object
     * @return fortifycloudscan Options
     */
    private String[] generateArgs(AbstractBuild build, BuildListener listener) {
        List<String> command = new ArrayList<String>();

        // Check if the path to the cloudscan executable was specified
        String exePath = this.getDescriptor().getExePath();
        if (StringUtils.isNotBlank(exePath)) {
            command.add(exePath);
        } else {
            // Path was not specified, so just default to running 'cloudscan'. Must be in the path.
            command.add("cloudscan");
        }
        if (useSsc) {
            append(command, this.getDescriptor().getSscUrl(), "-sscurl");
            append(command, sscToken, "-ssctoken");
            append(command, null, "start");
            append(command, null, "-upload");
            append(command, versionId, "-versionid");
            append(command, upToken, "-uptoken");
        } else {
            append(command, this.getDescriptor().getControllerUrl(), "-url");
            append(command, null, "start");
        }
        append(command, buildId, "-b");
        append(command, null, "-scan");
        // Everthing appearing after -scan are parameters specific to sourceanalyzer
        append(command, xmx, "-Xmx");
        append(command, buildLabel, "-build-label");
        append(command, buildProject, "-build-project");
        append(command, buildVersion, "-build-version");
        append(command, scanArgs, scanArgs);
        append(command, filter, "-filter");
        append(command, noDefaultRules, "-no-default-rules");
        append(command, disableSourceRendering, "-disable-source-rendering");
        append(command, quick, "-quick");
        append(command, rules, "-rules");
        append(command, workers, "-j");

        Object[] objectList = command.toArray();
        return Arrays.copyOf(objectList, objectList.length, String[].class);
    }

    /**
     * Add arguments to the stack based on the type of parameter being added.
     */
    private void append(List<String> command, Object confItem, String arg) {
        if (confItem == null && arg != null) {
            command.add(arg);
        }
        if (confItem instanceof String) {
            String value = (String)confItem;
            if (StringUtils.isNotBlank(value)) {
                command.add(arg);
                command.add(value);
            }
        } else if (confItem instanceof Boolean) {
            boolean value = (Boolean)confItem;
            if (value) {
                command.add(arg);
            }
        }
    }

    /**
     * A Descriptor Implementation.
     */
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * Descriptor for {@link FortifyCloudScanBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     * <p/>
     * <p/>
     * See <tt>src/main/resources/org/jenkinsci/plugins/fortifycloudscan/FortifyCloudScanBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        /**
         * Specifies the path and filename to the CloudScan executable
         */
        private String exePath;

        /**
         * Specifies the URL to Software Security Center
         */
        private String sscUrl;

        /**
         * Specifies the URL to CloudScan Controller
         */
        private String controllerUrl;


        public DescriptorImpl() {
            super(FortifyCloudScanBuilder.class);
            load();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This name is used on the build configuration screen.
         */
        public String getDisplayName() {
            return Messages.Builder_Name();
        }

        public FormValidation doCheckSscUrl(@QueryParameter String value) {
            return doCheckUrl(value);
        }

        public FormValidation doCheckControllerUrl(@QueryParameter String value) {
            return doCheckUrl(value);
        }

        public FormValidation doCheckExePath(@QueryParameter String value) {
            return doCheckPath(value);
        }

        /**
         * Performs input validation when submitting the global config
         * @param value The value of the URL as specified in the global config
         * @return a FormValidation object
         */
        private FormValidation doCheckUrl(@QueryParameter String value) {
            if (StringUtils.isBlank(value)) {
                return FormValidation.ok();
            }
            try {
                new URL(value);
            } catch (MalformedURLException e) {
                return FormValidation.error("The specified value is not a valid URL");
            }
            return FormValidation.ok();
        }

        /**
         * Performs input validation when submitting the global config
         * @param value The value of the path as specified in the global config
         * @return a FormValidation object
         */
        private FormValidation doCheckPath(@QueryParameter String value) {
            if (StringUtils.isBlank(value)) {
                return FormValidation.ok();
            }
            try {
                final FilePath filePath = new FilePath(new File(value));
                filePath.exists();
            } catch (Exception e) {
                return FormValidation.error("The specified value is not a valid path");
            }
            return FormValidation.ok();
        }

        /**
         * Takes the /apply/save step in the global config and saves the JSON data.
         * @param req the request
         * @param formData the form data
         * @return a boolean
         * @throws FormException
         */
        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            exePath = formData.getString("exePath");
            sscUrl = formData.getString("sscUrl");
            controllerUrl = formData.getString("controllerUrl");
            save();
            return super.configure(req, formData);
        }

        /**
         * This method returns the global configuration for exePath.
         */
        public String getExePath() {
            return exePath;
        }

        /**
         * Returns the global configuration to determine for sscUrl.
         */
        public String getSscUrl() {
            return sscUrl;
        }

        /**
         * Returns the global configuration to determine for controllerUrl.
         */
        public String getControllerUrl() {
            return controllerUrl;
        }
    }

    /**
     * Replace a Jenkins environment variable in the form ${name} contained in the
     * specified String with the value of the matching environment variable.
     */
    protected String substituteVariable(AbstractBuild build, BuildListener listener, String parameterizedValue) {
        try {
            if (parameterizedValue != null && parameterizedValue.contains("${")) {
                final int start = parameterizedValue.indexOf("${");
                final int end = parameterizedValue.indexOf("}", start);
                final String parameter = parameterizedValue.substring(start + 2, end);
                final String value = build.getEnvironment(listener).get(parameter);
                if (value == null) {
                    throw new IllegalStateException(parameter);
                }
                final String substitutedValue = parameterizedValue.substring(0, start) + value + (parameterizedValue.length() > end + 1 ? parameterizedValue.substring(end + 1) : "");
                if (end > 0) { // recursively substitute variables
                    return substituteVariable(build, listener, substitutedValue);
                } else {
                    return parameterizedValue;
                }
            } else {
                return parameterizedValue;
            }
        } catch (Exception e) {
            return parameterizedValue;
        }
    }
}
