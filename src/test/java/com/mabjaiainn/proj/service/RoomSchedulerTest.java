package com.mabjaiainn.proj.service;

import com.mabjaiainn.proj.entity.Room;
import com.mabjaiainn.proj.entity.RoomStatus;
import com.mabjaiainn.proj.entity.Booking;
import com.mabjaiainn.proj.entity.BookingStatus;
import com.mabjaiainn.proj.repository.BookingRepository;
import com.mabjaiainn.proj.repository.RoomRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RoomSchedulerTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private RoomScheduler roomScheduler;

    @Test
    void cleanedRoomBecomesAvailableAfterItsCleaningDeadline() {
        Room room = new Room(101, RoomStatus.EM_LIMPEZA);
        room.setRoomId(12L);
        room.setDataFimLimpeza(LocalDateTime.now().minusMinutes(1));
        when(roomRepository.findByStatusAndDataFimLimpezaIsNull(RoomStatus.EM_LIMPEZA))
                .thenReturn(List.of());
        when(roomRepository.findByStatusAndDataFimLimpezaLessThanEqual(
                org.mockito.ArgumentMatchers.eq(RoomStatus.EM_LIMPEZA),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(List.of(room));
        when(bookingRepository.findByRoom(room)).thenReturn(List.of());

        roomScheduler.releaseCleanedRooms();

        assertEquals(RoomStatus.DISPONIVEL, room.getStatus());
        assertNull(room.getDataFimLimpeza());
    }

    @Test
    void bookingStartingDoesNotCheckInOrOccupyRoomAutomatically() {
        Room room = new Room(101, RoomStatus.RESERVADO);
        room.setRoomId(13L);
        Booking booking = new Booking(LocalDateTime.now().minusMinutes(1));
        booking.setBookingOut(LocalDateTime.now().plusHours(1));
        booking.setRoom(room);
        booking.setStatus(BookingStatus.RESERVED);
        when(bookingRepository.findByStatus(BookingStatus.CHECKED_IN)).thenReturn(List.of());
        when(roomRepository.findByStatusAndDataFimLimpezaIsNull(RoomStatus.EM_LIMPEZA))
                .thenReturn(List.of());
        when(roomRepository.findByStatusAndDataFimLimpezaLessThanEqual(
                org.mockito.ArgumentMatchers.eq(RoomStatus.EM_LIMPEZA),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(bookingRepository.findByStatusAndBookingEnterGreaterThanAndBookingOutGreaterThan(
                org.mockito.ArgumentMatchers.eq(BookingStatus.RESERVED),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(List.of());

        roomScheduler.releaseCleanedRooms();

        assertEquals(BookingStatus.RESERVED, booking.getStatus());
        assertEquals(RoomStatus.RESERVADO, room.getStatus());
    }

    @Test
    void checkedInRoomStaysOccupiedUntilCheckoutIsProcessed() {
        Room room = new Room(101, RoomStatus.DISPONIVEL);
        room.setRoomId(14L);
        Booking booking = new Booking(LocalDateTime.now().minusHours(2));
        booking.setBookingOut(LocalDateTime.now().minusMinutes(5));
        booking.setRoom(room);
        booking.setStatus(BookingStatus.CHECKED_IN);
        when(bookingRepository.findByStatus(BookingStatus.CHECKED_IN)).thenReturn(List.of(booking));
        when(roomRepository.findByStatusAndDataFimLimpezaIsNull(RoomStatus.EM_LIMPEZA))
                .thenReturn(List.of());
        when(roomRepository.findByStatusAndDataFimLimpezaLessThanEqual(
                org.mockito.ArgumentMatchers.eq(RoomStatus.EM_LIMPEZA),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(bookingRepository.findByStatusAndBookingEnterGreaterThanAndBookingOutGreaterThan(
                org.mockito.ArgumentMatchers.eq(BookingStatus.RESERVED),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(List.of());

        roomScheduler.releaseCleanedRooms();

        assertEquals(RoomStatus.OCUPADO, room.getStatus());
        verify(bookingRepository).findByStatus(BookingStatus.CHECKED_IN);
    }

    @Test
    void missedBookingExpiresAtCheckoutAndReleasesItsRoom() {
        Room room = new Room(101, RoomStatus.RESERVADO);
        room.setRoomId(15L);
        Booking booking = new Booking(LocalDateTime.now().minusHours(3));
        booking.setBookingOut(LocalDateTime.now().minusMinutes(1));
        booking.setRoom(room);
        booking.setStatus(BookingStatus.RESERVED);
        when(bookingRepository.findByStatusAndBookingOutLessThanEqual(
                org.mockito.ArgumentMatchers.eq(BookingStatus.RESERVED),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        when(bookingRepository.findByRoom(room)).thenReturn(List.of(booking));
        when(bookingRepository.findByStatus(BookingStatus.CHECKED_IN)).thenReturn(List.of());
        when(bookingRepository.findByStatusAndBookingEnterGreaterThanAndBookingOutGreaterThan(
                org.mockito.ArgumentMatchers.eq(BookingStatus.RESERVED),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(roomRepository.findByStatusAndDataFimLimpezaIsNull(RoomStatus.EM_LIMPEZA))
                .thenReturn(List.of());
        when(roomRepository.findByStatusAndDataFimLimpezaLessThanEqual(
                org.mockito.ArgumentMatchers.eq(RoomStatus.EM_LIMPEZA),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(List.of());

        roomScheduler.releaseCleanedRooms();

        assertEquals(BookingStatus.EXPIRED, booking.getStatus());
        assertEquals(RoomStatus.DISPONIVEL, room.getStatus());
    }

    @Test
    void expiredBookingKeepsRoomReservedForAnotherUpcomingBooking() {
        Room room = new Room(101, RoomStatus.RESERVADO);
        room.setRoomId(16L);
        Booking expired = new Booking(LocalDateTime.now().minusHours(3));
        expired.setBookingOut(LocalDateTime.now().minusMinutes(1));
        expired.setRoom(room);
        expired.setStatus(BookingStatus.RESERVED);
        Booking upcoming = new Booking(LocalDateTime.now().plusHours(1));
        upcoming.setBookingOut(LocalDateTime.now().plusHours(2));
        upcoming.setRoom(room);
        upcoming.setStatus(BookingStatus.RESERVED);
        when(bookingRepository.findByStatusAndBookingOutLessThanEqual(
                org.mockito.ArgumentMatchers.eq(BookingStatus.RESERVED),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(List.of(expired));
        when(bookingRepository.findByRoom(room)).thenReturn(List.of(expired, upcoming));
        when(bookingRepository.findByStatus(BookingStatus.CHECKED_IN)).thenReturn(List.of());
        when(bookingRepository.findByStatusAndBookingEnterGreaterThanAndBookingOutGreaterThan(
                org.mockito.ArgumentMatchers.eq(BookingStatus.RESERVED),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(List.of(upcoming));
        when(roomRepository.findByStatusAndDataFimLimpezaIsNull(RoomStatus.EM_LIMPEZA))
                .thenReturn(List.of());
        when(roomRepository.findByStatusAndDataFimLimpezaLessThanEqual(
                org.mockito.ArgumentMatchers.eq(RoomStatus.EM_LIMPEZA),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(List.of());

        roomScheduler.releaseCleanedRooms();

        assertEquals(BookingStatus.EXPIRED, expired.getStatus());
        assertEquals(BookingStatus.RESERVED, upcoming.getStatus());
        assertEquals(RoomStatus.RESERVADO, room.getStatus());
    }
}
