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
			System.out.println(keyNbr+".����"+key+"��");
			keyNbr++;
		}
	}
	
	private void getValue(String key){
		value = shardedJedis.get(key);
		System.out.println("���redisֵ��"+value);
	}
	
	public void queryCacheData() throws InputMismatchException{
		Scanner scanner = new Scanner(System.in);
		int select;
		while(true){
			System.out.println("��ѡ��\n1.ģ����ѯ\n2.��ѯ����\n3.�������\n4.�˳�");
			select =  scanner.nextInt();
			
			switch(select){
				//ģ����ѯ
				case 1 :
					System.out.println("������ģ��ƥ��key������");
					scanner.reset();
					fuzzyMatch(scanner.next());
					
					while(true){
						System.out.println("��ѡ����Ҫ��ѯ�����,������һ��������0,��Ҫ��ӡ��������żӡ�����");
						int get = 0;
						boolean ifPrint = false;
						
						try{
							get = scanner.nextInt();
						}
						catch(InputMismatchException e){
							System.err.println("��������������������룡");
							scanner.next();
							continue;
						}
						
						if(get!=0){
							if(getKeys().size()<get){
								System.err.println("��������������������룡");
							}
							else{
								String key = getKeys().get(get-1);
								getValue(key);
								System.out.println("��Ҫ����������p��������ѯ�밴�����");
								if("p".equals(scanner.next())){
									System.out.println("�����뱣��·��");
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
				//key��ѯ
				case 2 :
					while(true){

						System.out.println("�������ѯ��key������");
						scanner.reset();
						String key = scanner.next();
						getValue(key);
						System.out.println("������ѯ������0,�����밴���⡣����");
						
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
				//�建��
				case 3 :
					System.out.println("ȷ��������[Y/N]");
					String flag = scanner.next();
					if("Y".equals(flag) || "y".equals(flag)){
						System.out.println("����������档����");
						jedis.flushDB();
						System.out.println("���������ϣ�");
					}
					
					break;
				
				case 4 :
					jedis.quit();
					
					break;
					
				default :
					System.out.println("������������������");
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
			System.out.println("������������������");
			redis4GTest.queryCacheData();
		}
		catch(JedisConnectionException e1){
			System.out.println("���ӳ�ʱ���������½������ӡ�����");
			catchException();
		}
	}
	
	public static void main(String[] args) 
	{
		new Redis4GTest().catchException();
	}
}
