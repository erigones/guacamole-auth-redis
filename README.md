guacamole-auth-redis
====================

[Guacamole](http://guac-dev.org/) Redis authentication extension.


Build
-----

- Clone the [git repository](https://github.com/erigones/guacamole-auth-redis.git):

		git clone https://github.com/erigones/guacamole-auth-redis.git

- Compile. This will create a new jar file `guacamole-auth-redis-VERSION.jar` in the `target/` folder:

		cd guacamole-auth-redis
		mvn package

Install
-------

- Copy `guacamole-auth-redis-VERSION.jar` into `webapps/guacamole/WEB-INF/lib/`. It does not work if you copy the jar into common/lib/ or shared/lib/.

- Download [jedis.jar](https://github.com/xetorthio/jedis) into `webapps/guacamole/WEB-INF/lib/`.


Configure
---------

- Edit the Guacamole configuration file (`guacamole.properties`):

		# Auth provider class
		auth-provider: org.erigones.guacamole.net.auth.RedisAuthenticationProvider

		# Redis properties
		redis-host: localhost
		redis-port: 6379
		#redis-parent: myparent:
		#redis-password: secret
		#redis-timeout: 2

- Restart guacamole.


Use
---

- To create a user mapping for user "testuser" with one VNC connection named "connection1":

		redis> HSET testuser password "SecretPassw0rd"

		redis> HSET testuser connection1 "protocol=vnc\nhostname=localhost\nport=5900\npassword=VNCPASS"

- You can use redis *pipelines* for grouping operations into transactions and reducing overhead.
