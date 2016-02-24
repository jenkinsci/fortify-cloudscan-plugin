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

import com.fortifysoftware.schema.wsTypes.Project;
import com.fortifysoftware.schema.wsTypes.ProjectVersionLite;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.security.MasterToSlaveCallable;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.fortifycloudscan.util.CommandUtil;
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
import java.util.regex.Pattern;

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
    private final boolean disableSnippets;
    private final boolean quick;
    private final String rules;
    private final String workers;


    @DataBoundConstructor // Fields in config.jelly must match the parameter names
    public FortifyCloudScanBuilder(String buildId, String xmx, String buildLabel, String buildProject,
                                   String buildVersion, Boolean useSsc, String sscToken, String upToken,
                                   String versionId, String scanArgs, String filter, Boolean noDefaultRules,
                                   Boolean disableSourceRendering, Boolean disableSnippets, Boolean quick,
                                   String rules, String workers) {

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
        this.disableSnippets = (disableSnippets != null) && disableSnippets;
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
     * Retrieves value of disable snippets. This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public Boolean getDisableSnippets() {
        return disableSnippets;
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

        final EnvVars env = build.getEnvironment(listener);
        final String[] args = generateArgs(build, listener);
        final String[] rules = preProcessRules(build, listener);
        final String[] scanOpts = generateScanOptions(build, listener);

        return launcher.getChannel().call(new MasterToSlaveCallable<Boolean, IOException>() {
            public Boolean call() throws IOException {
                final FortifyCloudScanExecutor executor = new FortifyCloudScanExecutor(listener);
                return executor.perform(env, args, rules, scanOpts);
            }
        });
    }

    /**
     * Generate Options from build configuration preferences that will be passed to
     * the build step in fortifycloudscan
     * @param build an AbstractBuild object
     * @return fortifycloudscan Arguments
     */
    private String[] generateArgs(AbstractBuild build, BuildListener listener) {
        List<String> command = new ArrayList<String>();

        // Check if the path to the cloudscan executable was specified
        String exePath = substituteVariable(build, listener, this.getDescriptor().getExePath());
        if (StringUtils.isNotBlank(exePath)) {
            command.add(exePath);
        } else {
            // Path was not specified, so just default to running 'cloudscan'. Must be in the path.
            command.add("cloudscan");
        }
        if (useSsc) {
            CommandUtil.append(command, substituteVariable(build, listener, this.getDescriptor().getSscUrl()), "-sscurl");
            CommandUtil.append(command, substituteVariable(build, listener, sscToken), "-ssctoken");
            CommandUtil.append(command, null, "start");
            CommandUtil.append(command, null, "-upload");
            CommandUtil.append(command, substituteVariable(build, listener, versionId), "-versionid");
            CommandUtil.append(command, substituteVariable(build, listener, upToken), "-uptoken");
        } else {
            CommandUtil.append(command, substituteVariable(build, listener, this.getDescriptor().getControllerUrl()), "-url");
            CommandUtil.append(command, null, "start");
        }
        /* Populate CloudScan START command */
        CommandUtil.append(command, substituteVariable(build, listener, buildId), "-b");
        CommandUtil.append(command, substituteVariable(build, listener, filter), "-filter");
        CommandUtil.append(command, noDefaultRules, "-no-default-rules");

        Object[] objectList = command.toArray();
        return Arrays.copyOf(objectList, objectList.length, String[].class);
    }

    /**
     * Pre processes the rules field by separating multiple rules and performing
     * environment variable substitution if necessary.
     * @return a string array of zero or more rule paths
     */
    private String[] preProcessRules(AbstractBuild build, BuildListener listener) {
        String[] paths = rules.split("\t|\n|\r|,");
        for (int i=0; i<paths.length; i++) {
            paths[i] = substituteVariable(build, listener, paths[i]);
        }
        return paths;
    }

    /**
     * Generate Scan Options from build configuration preferences that will be passed to
     * the build step in fortifycloudscan
     * @param build an AbstractBuild object
     * @return fortifycloudscan Options
     */
    private String[] generateScanOptions(AbstractBuild build, BuildListener listener) {
        List<String> command = new ArrayList<String>();

        CommandUtil.append(command, null, "-scan");
        /* Populate SCA Scan Arguments */
        // Everthing appearing after -scan are parameters specific to sourceanalyzer
        CommandUtil.append(command, substituteVariable(build, listener, xmx), "-Xmx", true);
        CommandUtil.append(command, substituteVariable(build, listener, buildLabel), "-build-label");
        CommandUtil.append(command, substituteVariable(build, listener, buildProject), "-build-project");
        CommandUtil.append(command, substituteVariable(build, listener, buildVersion), "-build-version");
        CommandUtil.append(command, substituteVariable(build, listener, scanArgs), scanArgs);
        CommandUtil.append(command, disableSourceRendering, "-disable-source-rendering");
        CommandUtil.append(command, disableSnippets, "-Dcom.fortify.sca.FVDLDisableSnippets=true");
        CommandUtil.append(command, quick, "-quick");
        CommandUtil.append(command, substituteVariable(build, listener, workers), "-j");

        Object[] objectList = command.toArray();
        return Arrays.copyOf(objectList, objectList.length, String[].class);
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

        /**
         * Specifies the global SSC token
         */
        private String globalSscToken;

        /**
         * Precompiled RegEx validation patterns
         */
        private static final Pattern PATTERN_VERSION_ID = Pattern.compile("^[0-9]{5}?$");
        private static final Pattern PATTERN_UUID = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
        private static final Pattern PATTERN_MEMORY = Pattern.compile("^[0-9]*(g|G|m|M)$");

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
            return checkUrl(value);
        }

        public FormValidation doCheckControllerUrl(@QueryParameter String value) {
            return checkUrl(value);
        }

        public FormValidation doCheckExePath(@QueryParameter String value) {
            return checkPath(value);
        }

        /**
         * Performs input validation when submitting the global config
         * @param value The value of the URL as specified in the global config
         * @return a FormValidation object
         */
        private FormValidation checkUrl(String value) {
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
        private FormValidation checkPath(String value) {
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

        public FormValidation doCheckXmx(@QueryParameter String value) {
            if (PATTERN_MEMORY.matcher(value).matches()) {
                return FormValidation.ok();
            } else {
                return FormValidation.error("Xmx is not in the correct format.");
            }
        }

        public FormValidation doCheckUpToken(@QueryParameter String value) {
            return checkToken(value);
        }

        public FormValidation doCheckSscToken(@QueryParameter String value) {
            return checkToken(value);
        }

        private FormValidation checkToken(String value) {
            if (PATTERN_UUID.matcher(value).matches()) {
                return FormValidation.ok();
            } else {
                return FormValidation.error("Token is not in the correct format.");
            }
        }

        public FormValidation doCheckVersionId(@QueryParameter String value) {
            if (PATTERN_VERSION_ID.matcher(value).matches()) {
                return FormValidation.ok();
            } else {
                return FormValidation.error("Project Version ID not in the correct format.");
            }
        }

        /**
         * Performs a lookup of all Projects defined in SSC.
         * @return a ListBoxModel which will be rendered in the select dropdown.
         */
        public ListBoxModel doFillProjectItems() {
            if (StringUtils.isBlank(this.sscUrl) || StringUtils.isBlank(this.globalSscToken)) {
                return null;
            }
            ListBoxModel m = new ListBoxModel();
            try {
                FortifySsc ssc = new FortifySsc(new URL(this.sscUrl + "/fm-ws/services"), this.globalSscToken);
                List<Project> projects = ssc.getProjects();
                m.add("---- " + Messages.select() + " ---- ", "");
                for (Project project : projects) {
                    m.add(project.getName(), String.valueOf(project.getId()));
                }
            } catch (Exception e) {
                m.add(Messages.sscfailure(), e.getMessage());
            }
            return m;
        }

        /**
         * Performs a lookup of all Versions for the specified Project.
         * @param project The ID of the Project to lookup
         * @return a ListBoxModel which will be rendered in the select dropdown.
         */
        public ListBoxModel doFillProjectVersionItems(@QueryParameter String project) {
            if (StringUtils.isBlank(this.sscUrl) || StringUtils.isBlank(this.globalSscToken)) {
                return null;
            }
            ListBoxModel m = new ListBoxModel();
            if (project == null || project.equals("")) {
                m.add("", "");
                return m;
            }
            try {
                FortifySsc ssc = new FortifySsc(new URL(this.sscUrl + "/fm-ws/services"), this.globalSscToken);
                List<ProjectVersionLite> projectVersions = ssc.getActiveProjectVersions(Long.valueOf(project));
                m.add("---- " + Messages.select() + " ---- ", "");
                for (ProjectVersionLite projectVersion : projectVersions) {
                    m.add(projectVersion.getName(), String.valueOf(projectVersion.getId()));
                }
            } catch (Exception e) {
                m.add(Messages.sscfailure(), e.getMessage());
            }
            return m;
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
            sscUrl = formData.getString("sscUrl").replaceAll("/$", ""); // remove trailing slash if present
            controllerUrl = formData.getString("controllerUrl").replaceAll("/$", ""); // remove trailing slash if present
            globalSscToken = formData.getString("globalSscToken");
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

        /**
         * Returns the global SSC token.
         */
        public String getGlobalSscToken() {
            return globalSscToken;
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
