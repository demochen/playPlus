package snails.common.util.lang;

import java.util.Random;

/**
 * 
* @ClassName: GenerateCode 
* @Description: 各种code生成工具
* @author chenlinlin
* @date 2015年3月15日 下午3:43:14 
*
 */
public class GenerateCode {
   
	
	private static final Random random=new Random();
	/**
	 * 
	* @Title: generateVerifyCode 
	* @Description: 四位验证码 
	* @return    设定文件 
	* String    返回类型
	 */
	public  static String generateVerifyCode(){
		int code=(int)((Math.random()*9+1)*1000);
		//int code=random.nextInt(10000);
		return String.valueOf(code);
	}
	
	public static void main(String args[]){
		System.out.println(GenerateCode.generateVerifyCode());
	}
}
