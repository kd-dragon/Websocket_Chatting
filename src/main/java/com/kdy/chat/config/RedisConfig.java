package com.kdy.chat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableRedisRepositories
public class RedisConfig {
	
	@Value("${spring.redis.port}")
	public int port;

	@Value("${spring.redis.host}")
	public String host;
	
	@Value("${spring.redis.password}")
	public String password;
	
	@Value("${spring.redis.sentinelYn}")
	public String sentinelYn;
	
	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		
		if(sentinelYn.equals("Y")) {
			RedisSentinelConfiguration redisSentinelConfiguration = 
					new RedisSentinelConfiguration()
					.master("mymaster")
					.sentinel(host, port);
			redisSentinelConfiguration.setPassword(password);
			return new LettuceConnectionFactory(redisSentinelConfiguration);
		} else {
			if(password != null && !password.isEmpty()) {
				RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(host,port);
		        redisStandaloneConfiguration.setPassword(RedisPassword.of(password));
		        return new LettuceConnectionFactory(redisStandaloneConfiguration);
			} else {
				return new LettuceConnectionFactory(host, port);
			}
		}
	}
	
	@Bean
	public RedisMessageListenerContainer redisContainer() {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(this.redisConnectionFactory());
		return container;
	}
	
	@Bean(name="redisTemplate")
	public RedisTemplate<?, ?> redisTemplate() {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(this.redisConnectionFactory());
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
		redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
		return redisTemplate;
	}
	
}
