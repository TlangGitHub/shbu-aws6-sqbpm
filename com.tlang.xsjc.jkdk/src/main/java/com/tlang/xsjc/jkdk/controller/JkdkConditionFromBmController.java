package com.tlang.xsjc.jkdk.controller;

import java.util.List;
import java.util.Map;

import com.actionsoft.bpms.commons.database.ColumnMapRowMapper;
import com.actionsoft.bpms.server.UserContext;
import com.actionsoft.bpms.server.bind.annotation.Controller;
import com.actionsoft.bpms.server.bind.annotation.Mapping;
import com.actionsoft.bpms.util.DBSql;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tlang.xsjc.jkdk.utils.StrUtils;

@Controller
public class JkdkConditionFromBmController {
	/**
	 * 
	 * @Description 查询部门人员考勤及健康打卡分析情况->入参：日期、部门序号|状态（0:成功|1:失败）、信息提示（失败时携带）、人员列表【{人员工号、人员名称、在岗情况（暂定：初次~末次打卡时间、是否体温异常、是否健康报备）}】
	   * 人员工号为员工代码，人员列表为本部门及以下部门所有人员
	 * @author wulh
	 * @date 2020年2月11日 下午13:15:46
	 * @param uc
	 * @return
	 */
	@Mapping("jch5.qj_getJkdkConditionFromBm")
	public static String getJkdkConditionFromBm(UserContext uc,String date,String departmentId) {
		JSONObject result = new JSONObject();
		try {
			String companyId = uc.getCompanyModel().getId();//
			String conditon = "";
			if(!departmentId.contains("$")) {//如果是非本部门需要查出子部门所有数据
				conditon = " or a.departmentid in(select id from orgdepartment where PARENTDEPARTMENTID = '"+departmentId+"')";
			}
			departmentId = departmentId.replace("$", "");
			String queryUserInfo = "SELECT\r\n" + 
					"	a.orderindex,\r\n" + 
					"	a.userid,\r\n" + 
					"	a.EXT3,\r\n" + 
					"	a.USERNAME,\r\n" + 
					"	a.departmentid,\r\n" + 
					"	a.DEPARTMENTNAME,\r\n" + 
					"	c.DRSFZG,\r\n" + 
					"	( case when c.TWSFZC ='1' then '是' else '否' end ) TWSFZC,\r\n" +
					"	( case when c.REPORTUID is null  then '否' else '是' end ) SFJKDK,\r\n" +
					"	( case when c.SFTJZGFXDQ ='0' then '是' else '否' end ) SFTJGFX,\r\n" +
					"	( case when c.SFGL ='0' then '是' else '否' end ) SFGL,\r\n" +
					"	( case when c.DRSFZG ='1' then '是' else '否' end ) SFDS, \r\n" +
					" 	(case when DRSFZG = '0' and GCLX = '1' then '是' else '否' end) SFCH "+
					"FROM\r\n" + 
					"	(\r\n" + 
					"	SELECT DISTINCT\r\n" + 
					"		a.orderindex,\r\n" + 
					"		a.userid,\r\n" + 
					"		a.EXT3,\r\n" + 
					"		a.USERNAME,\r\n" + 
					"		a.departmentid,\r\n" + 
					"		b.DEPARTMENTNAME \r\n" + 
					"	FROM\r\n" + 
					"		orguser a\r\n" + 
					"		LEFT JOIN orgdepartment b ON b.closed = 0 \r\n" + 
					"		AND b.COMPANYID = '"+companyId+"' \r\n" +
					"		AND a.departmentid = b.id \r\n" + 
					"	WHERE\r\n" + 
					"		a.closed = 0 \r\n" + 
					"		AND b.DEPARTMENTNAME IS NOT NULL \r\n" + 
					"		AND ( a.departmentid = '"+departmentId+"' "+conditon+" ) \r\n" + 
					"	) a\r\n" + 
					"	LEFT JOIN (\r\n" + 
					"			SELECT\r\n" + 
					"				t.* \r\n" + 
					"			FROM\r\n" + 
					"				BO_ACT_EMP_HEALTH_CARD t\r\n" +
					"				INNER JOIN ( SELECT max( createdate ) createdate, REPORTDATE, REPORTUID FROM  BO_ACT_EMP_HEALTH_CARD  WHERE  DATE_FORMAT(REPORTDATE, '%Y-%m-%d') = '"+date+"' GROUP BY REPORTDATE, REPORTUID ) e ON t.createdate = e.createdate \r\n" +
					"				AND t.REPORTDATE = e.REPORTDATE \r\n" + 
					"				AND t.REPORTUID = e.REPORTUID \r\n" + 
					"			) c ON a.userid = c.REPORTUID \r\n" + 
					"	AND  DATE_FORMAT(c.REPORTDATE, '%Y-%m-%d') = '"+date+"' \r\n" +
					"ORDER BY\r\n" + 
					"	a.orderindex,\r\n" + 
					"	a.ext3";
			System.out.println("queryUserInfo==="+queryUserInfo);
			List<Map<String, Object>> dataList = DBSql.query(queryUserInfo, new ColumnMapRowMapper(), new Object[] {});
			JSONArray userArrInfo = new JSONArray();
			if(dataList != null && dataList.size() > 0) {//如果查到数据
				for(Map<String,Object> map : dataList) {//遍历组织中一级部门
					JSONObject returnDepartment = new JSONObject();
					String userNo = StrUtils.objToStr(map.get("EXT3"));//获取员工工号
					String userName = StrUtils.objToStr(map.get("USERNAME"));//获取员工姓名
					String twsfzc = StrUtils.objToStr(map.get("TWSFZC"));//获取员工体温是否正常
					String sfjkdk = StrUtils.objToStr(map.get("SFJKDK"));//获取员工是否健康打卡
					String sfDs = StrUtils.objToStr(map.get("SFDS"));//是否到所
					String sfGl = StrUtils.objToStr(map.get("SFGL"));//是否隔离
					String sfGfx = StrUtils.objToStr(map.get("SFTJGFX"));//是否高风险
					String sfCh = StrUtils.objToStr(map.get("SFCH"));//是否出沪
					returnDepartment.put("sfGfx", sfGfx);
					returnDepartment.put("sfGl", sfGl);
					returnDepartment.put("sfDs", sfDs);
					returnDepartment.put("userNo", userNo);
					returnDepartment.put("userName", userName);
					returnDepartment.put("twsfzc", twsfzc);
					returnDepartment.put("sfjkdk", sfjkdk);
					returnDepartment.put("sfCh", sfCh);
					userArrInfo.add(returnDepartment);
				}
			}
			result.put("status", 0);
			result.put("userHealthInfo", userArrInfo);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("status", 1);
			result.put("message", e.getMessage());
		}
		return result.toString();
	}
	public static void main(String[] args) {
		String str = "07:56~07:56";
		String a = str.substring(0, 5);
		String b = str.substring(6, 11);
		if(a.equals(b)) {
			str = a;
		}
		System.out.println(str);
	}
}
