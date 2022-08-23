# depsdoctor-action
GitHub Action for deps-doctor scanner

## Usage

This GitHub action download configure and executes depsdoctor scanner on the project directory `GITHUB_WORKSPACE` environment variable. You can see more information about default GitHub environment variables [here](https://docs.github.com/en/actions/learn-github-actions/environment-variables#default-environment-variables).

You can configure a GitHub action with the form:
```yaml
on:
  workflow_dispatch:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  deps-doctor:
    runs-on: ubuntu-latest
    name: depsdoctor-github-action
    steps:
      # To use this repository's private action,
      # you must check out the repository
      - name: Checkout
        uses: actions/checkout@v3
      - name: DepsDoctor-Scanner
        uses: wilosec/depsdoctor-action@v1
        id: Scanner
        with:
          username: ${{ secrets.SEC_U }}
          password: ${{ secrets.SEC_P }}
        env:
          USERNAME: ${{ secrets.SEC_U }}
          PASSWORD: ${{ secrets.SEC_P }}
```

Where: 
- `SEC_U` is wilosec web username.
- `SEC_P` is wilosec web password.


### Parameters

The available parameters are:

| Parameter            | Description                                                                                                                                                    | Mandatory | Default value                             |
|----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------|-------------------------------------------|
| wilosecurl           | Base URL of the wilosec server.                                                                                                                                | false     | https://api.nauik.com/deps-doctor-service |
| project              | Project name                                                                                                                                                   | false     | ${{ github.repository }}                  |
| username             | Username                                                                                                                                                       | true      |                                           |
| password             | Password                                                                                                                                                       | true      |                                           |
| run                  | Comma-separated scans to run: any, deps, secrets, misconf, codetamper, iac, compliance (all if not specified)                                                  | false     |                                           |
| include              | Include patterns, comma-separated                                                                                                                              | false     |                                           |
| exclude              | Exclude patterns, comma-separated. Example: "**/test/**"                                                                                                       | false     |                                           |
| output               | Output file template (filename will be prefixed by "SCAN"). Use "stdout" or "-" for standard output, "stderr" for standard error.                              | false     |                                           |
| format               | Output format: none, text, json, csv                                                                                                                           | false     | none                                      |
| columns              | Report columns, as --cols SCAN=col1,col2,...                                                                                                                   | false     | From config for each scan type            |
| code                 | If true, report code blocks or sensitive text, if false the code will be obfuscated                                                                            | false     | true                                      |
| conf                 | Configuration filepath template (filename will be prefixed by "SCAN")                                                                                          | false     |                                           |
| baseline             | Baseline filepath template (filename will be prefixed by "SCAN")                                                                                               | false     |                                           |
| custom_detectors_dir | Directory with custom detectors                                                                                                                                | false     |                                           |
| detectors            | SCAN=list, comma-separated list of IDs for detectors to run, severity or "all"                                                                                 | false     |                                           |
| skip_detectors       | SCAN=list, comma-separated list of IDs for detectors to ignore, or severity                                                                                    | false     |                                           |
| sbom                 | SBOM file to generate. Use "stdout" or "-" for standard output, "stderr" for standard error                                                                    | false     |                                           |
| sbom_format          | SBOM format: cyclonedx, spdx, swid                                                                                                                             | false     |                                           |
| secrets_mode         | (Secrets) scan mode: scan, diff, history                                                                                                                       | false     | scan                                      |
| standard             | (Compliance) ID of the standard to check                                                                                                                       | false     | From config file                          |
| fail_on              | When the action fails: "never" (always exit with code 0, default value), list of rules, like (severity:SEV ... shortcuts) that will force a non-zero exit code | false     | never                                     | 
| try_all_scans        | Try all scans, even after a scan failure or error                                                                                                              | false     | false                                     |

