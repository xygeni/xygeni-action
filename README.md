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



