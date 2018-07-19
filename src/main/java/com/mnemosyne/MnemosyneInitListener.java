package com.mnemosyne;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by Mr.Luo on 2018/7/8
 */
@Slf4j
public class MnemosyneInitListener implements ServletContextListener{

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("初始化上下文");
        Mnemosyne mnemosyne = new Mnemosyne();
        mnemosyne.initContext();
        mnemosyne.start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("销毁上下文");
    }
}
