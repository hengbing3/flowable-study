package com.christer.flowablestudy;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.stereotype.Component;

/**
 * @author Christer
 * @version 1.0
 * @date 2024-03-03 19:48
 * Description:
 */
@Component
public class FlowableUiApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(FlowableUiApplication.class);
    }
}
