package com.tlang.xsjc.jkdk.controller;

import com.actionsoft.bpms.commons.database.ColumnMapRowMapper;
import com.actionsoft.bpms.server.UserContext;
import com.actionsoft.bpms.server.bind.annotation.Controller;
import com.actionsoft.bpms.server.bind.annotation.Mapping;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.sdk.local.SDK;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tlang.xsjc.jkdk.utils.StrUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;


@Controller
public class GetFunMenuController {
	private static Logger LOGGER = LoggerFactory.getLogger(GetFunMenuController.class);

	/**
	 * @Description 获取某个人某个菜单下授权的功能清单-> 入参：账号、导航菜单序号|出参:状态（0：成功|1：失败）、信息提示（失败时携带），菜单【{菜单名称，菜单URL，菜单简介，菜单图标地址}】
	 * @author WU LiHua
	 * @date 2020年2月4日 上午10:28:42
	 */
	@Mapping("jch5.kq_getFunMenuInfo")
	public static String getFunMenuInfo(UserContext uc,String userId,String parentId) {
    	JSONObject returnData = new JSONObject();
		try {
			if(StringUtils.isEmpty(userId)) {//如果不传的话，就获取当前人员
				userId = uc.getUID();
			}

			String querySql = "select * from BO_EU_SHBU_MENU where MSTATUS='0' ";
			if(StringUtils.isEmpty(parentId)) {//如果不传的话，就获取一级菜单
				querySql = querySql +" and MSJNO is null ";
			}else{
				querySql = querySql +" and MSJNO='"+parentId+"'";
			}

			List<Map<String,Object>> menuList =  DBSql.query(querySql,new ColumnMapRowMapper(),new Object[]{});
			JSONArray menuArrInfo = new JSONArray();
			for(int i=0;i<menuList.size();i++) {
				Map<String,Object> menuMap = menuList.get(i);
				String menuName = StrUtils.objToStr(menuMap.get("MNAME"));
				String menuUrl = StrUtils.objToStr(menuMap.get("MADDR"));
				String menuDesc = StrUtils.objToStr(menuMap.get("MDESC"));
				String menuIco =  StrUtils.objToStr(menuMap.get("MPICTURE"));
				String orderIndex = StrUtils.objToStr(menuMap.get("MORDER"));
				String boId = StrUtils.objToStr(menuMap.get("ID"));
				//判断用户是否有当前菜单权限
				boolean hasAuth =  SDK.getPermAPI().havingACPermission(userId,"platform.process",boId,0);
				if(hasAuth){
					JSONObject returnMenuData = new JSONObject();
					returnMenuData.put("menuName", menuName);
					menuUrl = SDK.getRuleAPI().executeAtScript(menuUrl,uc);
					returnMenuData.put("menuUrl",  menuUrl);
					returnMenuData.put("menuIco96", menuIco);
					returnMenuData.put("menuDesc", menuDesc);
					returnMenuData.put("orderIndex",orderIndex);
					menuArrInfo.add(returnMenuData);
				}
			}
			returnData.put("status", "0");
			returnData.put("menuArrInfo", menuArrInfo);
		} catch (Exception e) {
			e.printStackTrace();
			returnData.put("status", "1");
			returnData.put("message", e.getMessage());
		}
		return returnData.toString();
	}
}
