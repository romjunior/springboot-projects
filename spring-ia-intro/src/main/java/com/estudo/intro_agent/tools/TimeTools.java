package com.estudo.intro_agent.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class TimeTools {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeTools.class);

    @Tool(
            name = "getCurrentLocalTime",
            description = "Obter a data e hora no horário local do usuário"
    )
    String getCurrentLocalTime() {
        final var localTime = LocalDateTime.now().toString();
        LOGGER.info("localtime={}",  localTime);
        return localTime;
    }

    @Tool(
            name = "getCurrentTime",
            description = "Obter data e hora data uma zona de tempo que o usuário passou ou pediu"
    )
    String getCurrentTime(@ToolParam(description = "Zona de tempo, formato em que o LocalDateTime ZoneId.of do Java aceita") String timeZone) {
        final var localTime = LocalDateTime.now(ZoneId.of(timeZone)).toString();
        LOGGER.info("localtime={}, zoneId={}", localTime, timeZone);
        return localTime;
    }
}
