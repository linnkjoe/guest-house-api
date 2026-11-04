package com.mabjaiainn.proj.service;

import com.mabjaiainn.proj.DTO.MonthlyRevenueReportDTO;
import com.mabjaiainn.proj.entity.Booking;
import com.mabjaiainn.proj.entity.Client;
import com.mabjaiainn.proj.entity.Packs;
import com.mabjaiainn.proj.exception.BusinessException;
import com.mabjaiainn.proj.repository.BookingRepository;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    void shouldIncludeTopClientsAndMostReservedPackages() throws BusinessException {
        Client client = new Client();
        client.setClientId(1L);
        client.setClientFirstName("Ana");
        client.setClientLastName("Silva");

        Packs pack = new Packs();
        pack.setPackId(2L);
        pack.setPackName("Fim de semana");

        Booking first = booking(client, pack, "1000", "50");
        Booking second = booking(client, pack, "1500", "25");
        when(bookingRepository.findBookingByBookingEnterBetween(any(), any())).thenReturn(List.of(first, second));

        YearMonth period = YearMonth.now();
        MonthlyRevenueReportDTO report = reportService.generateMonthlyReposrt(period.getYear(), period.getMonthValue());

        assertEquals(2L, report.totalBookings());
        assertEquals(new BigDecimal("2500"), report.totalRevenue());
        assertEquals(new BigDecimal("75"), report.totalPenalties());
        assertEquals(1, report.bestClients().size());
        assertEquals("Ana Silva", report.bestClients().get(0).clientName());
        assertEquals(2L, report.bestClients().get(0).bookingCount());
        assertEquals(new BigDecimal("2500"), report.bestClients().get(0).totalSpent());
        assertEquals(1, report.mostReservedPackages().size());
        assertEquals("Fim de semana", report.mostReservedPackages().get(0).packName());
        assertEquals(2L, report.mostReservedPackages().get(0).bookingCount());
        verify(bookingRepository).findBookingByBookingEnterBetween(any(), any());
    }

    @Test
    void shouldRejectFutureReportPeriod() {
        YearMonth nextMonth = YearMonth.now().plusMonths(1);

        assertThrows(BusinessException.class,
                () -> reportService.generateMonthlyReposrt(nextMonth.getYear(), nextMonth.getMonthValue()));
    }

    private Booking booking(Client client, Packs pack, String total, String penalty) {
        Booking booking = new Booking();
        booking.setClient(client);
        booking.setPack(pack);
        booking.setTotalPrice(new BigDecimal(total));
        booking.setPenalty(new BigDecimal(penalty));
        return booking;
    }
}
