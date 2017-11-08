package com.example.plugin.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * 十六进制处理工具类
 * 
 * @author Administrator
 * 
 */
public class HexUtil {

	/**
	 * 功能描述：将16进制的字符串转换为字节数组,例如有16进制字符串"12345678"
	 * 转换后的结果为：{18, 52 ,86 ,120 };
	 * 
	 * @param hex
	 *            需要转换的16进制字符串
	 * @return 以字节数组返回转换后的结果
	 */
	public static byte[] hexStringToByte(String hex) {
		int len = (hex.length() / 2);
		byte[] result = new byte[len];
		char[] achar = hex.toUpperCase().toCharArray();
		for (int i = 0; i < len; i++) {
			int pos = i * 2;
			result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
		}
		return result;
	}

	/**
	 * 功能描述：把字节数组转换为十六进制字符串，例如有字节数组
	 * byte [] data = new byte[]{18, 52 ,86 ,120 };转换之后的结果为："12 34 56 78"
	 * 
	 * @param bArray
	 *            所要进行转换的数组内容
	 * @return 返回转换后的结果，内容用空格隔开
	 */
	public static final String bytesToHexString(byte[] bArray) {
		StringBuffer sb = new StringBuffer(bArray.length);
		String sTemp;
		@SuppressWarnings("unused")
		int j = 0; // 此处定义的j用于控制每行输出的数据个??
		for (int i = 0; i < bArray.length; i++) {
			sTemp = Integer.toHexString(0xFF & bArray[i]);
			if (sTemp.length() < 2) {
				sb.append(0);
			}
			sb.append(sTemp.toUpperCase());
			j++;
		}
		return sb.toString();
	}

	/**
	 * 十六进制字符转换成十六进制字节
	 * 
	 * @param c
	 *            十六进制字符
	 * @return 返回十六进制字节
	 */
	private static byte toByte(char c) {
		byte b = (byte) "0123456789ABCDEF".indexOf(c);
		return b;
	}

	/**
	 * 将16位int转换为长度为2的byte数组
	 * 
	 * @param num
	 *            整数值
	 * @return 转换后的bytes数据
	 */
	public static byte[] int2bytes(int num) {
		byte[] b = new byte[4];
		@SuppressWarnings("unused")
		int mask = 0xff;
		for (int i = 0; i < 4; i++) {
			b[i] = (byte) (num >>> (24 - i * 8));
		}
		return b;
	}

	/**
	 * 将长度为2的byte数组转换为16位int
	 * 
	 * @param b
	 *            字节数组
	 * @return 转换后的int数据
	 */
	public static int bytes2int(byte[] b) {
		// byte[] b=new byte[]{1,2,3,4};
		int mask = 0xff;
		int temp = 0;
		int res = 0;
		for (int i = 0; i < 4; i++) {
			res <<= 8;
			temp = b[i] & mask;
			res |= temp;
		}
		return res;
	}

	/**
	 * 将长度为2的byte数组转换为16位int
	 * 
	 * @param b
	 *            byte[]
	 * @return int
	 * */
	public static int bytes2short(byte[] b) {
		// byte[] b=new byte[]{1,2,3,4};
		int mask = 0xff;
		int temp = 0;
		int res = 0;
		for (int i = 0; i < 2; i++) {
			res <<= 8;
			temp = b[i] & mask;
			res |= temp;
		}
		return res;
	}

	/**
	 * bcd码转换为字符串
	 * 
	 * @author: Administrator
	 * @param bcds bcd码数组
	 * @return 字符串
	 */
	public static String bcd2str(byte[] bcds) {
		char[] ascii = "0123456789abcdef".toCharArray();
		byte[] temp = new byte[bcds.length * 2];
		for (int i = 0; i < bcds.length; i++) {
			temp[i * 2] = (byte) ((bcds[i] >> 4) & 0x0f);
			temp[i * 2 + 1] = (byte) (bcds[i] & 0x0f);
		}
		StringBuffer res = new StringBuffer();

		for (int i = 0; i < temp.length; i++) {
			res.append(ascii[temp[i]]);
		}
		return res.toString().toUpperCase();
	}

	/**
	 * 字符串转换
	 * @param hexString 原hex字符串
	 * @return 转换后字符串
	 * <p>例:"3132333435363738"转换为"12345678"
     */
	public static String hexString2String (String hexString) {
		byte[] byteary = HexUtil.hexStringToByte(hexString);

		Charset cs = Charset.forName ("UTF-8");
		ByteBuffer bb = ByteBuffer.allocate (byteary.length);
		bb.put (byteary);
		bb.flip ();
		CharBuffer cb = cs.decode (bb);

		return cb.toString();
	}

	/**
	 * 字符串转换
	 * @param string 原字符串
	 * @return 转换后字符串
	 * <p>"12345678"转换为"3132333435363738"
     */
	public static String string2HexString(String string) {

		char[] chars = "0123456789ABCDEF".toCharArray();
		StringBuilder sb = new StringBuilder("");
		byte[] bs = string.getBytes();
		int bit;

		for (int i = 0; i < bs.length; i++) {
			bit = (bs[i] & 0x0f0) >> 4;
			sb.append(chars[bit]);
			bit = bs[i] & 0x0f;
			sb.append(chars[bit]);
		}
		return sb.toString().trim();
	}

}
