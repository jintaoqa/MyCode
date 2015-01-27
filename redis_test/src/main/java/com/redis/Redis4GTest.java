package com.redis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class Redis4GTest {
	private Jedis jedis;
	private ShardedJedis shardedJedis;
	private List<String> keysList = new ArrayList<String>();
	private String value;
	
	public List<String> getKeys() {
		return keysList;
	}

	public void setKeys(List<String> keys) {
		this.keysList = keys;
	}

	public Redis4GTest()
	{
		JedisShardInfo jedisShardInfo = new JedisShardInfo("10.128.90.2", 6379);
		List<JedisShardInfo> jedisShardInfoList = new ArrayList<JedisShardInfo>();
		jedisShardInfo.setPassword("A07L22R07y10qL");
		jedisShardInfoList.add(jedisShardInfo);

		jedis= new Jedis(jedisShardInfo);
		jedis.auth("A07L22R07y10qL");
		shardedJedis = new ShardedJedis(jedisShardInfoList);
	}
	
	private void fuzzyMatch(String input)
	{
		Set<String> keys = jedis.keys("*"+input+"*");
		Iterator<String> it = keys.iterator();
		keysList.addAll(keys);
		int keyNbr = 1;
		
		while(it.hasNext()){
			String key = it.next();
			System.out.println(keyNbr+".键【"+key+"】");
			keyNbr++;
		}
	}
	
	private void getValue(String key){
		value = shardedJedis.get(key);
		System.out.println("获得redis值："+value);
	}
	
	public void queryCacheData() throws InputMismatchException{
		Scanner scanner = new Scanner(System.in);
		int select;
		while(true){
			System.out.println("请选择：\n1.模糊查询\n2.查询缓存\n3.清除缓存\n4.退出");
			select =  scanner.nextInt();
			
			switch(select){
				//模糊查询
				case 1 :
					System.out.println("请输入模糊匹配key。。。");
					scanner.reset();
					fuzzyMatch(scanner.next());
					
					while(true){
						System.out.println("请选择需要查询的序号,返回上一级请输入0,需要打印请输入序号加。。。");
						int get = 0;
						boolean ifPrint = false;
						
						try{
							get = scanner.nextInt();
						}
						catch(InputMismatchException e){
							System.err.println("输入序号有误请重新输入！");
							scanner.next();
							continue;
						}
						
						if(get!=0){
							if(getKeys().size()<get){
								System.err.println("输入序号有误请重新输入！");
							}
							else{
								String key = getKeys().get(get-1);
								getValue(key);
								System.out.println("需要保存请输入p，继续查询请按任意键");
								if("p".equals(scanner.next())){
									System.out.println("请输入保存路径");
									File file = new File(scanner.next().trim()+"vaule.txt");
									try{
										FileWriter writer = new FileWriter(file);
										writer.write(value);
										writer.flush();
									}
									catch(IOException e){
										e.printStackTrace();
									}
								}
							}
						}
						else{
							break;
						}
					}
					
					break;
				//key查询
				case 2 :
					while(true){

						System.out.println("请输入查询的key。。。");
						scanner.reset();
						String key = scanner.next();
						getValue(key);
						System.out.println("结束查询请输入0,继续请按任意。。。");
						
						try{
							if(Integer.valueOf(scanner.next())==0){
								break;
							}
						}
						catch(NumberFormatException e){
							continue;
						}
					}
					
					break;
				//清缓存
				case 3 :
					System.out.println("确认请输入[Y/N]");
					String flag = scanner.next();
					if("Y".equals(flag) || "y".equals(flag)){
						System.out.println("正在清除缓存。。。");
						jedis.flushDB();
						System.out.println("缓存清除完毕！");
					}
					
					break;
				
				case 4 :
					jedis.quit();
					
					break;
					
				default :
					System.out.println("输入有误请重新输入");
			}
			
			if(select==4){
				break;
			}
		}
	}
	
	public void catchException() {
		Redis4GTest redis4GTest = new Redis4GTest();
		try{
			redis4GTest.queryCacheData();
		}
		catch(InputMismatchException e){
			System.out.println("输入有误请重新输入");
			redis4GTest.queryCacheData();
		}
		catch(JedisConnectionException e1){
			System.out.println("连接超时，正在重新建立连接。。。");
			catchException();
		}
	}
	
	public static void main(String[] args) 
	{
		new Redis4GTest().catchException();
	}
}
