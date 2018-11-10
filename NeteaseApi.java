import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class NeteaseApi {

	private static final String URL_INFO = "https://music.163.com/weapi/cloudsearch/get/web?csrf_token=";
	private static final String URL_LINK = "https://music.163.com/weapi/song/enhance/player/url?csrf_token=";
	private static final String MODULUS = "00e0b509f6259df8642dbc35662901477df22677ec152b5ff68ace615bb7b725152b3ab17a876aea8a5aa76d2e417629ec4ee341f56135fccf695280104e0312ecbda92557c93870114af6c9d05c4f7f0c3685b7a46bee255932575cce10b424d813cfe4875d3e82047b97ddef52741d546b8e289dc6935b3ece0462db0a22b8e7";
	private static final String SECKEY = "0CoJUm6Qyw8W8jud";
	private static final String PUBKEY = "010001";
	private static final String PARAMS = "params=";
	private static final String ENCSECKEY = "&encSecKey=";
	private static final String SONG_HEAD = "{\"hlpretag\":\"\",\"hlposttag\":\"\",\"s\":\"";
	private static final String SONG_TAIL = "\",\"type\":\"1\",\"offset\":\"0\",\"total\":\"true\",\"limit\":\"30\"}";
	private static final String LINK_HEAD = "{\"ids\":\"[";
	private static final String LINK_MIDDLE = "]\",\"br\":"; //64 128 192 320 999
	private static final String LINK_TAIL = "000,\"csrf_token\":\"\"}";

	public static String getInfo(String name) {
		try {
			return getSource(URL_INFO, encrypt(SONG_HEAD + name + SONG_TAIL)).toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getLink(String id, String bit) {
		try {
			return getSource(URL_LINK, encrypt(LINK_HEAD + id + LINK_MIDDLE + bit + LINK_TAIL)).toString();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String encrypt(String text) throws UnsupportedEncodingException {
		String secKey = RandomString(16);
		String encText = aesEncrypt(aesEncrypt(text, SECKEY), secKey);
		String encSecKey = rsaEncrypt(secKey, PUBKEY, MODULUS);
		return PARAMS + URLEncoder.encode(encText, "UTF-8") + ENCSECKEY + encSecKey;
	}

	private static String RandomString(int a) {
		String b = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		StringBuffer c = new StringBuffer();
		int e;
		Random random = new Random();
		for (int d = 0; a > d; d += 1) {
			e = random.nextInt(b.length());
			c.append(b.charAt(e));
		}
		return c.toString();
	}

	private static String aesEncrypt(String text, String key) {
		try {
			IvParameterSpec iv = new IvParameterSpec("0102030405060708".getBytes("UTF-8"));
			SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
			byte[] encrypted = cipher.doFinal(text.getBytes());
			return Base64.getEncoder().encodeToString(encrypted);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String rsaEncrypt(String text, String PUBKEY, String MODULUS) {
		text = new StringBuilder(text).reverse().toString();
		BigInteger rs = new BigInteger(String.format("%x", new BigInteger(1, text.getBytes())), 16).modPow(new BigInteger(PUBKEY, 16), new BigInteger(MODULUS, 16));
		String r = rs.toString(16);
		if (r.length() >= 256) {
			return r.substring(r.length() - 256, r.length());
		} else {
			while (r.length() < 256) {
				r = 0 + r;
			}
			return r;
		}
	}

	public static StringBuffer getSource(String st, String data) {
		try {
			URL url = new URL(st);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setRequestProperty("Host", "music.163.com");
			conn.setRequestProperty("Referer", "http://music.163.com/");
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36");
			if (data != null) {
				OutputStream outwritestream = conn.getOutputStream();
				outwritestream.write(data.getBytes());
				outwritestream.flush();
				outwritestream.close();
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			StringBuffer sb = new StringBuffer();
			String line;
			while ((line = in.readLine()) != null)
				sb.append(line);
			in.close();
			return sb;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
