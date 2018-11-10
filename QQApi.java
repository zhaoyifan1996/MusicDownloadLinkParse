import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QQApi {

	private static final String QQ_Search = "https://c.y.qq.com/soso/fcgi-bin/client_search_cp?new_json=1&aggr=1&cr=1&p=1&n=30&w=";
	private static final String QQ_Head = "http://dl.stream.qqmusic.qq.com/";
	private static final String QQ_KEY = "https://c.y.qq.com/base/fcgi-bin/fcg_music_express_mobile3.fcg?cid=205361747&songmid=003a1tne1nSz1Y&filename=C400003a1tne1nSz1Y.m4a&guid=";

	public static String getInfo(String name) {
		String st;
		try {
			st = getSource(QQ_Search + URLEncoder.encode(name, "UTF-8"));
			return st.substring(9, st.length() - 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getLink(String format, String id, String vkey, String guid) {
		switch (format) {
		case "AAC/48K":
			return QQ_Head + "C200" + id + ".m4a?vkey=" + vkey + "&fromtag=8&guid=" + guid;
		case "AAC/96K":
			return QQ_Head + "C400" + id + ".m4a?vkey=" + vkey + "&fromtag=8&guid=" + guid;
		case "MP3/128K":
			return QQ_Head + "M500" + id + ".mp3?vkey=" + vkey + "&fromtag=8&guid=" + guid;
		case "AAC/192K":
			return QQ_Head + "C600" + id + ".m4a?vkey=" + vkey + "&fromtag=8&guid=" + guid;
		case "OGG/192K":
			return QQ_Head + "G600" + id + ".ogg?vkey=" + vkey + "&fromtag=8&guid=" + guid;
		case "MP3/320K":
			return QQ_Head + "M800" + id + ".mp3?vkey=" + vkey + "&fromtag=8&guid=" + guid;
		case "APE":
			return QQ_Head + "A000" + id + ".ape?vkey=" + vkey + "&fromtag=8&guid=" + guid;
		case "FLAC":
			return QQ_Head + "F000" + id + ".flac?vkey=" + vkey + "&fromtag=8&guid=" + guid;
		default:
			return QQ_Head + "C100" + id + "?fromtag=0&guid=" + guid;
		}
	}

	public static String getGUID() {
		return String.valueOf((int) (Math.random() * 2147483647));
	}

	public static String getKey(String guid) {
		String st = getSource(QQ_KEY + guid);
		Matcher vkey = Pattern.compile("(?<=vkey\":\")[^\"]+").matcher(st);
		while (vkey.find()) {
			return vkey.group();
		}
		return null;
	}

	public static String getSource(String u) {
		try {
			URL url = new URL(u);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("Referer", "https://y.qq.com/portal/profile.html");
			conn.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36");
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			StringBuffer sb = new StringBuffer();
			String line;
			while ((line = in.readLine()) != null)
				sb.append(line);
			in.close();
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
