/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mabjaiainn.proj.service;

import com.mabjaiainn.proj.DTO.MonthlyRevenueReportDTO;
import com.mabjaiainn.proj.DTO.TopClientReportDTO;
import com.mabjaiainn.proj.DTO.TopPackageReportDTO;
import com.mabjaiainn.proj.entity.Booking;
import com.mabjaiainn.proj.entity.Client;
import com.mabjaiainn.proj.entity.Packs;
import com.mabjaiainn.proj.repository.BookingRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.mabjaiainn.proj.exception.BusinessException;
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
    
    public MonthlyRevenueReportDTO generateMonthlyReposrt(int year, int month) throws BusinessException {
        LocalDateTime now = LocalDateTime.now();
        if (year < 2000 || month < 1 || month > 12) {
            throw new BusinessException("Report period is invalid");
        }
        if (year > now.getYear() || (year == now.getYear() && month > now.getMonthValue())) {
            throw new BusinessException("Report period cannot be in the future");
        }
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);
        
        List<Booking> bookings = bookingRepository.findBookingByBookingEnterBetween(startOfMonth, endOfMonth);
     
        BigDecimal totalReveneu = bookings.stream().map(Booking::getTotalPrice)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPenalties = bookings.stream().map(Booking::getPenalty)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Long, ClientTotals> clients = new HashMap<>();
        Map<Long, PackageTotals> packages = new HashMap<>();
        for (Booking booking : bookings) {
            Client client = booking.getClient();
            if (client != null && client.getClientId() != null) {
                ClientTotals totals = clients.computeIfAbsent(client.getClientId(), ignored -> new ClientTotals(client));
                totals.bookingCount++;
                if (booking.getTotalPrice() != null) {
                    totals.totalSpent = totals.totalSpent.add(booking.getTotalPrice());
                }
            }
            Packs pack = booking.getPack();
            if (pack != null && pack.getPackId() != null) {
                PackageTotals totals = packages.computeIfAbsent(pack.getPackId(), ignored -> new PackageTotals(pack));
                totals.bookingCount++;
            }
        }

        List<TopClientReportDTO> bestClients = clients.values().stream()
                .sorted(Comparator.comparing((ClientTotals totals) -> totals.totalSpent).reversed()
                        .thenComparing(Comparator.comparingLong((ClientTotals totals) -> totals.bookingCount).reversed()))
                .limit(5)
                .map(totals -> new TopClientReportDTO(
                        totals.client.getClientId(),
                        (totals.client.getClientFirstName() + " " + totals.client.getClientLastName()).trim(),
                        totals.bookingCount,
                        totals.totalSpent))
                .toList();
        List<TopPackageReportDTO> mostReservedPackages = packages.values().stream()
                .sorted(Comparator.comparingLong((PackageTotals totals) -> totals.bookingCount).reversed())
                .limit(5)
                .map(totals -> new TopPackageReportDTO(
                        totals.pack.getPackId(),
                        totals.pack.getPackName(),
                        totals.bookingCount))
                .toList();

        return new MonthlyRevenueReportDTO(
                YearMonth.of(year, month),
                (long) bookings.size(),
                totalReveneu,
                totalPenalties,
                bestClients,
                mostReservedPackages
        );
    }

    private static final class ClientTotals {
        private final Client client;
        private long bookingCount;
        private BigDecimal totalSpent = BigDecimal.ZERO;

        private ClientTotals(Client client) {
            this.client = client;
        }
    }

    private static final class PackageTotals {
        private final Packs pack;
        private long bookingCount;

        private PackageTotals(Packs pack) {
            this.pack = pack;
        }
    }
     
}
