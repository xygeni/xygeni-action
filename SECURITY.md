# Security Policy

## Reporting a Vulnerability

If there are any vulnerabilities in `xygeni-action`, don't hesitate to _report them_.

1. **Email us directly** at [security@xygeni.io](mailto:security@xygeni.io) with:
   - A description of the vulnerability
   - Steps to reproduce the issue
   - Potential impact assessment
   - Any suggested fixes (optional)

2. Alternatively, use any of the [private contact addresses](https://github.com/xygeni/xygeni-action#support).

3. We will evaluate the vulnerability and, if necessary, release a fix or mitigating steps to address it. 
   We will reach out to you to let you know the outcome, and will credit you in the report.

   Please **do not disclose the vulnerability publicly** until a fix is released!

4. Once we have either a) published a fix, or b) declined to address the vulnerability for whatever reason, 
   you are free to publicly disclose it.

## Security Best Practices

When using this action, we recommend:

1. **Store API tokens as GitHub Encrypted Secrets** - Never hardcode tokens in workflows.
2. **Use the latest version** - Keep the action updated to receive security fixes.
3. **Pin to a specific version** - Use exact version tags (e.g., `@v5.38.0`) rather than floating tags.
4. **Limit token permissions** - Use tokens with the minimum required permissions.

## Security Features

This action is designed with security in mind:

- **No credential exposure**: API tokens are passed via environment variables, not command-line arguments.
- **Direct download**: Scanner is downloaded directly from official Xygeni servers over HTTPS.
- **No third-party dependencies**: The composite action uses only standard shell commands.