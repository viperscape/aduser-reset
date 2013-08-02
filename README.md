# aduser

Clojure webserver to allow users to reset their password remotely.
Currently uses commons-exec to process a typical windows user reset command. 
This should likely be done away with in favor of an LDAP library.
Secure this by using a reverse proxy with nginx, etc.
Runs standalone with Jetty as an HTTP server.


# usage

The user submits a password reset request by inputting their username. 
A token is generated and emailed to the account corresponding to the username.
Tokens are in an atom and are confirmed against when the email link is clicked-- completing the authorization.


## License

Copyright © 2013

Distributed under the Eclipse Public License, the same as Clojure.
