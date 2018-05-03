package com.cash.slide.Controller;

import com.cash.slide.model.SlideResponse;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * 滑动验证
 * author cash
 * create 2018-05-02-13:20
 **/
@Controller()
@RequestMapping("/Slide")
public class SlideService {

    Map offsetMap=Maps.newConcurrentMap();

    @RequestMapping("/init")
    public @ResponseBody
    SlideResponse InitCaptcha() throws IOException{
        SlideResponse slideResponse=new SlideResponse();

        Map map= Maps.newHashMap();
        BASE64Encoder encoder = new BASE64Encoder();

        Random intRandom=new Random();
        Random boolRandom=new Random();

        String[] imgArr={"car","girl","road","children","keep","man","mail","hand"};

        String path=this.getClass().getClassLoader().getResource("./").getPath();

        String srcImageFile = path+"static/slide/img/"+imgArr[intRandom.nextInt(8)]+".jpg";
        BufferedImage src = ImageIO.read(new File(srcImageFile)); // 读入文件
        int srcWidth = src.getWidth(); //源宽 320
        int srcHeight = src.getHeight(); //源长 160

        int captchaWidth = 45;//验证码区域宽
        int captchaHeight = 45;//验证码区域高

        int gap=captchaWidth/3; //gap是凹凸长/宽  1/3 width



        //随机起始位置左上角坐标
        int captchaX= intRandom.nextInt(srcWidth-captchaWidth-gap);
        if(captchaX<=80){
            captchaX+=80;
        }
        //int captchaY= intRandom.nextInt(srcHeight-captchaHeight-gap);
        int captchaY= 100;

        //验证码path
        GeneralPath captchaPath = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        captchaPath.reset();
        captchaPath.moveTo(0,0);

        captchaPath.moveTo(captchaX,captchaY);//左上
        captchaPath.lineTo(captchaX+gap,captchaY);//top凹凸起始point
        drawPartCircle(new Point2D.Float(captchaX+gap,captchaY),
                new Point2D.Float(captchaX+gap*2,captchaY),
                captchaPath,boolRandom.nextBoolean());

        captchaPath.lineTo(captchaX+captchaWidth,captchaY);//右上
        captchaPath.lineTo(captchaX+captchaWidth,captchaY-gap);
        drawPartCircle(new Point2D.Float(captchaX+captchaWidth,captchaY-gap),
                new Point2D.Float(captchaX+captchaWidth,captchaY-gap*2),
                captchaPath,boolRandom.nextBoolean());

        captchaPath.lineTo(captchaX+captchaWidth,captchaY-captchaHeight);//右下
        captchaPath.lineTo(captchaX+captchaWidth-gap,captchaY-captchaHeight);
        drawPartCircle(new Point2D.Float(captchaX+captchaWidth-gap,captchaY-captchaHeight),
                new Point2D.Float(captchaX+captchaWidth-gap*2,captchaY-captchaHeight),
                captchaPath,boolRandom.nextBoolean());

        captchaPath.lineTo(captchaX,captchaY-captchaHeight);//左下
        captchaPath.lineTo(captchaX,captchaY-captchaHeight+gap);
        drawPartCircle(new Point2D.Float(captchaX,captchaY-captchaHeight+gap),
                new Point2D.Float(captchaX,captchaY-captchaHeight+gap*2),
                captchaPath,true);

        captchaPath.closePath();

        String uuid= String.valueOf(UUID.randomUUID());
        map.put("uuid",uuid);

        //uuid 跟偏移量入缓存
        offsetMap.put(uuid,captchaX);

        BufferedImage bi2 = new BufferedImage(srcWidth, srcHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = bi2.createGraphics();

        bi2 = g2.getDeviceConfiguration().createCompatibleImage(srcWidth, srcHeight, Transparency.TRANSLUCENT);
        g2.dispose();
        g2 = bi2.createGraphics();
        g2.setClip(captchaPath);
        g2.drawImage(src, 0, 0, null);
        g2.dispose();

        BufferedImage cutBi=bi2.getSubimage(captchaX,0,srcWidth-captchaX,srcHeight);
        //String frontPath = "C:\\Users\\xuchao02\\Desktop\\imxx\\front"+ uuid+".png";
        //ImageIO.write(cutBi,"png",new File(frontPath));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(cutBi, "png", outputStream);
        String frontBase64Img = encoder.encode(outputStream.toByteArray());
        map.put("front","data:image/png;base64,"+frontBase64Img);


        BufferedImage bi3 = new BufferedImage(srcWidth, srcHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g3 =  bi3.createGraphics();
        g3.drawImage(src, 0, 0, null);
        g3.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.7f));
        g3.setColor(new Color(219, 220, 237));
        g3.setStroke(new BasicStroke(1f));
        g3.fill(captchaPath);
        g3.dispose();

        //String backPath = "C:\\Users\\xuchao02\\Desktop\\imxx\\back"+ uuid+".png";
        //ImageIO.write(bi3,"png",new File(backPath));

        ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();
        ImageIO.write(bi3, "png", outputStream2);
        String backBase64Img = encoder.encode(outputStream2.toByteArray());
        map.put("back","data:image/png;base64,"+backBase64Img);

        slideResponse.setRetCode(0);
        slideResponse.setData(map);
        return slideResponse;

    }

