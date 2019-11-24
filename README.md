[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins/fortify-cloudscan-plugin/master)](https://ci.jenkins.io/job/Plugins/job/fortify-cloudscan-plugin)
[![License](https://img.shields.io/badge/license-Apache%202.0-brightgreen.svg)][License]

# Fortify CloudScan Jenkins Plugin
Fortify CloudScan allows an organization to host their own internal cloud-based infrastructure of Static Code Analyzer (SCA) machines that are distributed jobs by a centralized controller and optionally integrated with Software Security Center (SSC). CloudScan is included with Fortify 4.30 and higher and was an optional component in previous versions of Fortify.

This plugin provides simple configuration of CloudScan jobs without sacrificing the flexibility of performing custom scan jobs.

#### Deprecation Notice

With the launch of Fortify 19.2 in November 2019, Micro Focus now supports Scan Central (aka CloudScan) in the Jenkins plugin. The official support goes beyond the capabilities offered in this plugin and as a result, this plugin is being deprecated. As of November 24 2019, this plugin will no longer be maintained. Please consider migrating to the official [Fortify plugin](https://plugins.jenkins.io/fortify). 

### Usage

#### Step 1 - Configure Fortify CloudScan global parameters

![global configuration](https://raw.githubusercontent.com/jenkinsci/fortify-cloudscan-plugin/master/docs/images/global-config.png)

Add the URL to Fortify CloudScan and to Software Security Center (SSC). Using SSC is optional but recommended. When SSC is used, the controllers URL will be resolved from SSC. However, scans can also be sent directly to the controller without passing through SSC. When using SSC, a token will be required for authentication. This token is only used for displaying projects and project versions in the job configuration. The token should be assigned to a user with the ability to read all projects and versions from SSC.

#### Step 2 - Configure job to invoke Fortify CloudScan

![job configuration](https://raw.githubusercontent.com/jenkinsci/fortify-cloudscan-plugin/master/docs/images/job-config.png)

Fill out the required Build ID field and optionally the other fields. When using SSC, the plugin can perform lookups for project version id's based on the selected project and version retrieved dynamically from SSC.

Selected the 'Advanced' button will provide configuration for commonly used advanced parameters. If custom parameters are required, use the 'Advanced Scan Arguments' textbox.

![advanced job configuration](https://raw.githubusercontent.com/jenkinsci/fortify-cloudscan-plugin/master/docs/images/job-config-advanced.png)

## Copyright & License
Fortify CloudScan Jenkins Plugin is Copyright (c) Steve Springett. All Rights Reserved.

Fortify and Fortify CloudScan are Copyright (c) Micro Focus All Rights Reserved.

Permission to modify and redistribute is granted under the terms of the Apache 2.0 license. See the [LICENSE] file for the full license.

[license]: https://github.com/jenkinsci/fortify-cloudscan-plugin/blob/master/LICENSE.txt
