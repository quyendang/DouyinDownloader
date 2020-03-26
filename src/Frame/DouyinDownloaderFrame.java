/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Frame;

import Model.VideoObject;
import Model.WrapLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author E6540
 */
public class DouyinDownloaderFrame extends javax.swing.JFrame {

    /**
     * Creates new form DouyinDownloaderFrame
     */
    private ArrayList<VideoObject> listVideo;
    private JLabel labelStatus;

    public DouyinDownloaderFrame() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        UIManager.put("PopupMenu.border", BorderFactory.createEmptyBorder());
        initComponents();
        panel_Video.setLayout(new WrapLayout(WrapLayout.CENTER, 5, 5));
        labelStatus = new JLabel();
        labelStatus.setForeground(Color.WHITE);
        labelStatus.setPreferredSize(new Dimension(650, 14));
        labelStatus.setFont(new Font("SansSerif", Font.BOLD, 12));
        panel_Status.add(labelStatus);
        resetPanel(panel_Status);
    }

    private void getLink() {
        listVideo = new ArrayList<>();
        String curl = txt_CurlLink.getText();
        listVideo = getListVideo(curl);
        for (VideoObject v : listVideo) {
            VideoContent video = new VideoContent(v);
            panel_Video.add(video);
        }
        resetPanel(panel_Video);
    }

    private void resetPanel(JPanel panel) {
        panel.validate();
        panel.repaint();
    }

    //====================== xử lý get link video=================
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

        labelStatus.setText("G e t     L i n k     S u c c e s s ! ! !");
        return listVideo;
    }

    //Lấy link download cuối cùng có thể xem được
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

    //tSave = true : Save video với tên theo mô tả
    //tSave = false : Save video theo số thứ tự 1 -> vobj.size();
    private void downloadVideoWithoutWatermark(String linkDownload, String filePath) {
        try {
            URL url = new URL(linkDownload);
            URLConnection urlConn = url.openConnection();
            urlConn.setConnectTimeout(30000);
            urlConn.setReadTimeout(30000);
            urlConn.setAllowUserInteraction(false);
            InputStream inputStream = urlConn.getInputStream();
            byte[] videoBuffer = new byte[512];

            File file = new File(filePath);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            int len;
            while ((len = inputStream.read(videoBuffer)) != -1) {
                fileOutputStream.write(videoBuffer, 0, len);
            }
            //Close the file stream
            fileOutputStream.close();

            //Done message
            System.out.println("Video Downloaded!");
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    //Lọc video theo điều kiện
    private ArrayList<VideoObject> fillterVideo(ArrayList<VideoObject> listVObj, Map<String, Integer> reqr) {
        ArrayList<VideoObject> filltedVideo = new ArrayList<>();
        for (VideoObject vobj : listVObj) {
            if (vobj.getCmtNums() >= reqr.get("cmt") && vobj.getHeartNums() >= reqr.get("heart") && vobj.getShareNums() >= reqr.get("share")) {
                filltedVideo.add(vobj);
            }
        }
        return filltedVideo;
    }
    //======================kết thúc xử lý get link video========================

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        panel_main = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        txt_CurlLink = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        spinner_comment = new javax.swing.JSpinner();
        spinner_heart = new javax.swing.JSpinner();
        spinner_share = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        panel_Status = new javax.swing.JPanel();
        scollPanel_Video = new javax.swing.JScrollPane();
        panel_Video = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMaximumSize(new java.awt.Dimension(900, 600));
        setResizable(false);

        jSplitPane1.setBackground(new java.awt.Color(44, 62, 80));
        jSplitPane1.setDividerSize(2);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setMaximumSize(new java.awt.Dimension(900, 650));
        jSplitPane1.setMinimumSize(new java.awt.Dimension(900, 650));
        jSplitPane1.setPreferredSize(new java.awt.Dimension(900, 650));

        panel_main.setBackground(new java.awt.Color(44, 62, 80));
        panel_main.setForeground(new java.awt.Color(255, 255, 255));
        panel_main.setPreferredSize(new java.awt.Dimension(900, 650));

        jLabel2.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("CURL");

        txt_CurlLink.setBackground(new java.awt.Color(52, 73, 94));
        txt_CurlLink.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        txt_CurlLink.setForeground(new java.awt.Color(255, 255, 255));
        txt_CurlLink.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        txt_CurlLink.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        txt_CurlLink.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        txt_CurlLink.setDisabledTextColor(new java.awt.Color(255, 255, 255));
        txt_CurlLink.setMargin(new java.awt.Insets(2, 10, 2, 2));

        jButton1.setBackground(new java.awt.Color(255, 255, 255));
        jButton1.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        jButton1.setText("GET LINK");
        jButton1.setToolTipText("Get Link Download Video");
        jButton1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        spinner_comment.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N

        spinner_heart.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N

        spinner_share.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N

        jLabel1.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Heart");

        jLabel3.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Share");

        jLabel4.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Comment");

        jButton2.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        jButton2.setText("Fillter Video");
        jButton2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jButton3.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        jButton3.setText("Download All");
        jButton3.setToolTipText("Download All Video");
        jButton3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        panel_Status.setBackground(new java.awt.Color(44, 62, 80));
        panel_Status.setMaximumSize(new java.awt.Dimension(670, 14));
        panel_Status.setMinimumSize(new java.awt.Dimension(670, 14));
        panel_Status.setPreferredSize(new java.awt.Dimension(670, 14));
        panel_Status.setLayout(new javax.swing.BoxLayout(panel_Status, javax.swing.BoxLayout.PAGE_AXIS));

        javax.swing.GroupLayout panel_mainLayout = new javax.swing.GroupLayout(panel_main);
        panel_main.setLayout(panel_mainLayout);
        panel_mainLayout.setHorizontalGroup(
            panel_mainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel_mainLayout.createSequentialGroup()
                .addGroup(panel_mainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panel_mainLayout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(jLabel2)
                        .addGap(12, 12, 12)
                        .addComponent(txt_CurlLink, javax.swing.GroupLayout.PREFERRED_SIZE, 670, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panel_mainLayout.createSequentialGroup()
                        .addGap(80, 80, 80)
                        .addComponent(panel_Status, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panel_mainLayout.createSequentialGroup()
                        .addGap(50, 50, 50)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(spinner_heart, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(50, 50, 50)
                        .addComponent(jLabel3)
                        .addGap(6, 6, 6)
                        .addComponent(spinner_share, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(40, 40, 40)
                        .addComponent(jLabel4)
                        .addGap(5, 5, 5)
                        .addComponent(spinner_comment, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(50, 50, 50)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(30, 30, 30))
        );
        panel_mainLayout.setVerticalGroup(
            panel_mainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel_mainLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(panel_mainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txt_CurlLink, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addComponent(panel_Status, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addGroup(panel_mainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spinner_heart, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spinner_share, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spinner_comment, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(25, 25, 25))
        );

        jSplitPane1.setLeftComponent(panel_main);

        scollPanel_Video.setBackground(new java.awt.Color(39, 59, 79));
        scollPanel_Video.setBorder(null);
        scollPanel_Video.setPreferredSize(new java.awt.Dimension(0, 0));

        panel_Video.setBackground(new java.awt.Color(39, 59, 79));
        panel_Video.setMaximumSize(new java.awt.Dimension(0, 0));
        panel_Video.setPreferredSize(new java.awt.Dimension(0, 0));
        panel_Video.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                panel_VideoMouseMoved(evt);
            }
        });

        javax.swing.GroupLayout panel_VideoLayout = new javax.swing.GroupLayout(panel_Video);
        panel_Video.setLayout(panel_VideoLayout);
        panel_VideoLayout.setHorizontalGroup(
            panel_VideoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        panel_VideoLayout.setVerticalGroup(
            panel_VideoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        scollPanel_Video.setViewportView(panel_Video);

        jSplitPane1.setRightComponent(scollPanel_Video);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 900, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jSplitPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 650, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jSplitPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        labelStatus.setText("G e t     L i n k     L o a d i n g . . . ! ! !");
        panel_Status.setVisible(true);
        resetPanel(panel_Status);
        getLink();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void panel_VideoMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panel_VideoMouseMoved
        // TODO add your handling code here:
    }//GEN-LAST:event_panel_VideoMouseMoved


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JPanel panel_Status;
    private javax.swing.JPanel panel_Video;
    private javax.swing.JPanel panel_main;
    private javax.swing.JScrollPane scollPanel_Video;
    private javax.swing.JSpinner spinner_comment;
    private javax.swing.JSpinner spinner_heart;
    private javax.swing.JSpinner spinner_share;
    private javax.swing.JTextField txt_CurlLink;
    // End of variables declaration//GEN-END:variables
}
