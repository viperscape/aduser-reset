# aduser

Clojure webserver to allow users to reset their password remotely.
Currently uses commons-exec to process a typical windows user reset command. 
This should likely be done away with in favor of an LDAP library.
Secure this by using a reverse proxy with nginx, etc.


## License

Copyright © 2013

Distributed under the Eclipse Public License, the same as Clojure.
