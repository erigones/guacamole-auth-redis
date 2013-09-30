package com.erigones.guacamole.net.auth.redis;


import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.io.StringReader;
import java.io.IOException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import com.erigones.guacamole.net.auth.redis.properties.RedisGuacamoleProperties;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.net.auth.Credentials;
import org.glyptodon.guacamole.net.auth.simple.SimpleAuthenticationProvider;
import org.glyptodon.guacamole.properties.GuacamoleProperty;
import org.glyptodon.guacamole.properties.GuacamoleProperties;
import org.glyptodon.guacamole.protocol.GuacamoleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RedisAuthenticationProvider extends SimpleAuthenticationProvider {

    private static Logger logger = LoggerFactory.getLogger(RedisAuthenticationProvider.class);

    private static String redisParent;
    private static JedisPool pool;

    private static final <T> T getRedisProperty(GuacamoleProperty<T> propName, T def) {
        T redisProp = null;

        try {
            redisProp = GuacamoleProperties.getProperty(propName);

            if (redisProp == null) {
                throw new GuacamoleException("Configuration parameter \""+ propName.getName() +"\" is null.");
            }
        } catch (GuacamoleException e) {
            logger.error("{}", e.getMessage());
            logger.error("Using default value of \"{}\" for configuration parameter \"{}\".", def, propName.getName());
            redisProp = def;
        }

        return redisProp;
    }

    static {
        // Create jedis pool
        pool = new JedisPool(new JedisPoolConfig(),
                getRedisProperty(RedisGuacamoleProperties.REDIS_HOST, "localhost"),
                getRedisProperty(RedisGuacamoleProperties.REDIS_PORT, 6379),
                getRedisProperty(RedisGuacamoleProperties.REDIS_TIMEOUT, 2),
                getRedisProperty(RedisGuacamoleProperties.REDIS_PASSWORD, null)
        );

        // Set key prefix
        redisParent = getRedisProperty(RedisGuacamoleProperties.REDIS_PARENT, "");
    }

    private Map<String, String> getMap(String key) {
        Jedis jedis = pool.getResource();

        try {
            logger.info("Fetching key \"{}\" from redis.", key);
            return jedis.hgetAll(key);
        } finally {
            pool.returnResource(jedis);
        }
    }

    private Map<String, GuacamoleConfiguration> getUserConfigurations(Map<String, String> cfgMap) throws GuacamoleException {
        // Authorized configuration hash map
        Map<String, GuacamoleConfiguration> configs = new HashMap<String, GuacamoleConfiguration>();

        for (Map.Entry<String, String> item : cfgMap.entrySet()) {
            // New empty configuration
            GuacamoleConfiguration config = new GuacamoleConfiguration();

            // Parse connection parameters from item value
            Properties properties = new Properties();
            try {
                properties.load(new StringReader(item.getValue()));
            } catch (IOException e) {
                throw new GuacamoleException("Error reading basic user mapping string.", e);
            }

            boolean protocol_found = false;
            // Iterate through properties
            for (String cfg_key : properties.stringPropertyNames()) {
                String cfg_val = properties.getProperty(cfg_key);

                if (cfg_key.equals("protocol")) {
                    config.setProtocol(cfg_val);
                    protocol_found = true;
                } else {
                    config.setParameter(cfg_key, cfg_val);
                }
            }

            if (protocol_found) {
                // Add user config to user config map
                configs.put(item.getKey(), config);
                protocol_found = false;
            } else {
                throw new GuacamoleException("Missing protocol in connection \""+ item.getKey() +"\".");
            }
        }

        return configs;
    }

    @Override
    public Map<String, GuacamoleConfiguration> getAuthorizedConfigurations(Credentials credentials) throws GuacamoleException {
        // We are using the username as redis key
        if (credentials.getUsername() == null) {
            return null; // Unauthorized
        }

        // Create the key
        String key = redisParent.concat(credentials.getUsername());

        // Fetch the configuration from redis
        Map<String, String> map = getMap(key);

        // Check the value
        if (map == null || map.isEmpty()) {
            logger.warn("User mapping for key \"{}\" not found in redis.", key);
            return null; // Unauthorized
        }

        // Check password
        if (! credentials.getPassword().equals(map.get("password"))) {
            logger.warn("Password mismatch in user mapping for key \"{}\".", key);
            return null;
        }

        // Remove password (everything else is a connection)
        map.remove("password");

        try {
            // Parse connections from remaining key/values in map
            return getUserConfigurations(map);

        } catch (GuacamoleException e) {
            logger.error("User mapping for key \"{}\" could not be parsed. Error was: \"{}\".", key, e.getMessage());

            // Return empty configuration map since we are already authenticated
            return new HashMap<String, GuacamoleConfiguration>();
        }
    }
}
