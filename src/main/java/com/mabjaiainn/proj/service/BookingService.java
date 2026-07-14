/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mabjaiainn.proj.service;

import com.mabjaiainn.proj.exception.BusinessException;
import com.mabjaiainn.proj.exception.SourceNotFound;
import com.mabjaiainn.proj.model.Booking;
import com.mabjaiainn.proj.model.BookingStatus;
import com.mabjaiainn.proj.model.Client;
import com.mabjaiainn.proj.model.Packs;
import com.mabjaiainn.proj.model.Room;
import com.mabjaiainn.proj.model.RoomStatus;
import com.mabjaiainn.proj.repository.BookingRepository;
import com.mabjaiainn.proj.repository.ClientRepository;
import com.mabjaiainn.proj.repository.PacksRepository;
import com.mabjaiainn.proj.repository.RoomRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

/**
 *
 * @author linnkjoe
 */
@Service
public class BookingService {

    private BookingRepository bookingRepository;
    private PackService packService;
    private RoomService roomService;
    private ClientService clientService;

    public BookingService(BookingRepository bookingRepository, PackService packService, RoomService roomService, ClientService clientService) {
        this.bookingRepository = bookingRepository;
        this.packService = packService;
        this.roomService = roomService;
        this.clientService = clientService;
    }

    @Transactional
    public Booking updateBooking(Booking booking) {
        return bookingRepository.save(booking);
    }

    public List<Booking> retrieveAllBookings() {
        return bookingRepository.findAll();
    }

    public Room findAvalableRoom(LocalDateTime checkIn, LocalDateTime checkout) {
        List<Room> rooms = roomService.retrieveAllRooms();

        for (Room room : rooms) {
            List<Booking> bookings = bookingRepository.findByRoom(room);
            boolean hasConflict = false;

            for (Booking booking : bookings) {
                if (checkIn.isBefore(booking.getBookingOut())
                        && checkout.isAfter(booking.getBookingEnter())) {
                    hasConflict = true;
                    break;
                }
            }
            if (!hasConflict) {
                return room;
            }
        }
        return null;
    }

    @Transactional
    public BookingResponseDTO createBookingForClient(Booking booking, Long clientId, Long packId) throws BusinessException {
        Client client = null;

        try {
            var response = clientService.retrieveClientById(clientId);
            client = (response != null) ? response.getContent() : null;
        } catch (Exception e) {
            throw new EntityNotFoundException("Client Nao encontrado");
        }
        if (client == null) {
            throw new EntityNotFoundException("Cliente nao encontrado");
        }

        LocalDateTime checkIn = booking.getBookingEnter();
        
        if(checkIn.isBefore(LocalDateTime.now())){
            throw new BusinessException("O check-in deve estar no futuro");
        }
        
        Packs pack = packService.findPackById(packId).orElseThrow(
                () -> new IllegalArgumentException("Pacote invalido recebido do servidor.")
        );
        LocalDateTime checkOut = checkIn.plusHours(pack.getDuration());

        Room room = findAvalableRoom(checkIn, checkOut);

        if (room == null) {
            throw new RuntimeException("Sem quartos disponiveis para este horario");
        }
        booking.setRoom(room);
        booking.setClient(client);
        booking.setBookingOut(checkOut);
        booking.setStatus(BookingStatus.RESERVED);
        
        Booking savedBooking = bookingRepository.save(booking);

        return new BookingResponseDTO(
                savedBooking.getBookingId(),
                savedBooking.getBookingEnter(),
                savedBooking.getBookingOut(),
                savedBooking.getClient().getClientId(),
                savedBooking.getRoom().getRoomNumber(),
                savedBooking.getStatus()
        );

    }

    @Transactional
    public BookingResponseDTO checkIn(Long bookingId) throws BusinessException {
        Booking booking = bookingRepository.findByBookingId(bookingId);
        if (booking == null) {
            throw new SourceNotFound("Reserva nao encontrada");
        }
        
        if(booking.getStatus()!=BookingStatus.RESERVED){
            throw new BusinessException("Esta reserva nao foi confirmada");
        }

        if (booking.getBookingEnter().isAfter(LocalDateTime.now())) {
            throw new BusinessException("Aguarde, ainda nao esta na hora desta reserva");
        }

        if (LocalDateTime.now().isAfter(booking.getBookingOut())) {
            throw new BusinessException("Esta reserva expirou");
        }

        booking.setStatus(BookingStatus.CHECKED_IN);
        booking.getRoom().setStatus(RoomStatus.OCUPADO);
        Booking checkedBooking = bookingRepository.saveAndFlush(booking);
        return new BookingResponseDTO(
                checkedBooking.getBookingId(),
                checkedBooking.getBookingEnter(),
                checkedBooking.getBookingOut(),
                checkedBooking.getClient().getClientId(),
                checkedBooking.getRoom().getRoomNumber(),
                checkedBooking.getStatus()
        );

    }

    @Transactional
    public BookingResponseDTO checkOut(Long bookingId) throws BusinessException{
        Booking booking = bookingRepository.findByBookingId(bookingId);
        
        if(booking.getStatus()!= BookingStatus.CHECKED_IN){
            throw new BusinessException("Esta Hospedagem nao foi festa Check-in");
        }
        booking.setStatus(BookingStatus.COMPLETED);
        booking.getRoom().setStatus(RoomStatus.EM_LIMPEZA);
        
        return new BookingResponseDTO(
                booking.getBookingId(),
                booking.getBookingEnter(),
                booking.getBookingOut(),
                booking.getClient().getClientId(),
                booking.getRoom().getRoomNumber(),
                booking.getStatus()
        );
    }
}
