package com.tlang.xsjc.jkdk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.actionsoft.apps.listener.PluginListener;
import com.actionsoft.apps.resource.AppContext;
import com.actionsoft.apps.resource.plugin.profile.AWSPluginProfile;
import com.actionsoft.apps.resource.plugin.profile.AppExtensionProfile;
import com.tlang.xsjc.jkdk.notify.JkdkFormatter;

public class Plugin implements PluginListener {
	@Override
	public List<AWSPluginProfile> register(AppContext appContext) {
		List<AWSPluginProfile> list = new ArrayList<>();
		//注册通知中心
		Map<String, Object> params1 = new HashMap<String, Object>();
		params1.put("systemName", JkdkFormatter.DEMO_NAME);
		
		params1.put("icon", "../commons/img/tools1_96.png");
		params1.put("formatter", JkdkFormatter.class.getName());
		list.add(new AppExtensionProfile(JkdkFormatter.DEMO_NAME, "aslp://com.actionsoft.apps.notification/registerApp", params1));
		return list;
	}
}
