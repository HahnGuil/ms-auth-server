# Business rules

- **Account per application:** an email can be used across different applications, but cannot be registered twice in the same application. Passwords are stored per user/application pair and are not shared across applications.
- **Account types:** direct users sign in with email and password. OAuth users sign in through Google and cannot use direct login or change their password through this service. OAuth users are associated with an application through the authenticated `/users/register` flow.
- **Password:** API format is 8–12 characters, including a digit, uppercase letter, and special character. Passwords are stored using BCrypt. Each application can disable expiration, use the 90-day default policy, or define expiration in months.
- **Password expiration:** applications with a policy send warnings 20, 15, 10, and 5 days before expiration. An expired password blocks access to that application; resetting or changing it clears that application's block.
- **User sessions:** one active session is maintained per user and application. A new login ends and invalidates the previous session in that application without ending sessions in other applications.
- **User tokens:** access JWTs last 15 minutes; user refresh tokens last 30 minutes. Refresh requires the correct active token and invalidates the previous token. Logout and password changes/resets also invalidate the corresponding session.
- **Recovery:** the six-digit numeric code expires after 30 minutes; a new request replaces the previous one for that user/application. The `recoverToken` returned after code validation is valid for 15 minutes and is only for password reset.
- **Applications:** only `USER_ADMIN` or `SUPER_USER` users with an active user token can register applications. The default policy is 90 days; a custom policy requires `passwordExpirationMonths >= 1`. `definePasswordPolicy=false` means no expiration.
- **Application tokens:** separate from user tokens and use their own scopes. Application access tokens last 10 hours. Application refresh expiry is currently calculated as 15 minutes in code, despite the constant name indicating hours.
- **Profiles and roles:** new users receive the normal global role and application role `USER`. `PATCH /users` changes the application role to `ADMIN` when the user is already associated.
- **JWT keys:** tokens are signed using RSA/RS256. Keys are generated at startup and rotated every two hours; old public keys remain published while the process is running. A restart generates a new key set.

These durations and rules reflect the current implementation; check the OpenAPI definition and live endpoints when integrating future versions.
