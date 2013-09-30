package com.erigones.guacamole.net.auth.redis.properties;

import org.glyptodon.guacamole.properties.IntegerGuacamoleProperty;
import org.glyptodon.guacamole.properties.StringGuacamoleProperty;

public class RedisGuacamoleProperties {

    private RedisGuacamoleProperties() {}

    public static final StringGuacamoleProperty REDIS_HOST = new StringGuacamoleProperty() {
        @Override
        public String getName() { return "redis-host"; }
    };

    public static final IntegerGuacamoleProperty REDIS_PORT = new IntegerGuacamoleProperty() {
        @Override
        public String getName() { return "redis-port"; }
    };

    public static final IntegerGuacamoleProperty REDIS_TIMEOUT = new IntegerGuacamoleProperty() {
        @Override
        public String getName() { return "redis-timeout"; }
    };

    public static final StringGuacamoleProperty REDIS_PASSWORD = new StringGuacamoleProperty() {
        @Override
        public String getName() { return "redis-password"; }
    };

    public static final StringGuacamoleProperty REDIS_PARENT = new StringGuacamoleProperty() {
        @Override
        public String getName() { return "redis-parent"; }
    };
}
