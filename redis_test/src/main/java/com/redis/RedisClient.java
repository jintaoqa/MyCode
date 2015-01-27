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
	private Jedis jedis;//����Ƭ��ͻ�������
	private JedisPool jedisPool;//����Ƭ���ӳ�
	private ShardedJedis sharedJedis;//��Ƭ��ͻ�������
	private ShardedJedisPool sharedJedisPool;//��Ƭ���ӳ�
	
	public RedisClient()
	{
		initialPool();
		initialShardedPool();
		sharedJedis = sharedJedisPool.getResource();
		jedis  =jedisPool.getResource();
	}
	
	/**
	 * ��ʼ������Ƭ��
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
	 * ��ʼ����Ƭ��
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
		//�������
		System.out.println("��տ����������ݣ�"+jedis.flushDB());
		System.out.println("�ж�key999�Ƿ���ڣ�"+sharedJedis.exists("key999"));
		System.out.println("��������001��value001��ֵ�ԣ�"+sharedJedis.set("key001", "value001����"));
		System.out.println("�ж�key001�Ƿ���ڣ�"+sharedJedis.exists("key001"));
		
		//�������key
		System.out.println("����key002.value002��ֵ�ԣ�"+sharedJedis.set("key002", "value002����"));
		System.out.println("ϵͳ�����м����£�");
		Set<String> keys = jedis.keys("*");
		Iterator<String> it = keys.iterator();
		while(it.hasNext()){
			String key = it.next();
			System.out.println("����["+key+"],ֵ��"+sharedJedis.get(key));
		}
		//ɾ��ĳ��key����key�����ڣ�����Ը����
		System.out.println("ϵͳɾ��key002:"+jedis.del("key002"));
		System.out.println("�ж�key002�Ƿ���ڣ�"+sharedJedis.exists("key002"));
		System.out.println("����key001����ʱ��Ϊ5s"+jedis.expire("key001", 5));
		try{
			Thread.sleep(2000);
		}
		catch(InterruptedException e){
			
		}
		//�鿴ĳ��key��ʣ������ʱ�䣬��λ���롿����������򲻴��ڶ�Ϊ-1
		System.out.println("�鿴key001��ʣ������ʱ�䣺"+jedis.ttl("key001"));
		//�Ƴ�ĳ��key������ʱ��
		System.out.println("�Ƴ�ĳ��key001������ʱ�䣺"+jedis.persist("key001"));
		System.out.println("�鿴key001��ʣ������ʱ�䣺"+jedis.ttl("key001"));
		System.out.println("�鿴key���洢��ֵ�����ͣ�"+jedis.type("key001"));
	}
	
	public static void main(String[] args) {
		RedisClient redis = new RedisClient();
		redis.show();
	}
}

