package com.tlang.xsjc.jkdk.wechart;

import com.actionsoft.bpms.util.UtilURL;
import com.actionsoft.sdk.local.SDK;
import com.alibaba.fastjson.JSONObject;
import com.tlang.xsjc.jkdk.consts.StringConsts;

/**
 * @Description 
 * @author Tlang
 * @date 2020年5月13日 上午10:23:29
 */
public class WeChatCoreUtil {
   private static  String WECHAT_CORPID = "";
   private static  String WECHAT_CORPSECRET = "";
   private static  String WECHAT_URL = "";
   private static  volatile String WECHAT_TOCKEN = "";
   private static  volatile long WECHAT_TOCKEN_CREATETIMESTAMP = 0;
   
   public static  String  getTocken(){
       try {
          if(WECHAT_TOCKEN.equals("")){
              createTocken();
          }
       } catch (Exception e) {
           e.printStackTrace();
       }
       return WECHAT_TOCKEN;
   }
   public static synchronized void  createTocken(){
       try {
           long  expires_in = (System.currentTimeMillis() - WECHAT_TOCKEN_CREATETIMESTAMP)/1000;
           if(WECHAT_TOCKEN.equals("")  || (expires_in>7000)){
               WECHAT_CORPID = SDK.getAppAPI().getProperty(StringConsts.APP_ID, "corpId");
               WECHAT_CORPSECRET = SDK.getAppAPI().getProperty(StringConsts.APP_ID, "corpSecret");
               WECHAT_URL = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=" + WECHAT_CORPID + "&corpsecret=" + WECHAT_CORPSECRET;
               String accessTokenStr = UtilURL.get(WECHAT_URL);
               JSONObject accessTokenObj = JSONObject.parseObject(accessTokenStr);
               String errcode = accessTokenObj.getString("errcode");
               if("0".equals(errcode)) {
                   WECHAT_TOCKEN = accessTokenObj.getString("access_token");
                   WECHAT_TOCKEN_CREATETIMESTAMP = System.currentTimeMillis();
               }
           }
       } catch (Exception e) {
           e.printStackTrace();
       }
   }
   public static  JSONObject  getWeChatUserInfoByWeChatUid(String weChatUid){
       String getWeChatUserInfoUrl = "https://qyapi.weixin.qq.com/cgi-bin/user/get?access_token="+getTocken()+"&userid="+weChatUid;
       try {
           String weChatUserInfo = UtilURL.get(getWeChatUserInfoUrl);
           return JSONObject.parseObject(weChatUserInfo);
       } catch (Exception e) {
           e.printStackTrace();
       }
       return null;
   }
   
   
}
