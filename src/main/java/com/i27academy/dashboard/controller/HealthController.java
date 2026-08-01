package com.i27academy.dashboard.controller;

import java.sql.Connection;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.i27academy.dashboard.config.AppProperties;
import com.i27academy.dashboard.dto.DashboardSummary;
import com.i27academy.dashboard.service.DashboardService;

@Controller
public class HealthController {

    private final AppProperties appProps;
    private final DashboardService dashboardService;
    private final DataSource dataSource;

    public HealthController(AppProperties appProps, DashboardService dashboardService, DataSource dataSource) {
        this.appProps = appProps;
        this.dashboardService = dashboardService;
        this.dataSource = dataSource;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/health")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> health() {
        String dbStatus;
        try (Connection conn = dataSource.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("SELECT 1");
            dbStatus = "healthy";
        } catch (Exception e) {
            dbStatus = "unhealthy";
        }

        return ResponseEntity.ok(Map.of(
                "status", "healthy",
                "app_name", appProps.name(),
                "version", appProps.version(),
                "environment", appProps.environment(),
                "database", dbStatus,
                "academy", "https://i27academy.com"));
    }

    @GetMapping("/dashboard")
    @ResponseBody
    public ResponseEntity<DashboardSummary> dashboard() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }
}
