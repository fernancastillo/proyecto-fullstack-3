package com.bffservice.client;

import com.bffservice.dto.LogRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@FeignClient(name = "REQUEST-SERVICE", contextId = "requestLogClient")
public interface RequestLogClient {

    @GetMapping("/api/logs/rango")
    List<LogRequestDTO> getLogsPorRango(
            @RequestParam("inicio") String inicio,
            @RequestParam("fin")    String fin);
}