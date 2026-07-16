/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mabjaiainn.proj.DTO;

import java.math.BigDecimal;
import java.time.YearMonth;

/**
 *
 * @author linnkjoe
 */
public record MonthlyRevenueReportDTO (
        YearMonth month,
        Long totalBookings,
        BigDecimal totalRevenue,
        BigDecimal totalPenalties
 )
{}
