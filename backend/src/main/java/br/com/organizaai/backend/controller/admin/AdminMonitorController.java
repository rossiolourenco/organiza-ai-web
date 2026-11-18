package br.com.organizaai.backend.controller.admin;

import br.com.organizaai.backend.dto.MonitorResponse;
import br.com.organizaai.backend.service.MonitorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/monitoramento")
public class AdminMonitorController {
    private final MonitorService service;

    public AdminMonitorController(MonitorService service) {
        this.service = service;
    }

    @GetMapping("/overview")
    public MonitorResponse overview() {
        return service.overview();
    }
}
