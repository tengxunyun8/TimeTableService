package com.yg.timetableservice.listener;

import com.yg.timetableservice.util.HttpUtil;
import com.yg.timetableservice.util.OCSUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

/**
 * destory resource
 */
@Component
public class WebListener implements ApplicationListener {
    @Autowired
    private OCSUtil ocsUtil;
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextClosedEvent) {
            ocsUtil.close();
            HttpUtil.stop();
        }
    }
}
