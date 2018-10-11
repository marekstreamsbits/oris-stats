package oris.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class HealthController extends BaseOrisStatsController {

    @GetMapping("/health")
    public String health() {
        return new Date(System.currentTimeMillis()).toString();
    }
}