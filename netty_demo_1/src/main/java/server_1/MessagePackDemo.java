package server_1;

import org.msgpack.MessagePack;

import java.io.IOException;

/**
 * @Author: xiaohuan
 * @Date: 2020-01-29 19:30
 */
public class MessagePackDemo {
	public static void serializesPOJO(){
		try {
			/** 创建序列化对象。提示：序列化是针对对象操作的
			 * User 类上必须加上 @Message 注解
			 * */
			User user = new User();
			user.setId("1");
			user.setAge(18);
			user.setName("xiaohuan");
			user.setSex("man");

			/** org.msgpack.MessagePack 是 java 开发的基本类
			 * 用于创建序列化器与反序列化器
			 */
			MessagePack messagePack = new MessagePack();

			/**序列化指定的对象为字节数组——————整个序列化过程就是如此简单，一个 API 解决
			 * 提示：这个字节数组与 java.lang.String#getBytes() 返回的字节数组是不一样的
			 * 使用 String(byte bytes[]) 相应返回是得不到正常结果的
			 * 只能再通过 MessagePack 进行反序列化得到结果*/
			byte[] raw = messagePack.write(user);
			/** read(byte[] bytes, Class<T> c)
			 * 将字节数组反序列化为指定类对象，c 指定 POJO 类即可
			 */
			User userFinal = messagePack.read(raw, User.class);
			System.out.println(userFinal);
			System.out.println(userFinal.getId() + "," + userFinal.getName());

		}catch(IOException e){
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		serializesPOJO();

	}
}
