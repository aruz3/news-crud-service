JWT Authentication Security Pentest Report
Date of Testing: June 30, 2026
Target of Evaluation: REST API Authentication Service (Spring Boot, Java)
Objective: To evaluate the security resilience of JWT (JSON Web Tokens) issuance, validation, and invalidation mechanisms against classic attack vectors.
1. Executive Summary / Results Dashboard
ID
	Attack Vector
	Status / Result
	Severity Level
	Final Verdict
	Attack 1
	Unprotected alg: none usage
	Successfully Secured
	CRITICAL
	The server correctly rejects the token with 401 Unauthorized.
	Attack 2
	Algorithm Confusion (RS256 to HS256)
	Not Applicable
	Informational
	The architecture strictly relies on symmetric HS256 only.
	Attack 3
	Secret Key / Credentials Brute-force
	Successfully Secured
	Low
	Rate Limiting triggered successfully (429 Too Many Requests).
	Attack 4
	Expired Access Token usage
	Successfully Secured
	HIGH
	Access is blocked immediately upon TTL expiration (401).
	Attack 5
	Revoked Refresh Token usage
	Successfully Secured
	Medium
	Session refresh is impossible after a logout call (401).
	Attack 6
	App Secrets Leakage in Git History
	Successfully Secured
	HIGH
	No production keys or sensitive .env data exposed.
	2. Detailed Test Results
Attack 1: Unprotected Signature Algorithm (alg: none)
* Description: Attempting to bypass authentication by modifying the JWT header to specify alg: none and stripping the signature (the third part of the token). Secure libraries must reject such tokens.
* Execution: Using the jwt.io web interface (or a PowerShell script), a legitimate token's header was modified to {"alg":"none","typ":"JWT"}. The signature was removed, and the following request was sent:


HTTP
GET http://localhost:8080/api/news
Authorization: Bearer eyJhbGciOiJub25lIn0.eyJzdWIiOiJhZG1pbk...


* Result: The server rejected the request and responded with an HTTP status of 401 Unauthorized.
* Verdict: Successfully Secured (Passed).
 
![Attack 1 Result](screenshots/attack1.jpg)
Attack 2: Algorithm Confusion (HS256/RS256 Mix-up)
* Description: This attack exploits flaws where an attacker signs a token using the server's public key but forces the server to treat it as a symmetric HS256 secret key.
* Analysis: Source code review confirmed that the application's configuration is strictly hardcoded to use the symmetric HS256 algorithm based on a single environment secret (JWT_SECRET). The authentication endpoints lack any code or capability to parse or handle asymmetric RS256 keys. Therefore, this attack vector is fully mitigated by design.
* Verdict: Not Applicable.
Attack 3: Secret Key / Credentials Brute-force
* Description: Evaluating the authentication endpoints' resilience against automated credential stuffing or signature brute-forcing.
* Execution: A burst of 6 rapid, consecutive authentication requests containing invalid credentials was sent to the login endpoint.
* Result: The server successfully handled the first 5 failed attempts, after which the built-in Rate Limiting mechanism kicked in. On the 6th request, the server responded with 429 Too Many Requests, blocking further brute-force attempts.
* Verdict: Successfully Secured (Passed).


![Attack 3 Result](screenshots/attack3.jpg)


Attack 4: Expired Access Token Usage
* Description: Verifying strict enforcement of the token's lifetime expiration (exp claim) on the server side. Expired tokens must be immediately rejected.
* Execution: 1. In application.yml, the temporary parameter jwt.access-expiration-ms was reduced to 5000 (5 seconds).
* 2. The server was restarted, and a fresh access token was obtained.
* 3. After waiting 6 seconds, a request was made to the protected /api/news endpoint using this token.
* Result: The server correctly identified that the token had expired and returned 401 Unauthorized. TTL control is functioning properly.
* Verdict: Successfully Secured (Passed).
![Attack 4 Result](screenshots/attack4.jpg)
Attack 5: Revoked Refresh Token Usage
* Description: Testing the server-side behavior of the Logout mechanism. Once a user logs out, their associated refreshToken must be invalidated or blacklisted on the backend.
* Execution: 1. A valid logout request was submitted: POST /api/auth/logout.
* 2. Immediately following the logout, a session renewal attempt was made via POST /api/auth/refresh using the recently logged-out refresh token.
* Result: The server responded with 401 Unauthorized, confirming that the token had been successfully revoked in the application database and could no longer be abused.
* Verdict: Successfully Secured (Passed).


![Attack 5 Result](screenshots/attack5.jpg)


![Attack 5 Result](screenshots/attack5.1.jpg)
Attack 6: App Secrets Leakage in Git History
* Description: Scanning the Git version control repository history to ensure no hardcoded production secrets or .env configuration files were accidentally committed.
* Execution: Deep log analysis commands were executed within the project terminal:
         PowerShell
git log -p | Select-String "JWT_SECRET"
git log --all --full-history -- .env


* Result: The command output revealed only default placeholder configuration strings (JWT_SECRET=change_me_min_32_chars) and secure environment variable bindings (secret: ${JWT_SECRET}). No actual production secret keys were found in the history.
* Verdict: Successfully Secured (Passed).


![Attack 6 Result](screenshots/attack6.jpg)


3. Conclusion
The implemented JWT authentication framework demonstrated a high level of security resilience. Critical common vulnerabilities (such as accepting alg: none headers or ignoring token expiration dates) are thoroughly blocked at the security library layer. Furthermore, the session management architecture—including Rate Limiting and explicit Refresh token revocation—is fully aligned with modern OWASP best practices. The application is considered secure and ready for production deployment.
