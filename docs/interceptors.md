


# Interceptors

### TransactionInterceptor

Retrieves the transactionId from request pathVariables.
If there is not an existing transaction or an error occurs, then a HttpClientErrorException, ApiErrorResponseException or URIValidationException is thrown.

These checks to be added to all public endpoints that include the pattern/transactions/**

### OpenTransactionInterceptor

This interceptor checks the transaction exists and if so, will check that the request method is a “GET” or the transaction status is “OPEN”.

If any of these checks fail a 403 HTTP Status Code is returned.

### TokenPermissionInterceptor

This interceptor creates a token permission and stores it in the request.

### RequestPermissionsInterceptor

Every request is checked for the required permission key user_psc_verification=create.

If unauthorised, a 401 HTTP Status Code is returned.

### TransactionClosedInterceptor

This interceptor checks the transaction exists and if so, will check that the specified filing is closed, if not 403 Http Status Code is returned.

The interceptor will check all requests to 

GET private/transactions/{transaction_id}/persons-with-significant-control-verification/individual/{filing_resource_id}/filings and verify that they have the correct permissions to access this.

### LoggingInterceptor

This interceptor uses the StructuredLogging library to log out the start and end of every request into the service.

### InternalUserInterceptor

The interceptor will check all requests to 

GET private/transactions/{transaction_id}/persons-with-significant-control-verification/individual/{filing_resource_id}/filings and verify that they have the correct permissions to access this.

It verifies they have the correct internal user api key with the correct privileges. If the check fails, it returns a 401 unauthorised or 403 forbidden response.

No Identity Type present = 401 Unauthorised

Identity Type other than API key = 403 Forbidden

API key with “internal_app_privileges”: false = 403 Forbidden

API Key with “internal_app_privileges”: true = 200 OK

The interceptor will check all requests to 

GET private/transactions/{transaction_id}/persons-with-significant-control-verification/individual/{filing_resource_id}/filings




