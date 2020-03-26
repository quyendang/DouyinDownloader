/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Frame;

import Model.VideoObject;
import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;

/**
 *
 * @author E6540
 */
public class VideoContent extends javax.swing.JPanel {

    /**
     * Creates new form Video
     */
    private VideoObject objV;
    private String linkDownload;

    public VideoContent(VideoObject obj) {
        initComponents();
        this.panel_video.setBackground(new Color(0, 0, 0, 0));
        this.panel_infoVideo.setBackground(new Color(0, 0, 0, 0));
        this.panel_video.setVisible(true);
        this.objV = obj;
        ImageIcon img = new ImageIcon(ResizeImg(objV.getImgPreviewVideo(), 290));
        this.lbl_videoThumb.setIcon(img);
        this.lbl_heart.setText(String.valueOf(objV.getHeartNums()));
        this.lbl_cmt.setText(String.valueOf(objV.getCmtNums()));
        this.lbl_share.setText(String.valueOf(objV.getShareNums()));
        this.linkDownload = obj.getLinkDownload();

    }

    public Image ResizeImg(ImageIcon imgIcon, int px) {
        return imgIcon.getImage().getScaledInstance(-1, px, Image.SCALE_SMOOTH);
    }

    public void downloadThisVideo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save this Video");

        int userSelection = fileChooser.showSaveDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            this.lbl_download.setEnabled(false);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    reqDownloadAndSaveFile(filePath);
                }
            });
            try {
                t.start();
                t.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(VideoContent.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.lbl_download.setEnabled(true);
        }
    }

    public void reqDownloadAndSaveFile(String fPath) {
        if (fPath.lastIndexOf(".") != -1) {
            if (!fPath.substring(fPath.lastIndexOf(".") + 1).equals("mp4")) {
                fPath += ".mp4";
            }
        } else {
            fPath += ".mp4";
        }
        try {
            URL url = new URL(linkDownload);
            URLConnection urlConn = url.openConnection();
            urlConn.setConnectTimeout(30000);
            urlConn.setReadTimeout(30000);
            urlConn.setAllowUserInteraction(false);
            InputStream inputStream = urlConn.getInputStream();
            byte[] videoBuffer = new byte[512];

            File file = new File(fPath);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            int len;
            
            while ((len = inputStream.read(videoBuffer)) != -1) {
                fileOutputStream.write(videoBuffer, 0, len);     
            }
            //Close the file stream
            fileOutputStream.close();
        } catch (MalformedURLException ex) {

        } catch (IOException ex) {

        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        layeredpane_video = new javax.swing.JLayeredPane();
        panel_video = new javax.swing.JPanel();
        panel_infoVideo = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        lbl_heart = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        lbl_cmt = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        lbl_share = new javax.swing.JLabel();
        lbl_download = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        lbl_videoThumb = new javax.swing.JLabel();

        setMaximumSize(new java.awt.Dimension(150, 290));
        setMinimumSize(new java.awt.Dimension(150, 290));
        setPreferredSize(new java.awt.Dimension(150, 290));

        layeredpane_video.setMaximumSize(new java.awt.Dimension(150, 290));
        layeredpane_video.setMinimumSize(new java.awt.Dimension(150, 290));
        layeredpane_video.setName(""); // NOI18N
        layeredpane_video.setLayout(new javax.swing.OverlayLayout(layeredpane_video));

        panel_video.setBackground(new java.awt.Color(0, 0, 0));
        panel_video.setAlignmentX(0.5F);
        panel_video.setAlignmentY(0.5F);
        panel_video.setMaximumSize(new java.awt.Dimension(150, 290));
        panel_video.setMinimumSize(new java.awt.Dimension(150, 290));
        panel_video.setPreferredSize(new java.awt.Dimension(150, 290));
        panel_video.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        panel_infoVideo.setBackground(new java.awt.Color(0, 0, 0));
        panel_infoVideo.setMaximumSize(new java.awt.Dimension(100, 90));
        panel_infoVideo.setMinimumSize(new java.awt.Dimension(100, 90));
        panel_infoVideo.setPreferredSize(new java.awt.Dimension(100, 90));
        panel_infoVideo.setLayout(new java.awt.GridLayout(3, 2));

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/heart.png"))); // NOI18N
        panel_infoVideo.add(jLabel1);

        lbl_heart.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lbl_heart.setForeground(new java.awt.Color(255, 255, 255));
        lbl_heart.setText("999999");
        panel_infoVideo.add(lbl_heart);

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/cmt.png"))); // NOI18N
        panel_infoVideo.add(jLabel2);

        lbl_cmt.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lbl_cmt.setForeground(new java.awt.Color(255, 255, 255));
        lbl_cmt.setText("999999");
        lbl_cmt.setMaximumSize(new java.awt.Dimension(50, 16));
        lbl_cmt.setMinimumSize(new java.awt.Dimension(50, 16));
        lbl_cmt.setPreferredSize(new java.awt.Dimension(50, 16));
        panel_infoVideo.add(lbl_cmt);

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/share.png"))); // NOI18N
        panel_infoVideo.add(jLabel3);

        lbl_share.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lbl_share.setForeground(new java.awt.Color(255, 255, 255));
        lbl_share.setText("999999");
        panel_infoVideo.add(lbl_share);

        panel_video.add(panel_infoVideo, new org.netbeans.lib.awtextra.AbsoluteConstraints(-10, 190, 100, 80));

        lbl_download.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lbl_download.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/download.png"))); // NOI18N
        lbl_download.setAlignmentX(0.5F);
        lbl_download.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lbl_download.setMaximumSize(new java.awt.Dimension(40, 40));
        lbl_download.setMinimumSize(new java.awt.Dimension(40, 40));
        lbl_download.setPreferredSize(new java.awt.Dimension(40, 40));
        lbl_download.setRequestFocusEnabled(false);
        lbl_download.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lbl_downloadMouseClicked(evt);
            }
        });
        panel_video.add(lbl_download, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 0, -1, -1));

        jLabel4.setText("jLabel4");
        panel_video.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, 110, 60));

        layeredpane_video.add(panel_video);

        lbl_videoThumb.setAlignmentX(0.5F);
        lbl_videoThumb.setMaximumSize(new java.awt.Dimension(150, 290));
        lbl_videoThumb.setMinimumSize(new java.awt.Dimension(150, 290));
        lbl_videoThumb.setPreferredSize(new java.awt.Dimension(150, 290));
        layeredpane_video.add(lbl_videoThumb);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(layeredpane_video, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(layeredpane_video, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void lbl_downloadMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbl_downloadMouseClicked
        downloadThisVideo();
    }//GEN-LAST:event_lbl_downloadMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLayeredPane layeredpane_video;
    private javax.swing.JLabel lbl_cmt;
    private javax.swing.JLabel lbl_download;
    private javax.swing.JLabel lbl_heart;
    private javax.swing.JLabel lbl_share;
    private javax.swing.JLabel lbl_videoThumb;
    private javax.swing.JPanel panel_infoVideo;
    private javax.swing.JPanel panel_video;
    // End of variables declaration//GEN-END:variables
}
