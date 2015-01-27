package com.redis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

public class RedisClient {
	private Jedis jedis;//非切片额客户端连接
	private JedisPool jedisPool;//非切片连接池
	private ShardedJedis sharedJedis;//切片额客户端连接
	private ShardedJedisPool sharedJedisPool;//切片连接池
	
	public RedisClient()
	{
		initialPool();
		initialShardedPool();
		sharedJedis = sharedJedisPool.getResource();
		jedis  =jedisPool.getResource();
	}
	
	/**
	 * 初始化非切片池
	 */
	private void initialPool()
	{
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxActive(20);
		config.setMaxIdle(5);
		config.setMaxWait(10001);
		config.setTestOnBorrow(false);
		
		jedisPool = new JedisPool(config,"121.41.40.147",6379);
	}
	
	/**
	 * 初始化切片池
	 */
	private void initialShardedPool()
	{
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxActive(20);
		config.setMaxIdle(5);
		config.setMaxWait(10001);
		config.setTestOnBorrow(false);
		
		List<JedisShardInfo> shards = new ArrayList<JedisShardInfo>();
		shards.add(new JedisShardInfo("121.41.40.147",6379,"master"));
		
		sharedJedisPool = new ShardedJedisPool(config, shards);
	}
	
	public void show(){
		KeyOperate();
	}
	
	private void KeyOperate(){
		System.out.println("================key=================");
		//清除数据
		System.out.println("清空库中所有数据："+jedis.flushDB());
		System.out.println("判断key999是否存在："+sharedJedis.exists("key999"));
		System.out.println("新增可以001，value001键值对："+sharedJedis.set("key001", "value001存入"));
		System.out.println("判断key001是否存在："+sharedJedis.exists("key001"));
		
		//输出所有key
		System.out.println("新增key002.value002键值对："+sharedJedis.set("key002", "value002存入"));
		System.out.println("系统中所有键如下：");
		Set<String> keys = jedis.keys("*");
		Iterator<String> it = keys.iterator();
		while(it.hasNext()){
			String key = it.next();
			System.out.println("键：["+key+"],值："+sharedJedis.get(key));
		}
		//删除某个key，若key不存在，则忽略该命令。
		System.out.println("系统删除key002:"+jedis.del("key002"));
		System.out.println("判断key002是否存在："+sharedJedis.exists("key002"));
		System.out.println("设置key001过期时间为5s"+jedis.expire("key001", 5));
		try{
			Thread.sleep(2000);
		}
		catch(InterruptedException e){
			
		}
		//查看某个key的剩余生存时间，单位【秒】。永久生存或不存在都为-1
		System.out.println("查看key001的剩余生存时间："+jedis.ttl("key001"));
		//移除某个key的生存时间
		System.out.println("移除某个key001的生存时间："+jedis.persist("key001"));
		System.out.println("查看key001的剩余生存时间："+jedis.ttl("key001"));
		System.out.println("查看key所存储的值得类型："+jedis.type("key001"));
	}
	
	public static void main(String[] args) {
		RedisClient redis = new RedisClient();
		redis.show();
	}
}

