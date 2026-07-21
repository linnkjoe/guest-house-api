/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mabjaiainn.proj.service;

import com.mabjaiainn.proj.DTO.MonthlyRevenueReportDTO;
import com.mabjaiainn.proj.entity.Booking;
import com.mabjaiainn.proj.repository.BookingRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 *
 * @author linnkjoe
 */

@Service
public class ReportService {
 
    private BookingRepository bookingRepository;

    public ReportService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }
    
    public MonthlyRevenueReportDTO generateMonthlyReposrt(int year, int month){
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.with(TemporalAdjusters.lastDayOfMonth())
                .withHour(23).withMinute(59).withSecond(59);
        
        List<Booking> bookings = bookingRepository.findBookingByBookingEnterBetween(startOfMonth, endOfMonth);
     
        BigDecimal totalReveneu = bookings.stream().map(Booking::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    
                
                Long totalBookings = (long) bookings.size();
                
                return new MonthlyRevenueReportDTO(
                        YearMonth.of(year,month),
                        totalBookings,
                        totalReveneu,
                        BigDecimal.ZERO
                );
    }
     
}
