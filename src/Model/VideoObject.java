/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import javax.swing.ImageIcon;

/**
 *
 * @author E6540
 */
public class VideoObject {
    private String linkDownload;
    private int heartNums;
    private int shareNums;
    private int cmtNums;
    private String des;
    private ImageIcon imgPreviewVideo;

    public VideoObject(String linkDownload, int heartNums, int shareNums, int cmtNums, String des, ImageIcon img) {
        this.linkDownload = linkDownload;
        this.heartNums = heartNums;
        this.shareNums = shareNums;
        this.cmtNums = cmtNums;
        this.des = des;
        this.imgPreviewVideo = img;
    }

    public ImageIcon getImgPreviewVideo() {
        return imgPreviewVideo;
    }

    public void setImgPreviewVideo(ImageIcon imgPreviewVideo) {
        this.imgPreviewVideo = imgPreviewVideo;
    }
 
    public String getLinkDownload() {
        return linkDownload;
    }

    public void setLinkDownload(String linkDownload) {
        this.linkDownload = linkDownload;
    }

    public int getHeartNums() {
        return heartNums;
    }

    public void setHeartNums(int heartNums) {
        this.heartNums = heartNums;
    }

    public int getShareNums() {
        return shareNums;
    }

    public void setShareNums(int shareNums) {
        this.shareNums = shareNums;
    }

    public int getCmtNums() {
        return cmtNums;
    }

    public void setCmtNums(int cmtNums) {
        this.cmtNums = cmtNums;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    
    
    public void printObj()
    {
        System.out.println("================================================");
        System.out.println("Tên: "+this.des+" - Lượt tim: "+this.heartNums+" - Lượt comment: "+this.cmtNums+" - Lượt share: "+this.shareNums);
        System.out.println("Link download: "+this.linkDownload);
    }
}
