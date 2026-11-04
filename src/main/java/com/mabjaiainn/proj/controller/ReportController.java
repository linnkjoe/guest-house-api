/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mabjaiainn.proj.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.mabjaiainn.proj.DTO.MonthlyRevenueReportDTO;
import com.mabjaiainn.proj.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author linnkjoe
 */
@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {
    private ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping({"/revenue", "/reveneu"})
    public ResponseEntity<MonthlyRevenueReportDTO> getReveneuReport(
    @RequestParam int year, @RequestParam int month
    ){
    return ResponseEntity.ok(reportService.generateMonthlyReposrt(year, month));
    }
}
