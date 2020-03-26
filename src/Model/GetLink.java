/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author E6540
 */
public class GetLink implements Runnable {

    ArrayList<VideoObject> vObj;
    String curl;
    
    public GetLink(String c)
    {
        curl = c;
    }
    
    @Override
    public void run() {
        vObj = getListVideo(curl);
    }

    public ArrayList<VideoObject> getvObj() {
        return vObj;
    }
 
    private Map<String, String> handleCurl(String curl) {
        //Chuẩn hóa curl
        String curl_handle = ((curl.replaceAll("'", "")).substring(5)).replaceAll(" --compressed", "");
        String[] curl_exc_list = curl_handle.split("-H");

        Map<String, String> header = new HashMap<String, String>();
        header.put("URL", curl_exc_list[0]);
        for (int i = 1; i < curl_exc_list.length; i++) {
            String[] c = new String[2];
            int index = curl_exc_list[i].indexOf(58, 0);
            c[0] = curl_exc_list[i].substring(0, index);
            c[1] = curl_exc_list[i].substring(index + 1);
            header.put(c[0].trim(), c[1].trim());
        }

        return header;
    }
    
    
    
    private ArrayList<VideoObject> getListVideo(String curl) {

        Map<String, String> header = handleCurl(curl);

        ArrayList<VideoObject> listVideo = new ArrayList<>();

        //Lấy url request
        //Lặp cho đến khi không còn video nào nữa
        String linkReq = header.get("URL");
        header.remove("URL");
        long next_cursor = 0;
        long pre_cursor = 0;
        boolean has_more = true;
        while (has_more) {
            try {
                URL url = new URL(linkReq);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                for (Map.Entry<String, String> entry : header.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    conn.setRequestProperty(key, value);
                }

                //int resCode = conn.getResponseCode();
                //                        if (resCode != 200) {
                //                            return;
                //                        }
                BufferedReader bf = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder respone = new StringBuilder();
                String inputLine;
                while ((inputLine = bf.readLine()) != null) {
                    respone.append(inputLine);
                }

                JSONObject resJson = new JSONObject(respone.toString());

                JSONArray resArrJson = resJson.getJSONArray("aweme_list");
                for (int i = 0; i < resArrJson.length(); i++) {
                    JSONObject videoObject = resArrJson.getJSONObject(i);

                    //Lấy link download video
                    JSONObject videoObj = videoObject.getJSONObject("video");
                    JSONObject addrObj = videoObj.getJSONObject("download_addr");
                    JSONArray urlArr = addrObj.getJSONArray("url_list");
                    String urlDownload = (urlArr.getString(0)).replaceAll("watermark=1", "watermark=0");

                    JSONObject infoVideo = videoObject.getJSONObject("statistics");
                    int heartNums = infoVideo.getInt("digg_count");
                    int shareNums = infoVideo.getInt("share_count");
                    int cmtNums = infoVideo.getInt("comment_count");

                    String decs = videoObject.getString("desc");

                    JSONObject imgLink = videoObj.getJSONObject("origin_cover");
                    JSONArray urlImgList = imgLink.getJSONArray("url_list");
                    String urlImg = urlImgList.getString(0);
                    urlImg = urlImg.substring(0, urlImg.lastIndexOf("?"));
                    ImageIcon imgVideoPre = downloadImagePreviewVideo(urlImg);

                    String finalLinkDownload = getFinalLinkDownload(urlDownload);

                    VideoObject vdObj = new VideoObject(finalLinkDownload, heartNums, shareNums, cmtNums, decs, imgVideoPre);
                    listVideo.add(vdObj);
                }
                next_cursor = resJson.getLong("max_cursor");
                has_more = resJson.getBoolean("has_more");
                linkReq = linkReq.replaceAll("max_cursor=" + String.valueOf(pre_cursor), "max_cursor=" + String.valueOf(next_cursor));
                pre_cursor = next_cursor;

            } catch (MalformedURLException ex) {

            } catch (IOException ex) {

            }
        }

        return listVideo;
    }

    private String getFinalLinkDownload(String urlV) {
        String finalLinkdownload;
        try {
            URL url = new URL(urlV);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(30000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
            conn.setRequestProperty("Accept-Language", "en-GB,en;q=0.9,en-US;q=0.8");
            conn.setRequestProperty("Cache-Control", "no-cache");
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Host", "v16-byteoversea.muscdn.com");
            conn.setRequestProperty("Pragma", "no-cache");
            conn.setRequestProperty("Upgrade-Insecure-Requests", "1");
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.149 Mobile Safari/537.36");
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            Map<String, List<String>> map = conn.getHeaderFields();
//            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
//                String key = entry.getKey();
//                List<String> value = entry.getValue();
//                System.out.println(key+":"+value);
//            }
            finalLinkdownload = map.get("location").get(0);
            return finalLinkdownload;
        } catch (MalformedURLException ex) {
            return null;
        } catch (IOException ex) {

        }
        return null;
    }
    
        private ImageIcon downloadImagePreviewVideo(String urlImg) {
        ImageIcon image = null;
        try {
            URL url = new URL(urlImg);
            BufferedImage c = ImageIO.read(url);
            image = new ImageIcon(c);
            return image;
        } catch (MalformedURLException ex) {
            return null;
        } catch (IOException ex) {

        }
        return image;
    }

}
