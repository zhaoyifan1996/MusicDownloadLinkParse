import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Parse {

	private static final String KG_Search = "http://songsearch.kugou.com/song_search_v2?page=1&pagesize=30&platform=WebFilter&keyword=";
	private static final String KG_INFO_Head = "http://wwwapi.kugou.com/yy/index.php?r=play/getdata&hash=";
	private static final String KG_INFO_Tail = "&album_id=";
	private static final String KW_Search = "http://search.kuwo.cn/r.s?ft=music&rformat=json&encoding=utf8&rn=8&vipver=MUSIC_8.0.3.1&SONGNAME=";
	private static final String KW_INFO = "http://www.kuwo.cn/webmusic/st/getMuiseByRid?rid=";

	public static void get(String name, String song, String site) throws UnsupportedEncodingException {
		switch (site) {
		case "1":
			QQJson(name, song);
			break;
		case "2":
			NeteaseJson(name, song);
			break;
		case "3":
			XiamiJson(name, song);
			break;
		case "4":
			KuGouJson(name, song);
			break;
		case "5":
			KuWoJson(name, song);
			break;
		default:
			System.out.println("以下结果来自QQ音乐");
			QQJson(name, song);
			System.out.println("以下结果来自网易云音乐");
			NeteaseJson(name, song);
			System.out.println("以下结果来自虾米音乐");
			XiamiJson(name, song);
			System.out.println("以下结果来自酷狗音乐");
			KuGouJson(name, song);
			System.out.println("以下结果来自酷我音乐");
			KuWoJson(name, song);
		}
	}

	public static String getSource(String st) {
		try {
			URL url = new URL(st);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36");
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

	private static void QQJson(String a, String b) {
		String json = QQApi.getInfo(b);
		JsonObject jsonObject = (JsonObject) new JsonParser().parse(json);
		JsonArray list = jsonObject.get("data").getAsJsonObject().get("song").getAsJsonObject().get("list").getAsJsonArray();
		for (JsonElement je : list) {
			JsonObject item = je.getAsJsonObject();
			String singer = item.get("singer").getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString();
			JsonObject file = item.get("file").getAsJsonObject();
			if (singer.equals(a)) {
				String song = item.get("name").getAsString();
				String album = item.get("album").getAsJsonObject().get("name").getAsString();
				if (album.equals("空")) {
					album = "未知专辑";
				}
				String id = file.get("media_mid").getAsString();
				String guid = QQApi.getGUID();
				String vkey = QQApi.getKey(guid);
				String link = QQApi.getLink("MP3/320K", id, vkey, guid);
				System.out.println(singer + " - " + song + "  《" + album + "》  " + link);
			}
		}
	}

	private static void NeteaseJson(String a, String b) {
		JsonObject jsonObject = (JsonObject) new JsonParser().parse(NeteaseApi.getInfo(b));
		JsonArray list = jsonObject.get("result").getAsJsonObject().get("songs").getAsJsonArray();
		for (JsonElement je : list) {
			JsonObject item = je.getAsJsonObject();
			String singer = item.get("ar").getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString();
			if (singer.equals(a)) {
				String song = item.get("name").getAsString();
				String album = item.get("al").getAsJsonObject().get("name").getAsString();
				String id = item.get("id").getAsString();
				System.out.print(singer + " - " + song + ".mp3  《" + album + "》  ");
				JsonObject link = (JsonObject) new JsonParser().parse(NeteaseApi.getLink(id, "320"));
				JsonObject listitem = link.get("data").getAsJsonArray().get(0).getAsJsonObject();
				try {
					System.out.println(listitem.get("url").getAsString());
				} catch (Exception e) {
					System.out.println("网易云没有这首歌版权");
				}
			}
		}
	}

	private static void XiamiJson(String a, String b) {
		JsonObject jsonObject = (JsonObject) new JsonParser().parse(XiamiApi.getInfo(b));
		JsonArray list = jsonObject.get("data").getAsJsonObject().get("songs").getAsJsonArray();
		for (JsonElement je : list) {
			JsonObject item = je.getAsJsonObject();
			String singer = item.getAsJsonObject().get("artist_name").getAsString();
			if (singer.equals(a)) {
				String song = item.get("song_name").getAsString();
				String album = item.get("album_name").getAsString();
				System.out.print(singer + " - " + song + ".mp3  《" + album + "》  ");
				try {
					System.out.println(item.get("listen_file").getAsString());
					// System.out.println(item.get("listen_file").getAsString().replace("m128", "m320")); 64(m4a) 128 192 320(mp3) 740(wav/flac)
				} catch (Exception e) {
					System.out.println("虾米没有这首歌版权");
				}
			}
		}
	}

	private static void KuGouJson(String a, String b) throws UnsupportedEncodingException {
		JsonObject jsonObject = (JsonObject) new JsonParser()
				.parse(getSource(KG_Search + URLEncoder.encode(b, "UTF-8")));
		JsonArray list = jsonObject.get("data").getAsJsonObject().get("lists").getAsJsonArray();
		for (JsonElement je : list) {
			JsonObject item = je.getAsJsonObject();
			String singer = item.get("SingerName").getAsString();
			if (singer.equals(a)) {
				String song = item.get("SongName").getAsString();
				String album = item.get("AlbumName").getAsString();
				String hash = item.get("FileHash").getAsString();
				String albumID = item.get("AlbumID").getAsString();
				if (album.equals(""))
					System.out.print(singer + " - " + song + ".mp3  ");
				else
					System.out.print(singer + " - " + song + ".mp3  《" + album + "》  ");
				JsonObject link = (JsonObject) new JsonParser().parse(getSource(KG_INFO_Head + hash + KG_INFO_Tail + albumID));
				JsonObject listitem = link.get("data").getAsJsonObject();
				String url = listitem.get("play_url").getAsString();
				if (url.equals(""))
					System.out.println("酷狗没有这首歌版权");
				else
					System.out.println(listitem.get("play_url").getAsString());
			}
		}
	}

	private static void KuWoJson(String a, String b) throws UnsupportedEncodingException {
		JsonObject jsonObject = (JsonObject) new JsonParser()
				.parse(getSource(KW_Search + URLEncoder.encode(b, "UTF-8")).replace("'", "\""));
		JsonArray list = jsonObject.get("abslist").getAsJsonArray();
		for (JsonElement je : list) {
			JsonObject item = je.getAsJsonObject();
			String singer = item.get("ARTIST").getAsString().replaceAll("&.+?;", " ");
			if (singer.equals(a)) {
				String song = item.get("SONGNAME").getAsString().replaceAll("&.+?;", " ");
				String album = item.get("ALBUM").getAsString().replaceAll("&.+?;", " ");
				String id = item.get("MUSICRID").getAsString();
				String filename = singer + " - " + song;
				String st = getSource(KW_INFO + id);
				Matcher headMatcher = Pattern.compile("(?<=<mp3dl>)[^<]+").matcher(st);
				Matcher tailMatcher = Pattern.compile("(?<=<mp3path>)[^<]+").matcher(st);
				while (headMatcher.find() && tailMatcher.find()) {
					if (album.equals(""))
						System.out.println(filename + ".mp3  http://" + headMatcher.group() + tailMatcher.group());
					else
						System.out.println(filename + ".mp3  《" + album + "》  http://" + headMatcher.group() + tailMatcher.group());
				}
			}
		}
	}

	public static void main(String[] args) throws IOException {
		if (args.length == 0) {
			System.out.print("歌手名：");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String str = br.readLine();
			System.out.print("歌名：");
			String str2 = br.readLine();
			System.out.println("输入数字：1.QQ音乐 2.网易云音乐 3.虾米音乐 4.酷狗音乐 5.酷我音乐");
			System.out.print("平台：");
			String str3 = br.readLine();
			get(str, str2, str3);
		} else if (args.length == 2)
			get(args[0], args[1], "");
		else if (args.length == 3)
			get(args[0], args[1], args[2]);
		else
			System.exit(0);
	}
}