    @RequestMapping("/check")
    public @ResponseBody SlideResponse checkCaptcha(@RequestParam String uuid, @RequestParam String offset) throws IOException{
        SlideResponse slideResponse=new SlideResponse();
        Integer offsetInMap= (Integer) offsetMap.get(uuid);
        if(null!=offsetInMap  && null!=offset && !"".equals(offset)){
            if(Math.abs(offsetInMap-Integer.parseInt(offset))<=1){
                slideResponse.setRetCode(0);
                Iterator<String> iter = offsetMap.keySet().iterator();
                while(iter.hasNext()){
                    String key = iter.next();
                    if(uuid.equals(key)){
                        iter.remove();
                    }
                }
            }else slideResponse.setRetCode(-1);
        }else{
            slideResponse.setRetCode(-1);
        }
        return slideResponse;

    }


    private void drawPartCircle(Point2D.Float start, Point2D.Float end, Path2D path2D, boolean outer){
        float c=0.551915024494f;
        //中点
        Point2D.Float middle=new Point2D.Float(start.x/2+end.x/2,start.y/2+end.y/2);
        //半径
        float r1= (float) Math.sqrt(Math.pow((middle.x-start.x),2)+Math.pow((middle.y-start.y),2));
        float gap1=r1*c;

        if(start.x==end.x){//竖直
            boolean topToBottom=end.y>start.y?true:false;
            int flag=topToBottom?1:-1;//旋转系数
            if(outer){//凸出
                path2D.curveTo(start.x+gap1*flag,start.y,
                        middle.x+r1*flag,middle.y-gap1*flag,
                        middle.x+r1*flag,middle.y);

                path2D.curveTo(middle.x+r1*flag,middle.y+gap1*flag,
                        end.x+gap1*flag,end.y,
                        end.x,end.y);
            }else{
                path2D.curveTo(start.x-gap1*flag,start.y,
                        middle.x-r1*flag,middle.y-gap1*flag,
                        middle.x-r1*flag,middle.y);

                path2D.curveTo(middle.x-r1*flag,middle.y+gap1*flag,
                        end.x-gap1*flag,end.y,
                        end.x,end.y);

            }

        }else{//水平
            boolean leftToRight=end.x>start.x?true:false;
            int flag=leftToRight?1:-1;
            if(outer){//凸出
                path2D.curveTo(start.x,start.y-gap1*flag,
                        middle.x-gap1*flag,middle.y-r1*flag,
                        middle.x,middle.y-r1*flag);

                path2D.curveTo(middle.x+gap1*flag,middle.y-r1*flag,
                        end.x,end.y-gap1*flag,
                        end.x,end.y);

            }else{
                path2D.curveTo(start.x,start.y+gap1*flag,
                        middle.x-gap1*flag,middle.y+r1*flag,
                        middle.x,middle.y+r1*flag);

                path2D.curveTo(middle.x+gap1*flag,middle.y+r1*flag,
                        end.x,end.y+gap1*flag,
                        end.x,end.y);

            }

        }

    }


}
