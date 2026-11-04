package com.mabjaiainn.proj.service;

import com.mabjaiainn.proj.entity.Room;
import com.mabjaiainn.proj.entity.RoomStatus;
import com.mabjaiainn.proj.entity.Booking;
import com.mabjaiainn.proj.entity.BookingStatus;
import com.mabjaiainn.proj.repository.BookingRepository;
import com.mabjaiainn.proj.repository.RoomRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RoomScheduler {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    public RoomScheduler(RoomRepository roomRepository, BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    @Scheduled(initialDelay = 10000, fixedDelay = 10000)
    @Transactional
    public void releaseCleanedRooms() {
        LocalDateTime now = LocalDateTime.now();
        for (Room room : roomRepository.findByStatusAndDataFimLimpezaIsNull(RoomStatus.EM_LIMPEZA)) {
            room.setDataFimLimpeza(now.plusMinutes(30));
        }

        for (Booking booking : bookingRepository.findByStatusAndBookingOutLessThanEqual(
                BookingStatus.RESERVED, now)) {
            booking.setStatus(BookingStatus.EXPIRED);
            Room room = booking.getRoom();
            if (room.getStatus() == RoomStatus.RESERVADO) {
                List<Booking> roomBookings = bookingRepository.findByRoom(room);
                boolean hasCheckedInBooking = roomBookings.stream()
                        .anyMatch(other -> other != booking && other.getStatus() == BookingStatus.CHECKED_IN);
                boolean hasActiveReservation = roomBookings.stream()
                        .anyMatch(other -> other != booking && other.getStatus() == BookingStatus.RESERVED
                                && other.getBookingOut() != null && other.getBookingOut().isAfter(now));
                room.setStatus(hasCheckedInBooking
                        ? RoomStatus.OCUPADO
                        : hasActiveReservation ? RoomStatus.RESERVADO : RoomStatus.DISPONIVEL);
                if (hasCheckedInBooking) {
                    room.setDataFimLimpeza(null);
                }
            }
        }

        for (Booking booking : bookingRepository.findByStatus(BookingStatus.CHECKED_IN)) {
            Room room = booking.getRoom();
            if (room.getStatus() != RoomStatus.OCUPADO) {
                room.setStatus(RoomStatus.OCUPADO);
                room.setDataFimLimpeza(null);
            }
        }

        for (Booking booking : bookingRepository.findByStatusAndBookingEnterGreaterThanAndBookingOutGreaterThan(
                BookingStatus.RESERVED, now, now)) {
            Room room = booking.getRoom();
            if (room.getStatus() == RoomStatus.DISPONIVEL) {
                room.setStatus(RoomStatus.RESERVADO);
            }
        }

        for (Room room : roomRepository.findByStatusAndDataFimLimpezaLessThanEqual(RoomStatus.EM_LIMPEZA, now)) {
            boolean hasUpcomingReservation = bookingRepository.findByRoom(room).stream()
                    .anyMatch(booking -> booking.getStatus() == BookingStatus.RESERVED
                            && booking.getBookingOut() != null && booking.getBookingOut().isAfter(now));
            room.setStatus(hasUpcomingReservation ? RoomStatus.RESERVADO : RoomStatus.DISPONIVEL);
            room.setDataFimLimpeza(null);
        }
    }
}
