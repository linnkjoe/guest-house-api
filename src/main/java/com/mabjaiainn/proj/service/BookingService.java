/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mabjaiainn.proj.service;

import com.mabjaiainn.proj.DTO.BookingResponseDTO;
import com.mabjaiainn.proj.exception.BusinessException;
import com.mabjaiainn.proj.exception.SourceNotFound;
import com.mabjaiainn.proj.entity.Booking;
import com.mabjaiainn.proj.entity.BookingStatus;
import com.mabjaiainn.proj.entity.Client;
import com.mabjaiainn.proj.entity.PaymentMethod;
import com.mabjaiainn.proj.entity.PaymentStatus;
import com.mabjaiainn.proj.entity.Packs;
import com.mabjaiainn.proj.entity.Room;
import com.mabjaiainn.proj.entity.RoomStatus;
import com.mabjaiainn.proj.repository.BookingRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
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
    private PricingService pricingService;

    public BookingService(BookingRepository bookingRepository, PackService packService, RoomService roomService,
            ClientService clientService, PricingService pricingService) {
        this.bookingRepository = bookingRepository;
        this.packService = packService;
        this.roomService = roomService;
        this.clientService = clientService;
        this.pricingService = pricingService;
    }

    @Transactional
    public Booking updateBooking(Booking booking) {
        return bookingRepository.save(booking);
    }

    @Transactional
    public List<BookingResponseDTO> retrieveAllBookings() {
        return bookingRepository.findAll().stream().map(this::toResponse).toList();
    }

    public Room findAvalableRoom(LocalDateTime checkIn, LocalDateTime checkout) {
       List<Room> rooms = roomService.findReservableRoomsForBooking();

        for (Room room : rooms) {
            if (room.getStatus() != RoomStatus.DISPONIVEL
                    && room.getStatus() != RoomStatus.RESERVADO
                    && room.getStatus() != RoomStatus.OCUPADO) {
                continue;
            }
            List<Booking> bookings = bookingRepository.findByRoom(room);
            boolean hasConflict = false;

            for (Booking booking : bookings) {
                if (booking.getStatus() == BookingStatus.CANCELED
                                || booking.getStatus() == BookingStatus.EXPIRED
                                || booking.getStatus() == BookingStatus.COMPLETED) {
                    continue;
                }
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
        return createBookingForClient(booking, clientId, packId, null);
    }

    @Transactional
    public BookingResponseDTO createBookingForClient(Booking booking, Long clientId, Long packId,
            PaymentMethod paymentMethod) throws BusinessException {

        Client client = clientService.retrieveClientById(clientId).getContent();

        if (client == null) {
            throw new SourceNotFound("Client not found");
        }

        LocalDateTime checkIn = booking.getBookingEnter();

        if (checkIn.isBefore(LocalDateTime.now())) {
            throw new BusinessException("Check-in must be in the future");
        }

        Packs pack = packService.findPackById(packId).orElseThrow(
                () -> new SourceNotFound("Package not found.")
        );
        LocalDateTime checkOut = checkIn.plusHours(pack.getDuration());

        Room room = findAvalableRoom(checkIn, checkOut);

        if (room == null) {
            throw new BusinessException("Not avalable Rooms for the providede time");
        }
        Room bookingRoom = room.getStatus() == RoomStatus.OCUPADO
                ? room
                : roomService.reserveRoom(room.getRoomId());
        booking.setRoom(bookingRoom);
        booking.setClient(client);
        booking.setPack(pack);
        BigDecimal bookingPrice = pack.getDiscountedPrice();
        booking.setAdvancedPayment(bookingPrice.divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP));
        booking.setTotalPrice(bookingPrice);
        booking.setPaymentMethod(paymentMethod);
        booking.setPaymentStatus(paymentMethod == null ? PaymentStatus.NOT_REQUIRED : PaymentStatus.PAID);
        booking.setBookingOut(checkOut);
        booking.setStatus(BookingStatus.RESERVED);

        Booking savedBooking = bookingRepository.save(booking);
        packService.recordRequest(packId);
        return toResponse(savedBooking);
    }

    @Transactional
    public BookingResponseDTO checkIn(Long bookingId) throws BusinessException {
        Booking booking = bookingRepository.findByBookingId(bookingId);
        if (booking == null) {
            throw new SourceNotFound("Booking not found");
        }

        if (booking.getStatus() != BookingStatus.RESERVED) {
            throw new BusinessException("Booking not confirmed");
        }

        if (booking.getBookingEnter().isAfter(LocalDateTime.now())) {
            throw new BusinessException("Please wait, not the time for this booking yet");
        }

        if (LocalDateTime.now().isAfter(booking.getBookingOut())) {
            throw new BusinessException("Booking Expired");
        }

        boolean anotherGuestStillCheckedIn = bookingRepository.findByRoom(booking.getRoom()).stream()
                .anyMatch(other -> other != booking && other.getStatus() == BookingStatus.CHECKED_IN);
        if (anotherGuestStillCheckedIn) {
            throw new BusinessException("The room is still occupied by another booking");
        }

        booking.setStatus(BookingStatus.CHECKED_IN);
        booking.getRoom().setStatus(RoomStatus.OCUPADO);
        booking.getRoom().setDataFimLimpeza(null);
        Booking checkedBooking = bookingRepository.saveAndFlush(booking);
        return toResponse(checkedBooking);
    }

    public BigDecimal penalty(Booking booking) {
        if (booking.getBookingOut().isBefore(LocalDateTime.now())) {
            Duration delay = Duration.between(booking.getBookingOut(), LocalDateTime.now());
            BigDecimal totalMinutes = BigDecimal.valueOf(delay.toMinutes());
            BigDecimal hours = totalMinutes.divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

            BigDecimal penalty = hours.multiply(BigDecimal.valueOf(500));
            return penalty;
        }
        return BigDecimal.ZERO;
    }

    @Transactional
    public BookingResponseDTO checkOut(Long bookingId) throws BusinessException {
        Booking booking = bookingRepository.findByBookingId(bookingId);
        if (booking == null) {
            throw new SourceNotFound("Booking not found");
        }

        if (booking.getStatus() != BookingStatus.CHECKED_IN) {
            throw new BusinessException("Did not checked-in");
        }

        booking.setPenalty(penalty(booking));
        booking.setStatus(BookingStatus.COMPLETED);
        booking.getRoom().setStatus(RoomStatus.EM_LIMPEZA);
        booking.getRoom().setDataFimLimpeza(LocalDateTime.now().plusMinutes(30));

        return toResponse(booking);
    }

    @Transactional
    public BookingResponseDTO cancelBooking(Long bookingId) throws BusinessException {
        Booking booking = bookingRepository.findByBookingId(bookingId);

        if (booking == null) {
            throw new SourceNotFound("Booking not found");
        }

        if (booking.getStatus() != BookingStatus.RESERVED) {
            throw new BusinessException("Allowed to Cancell Reserved Bookings");
        }

        if (booking.getBookingEnter().isAfter(LocalDateTime.now())) {
            booking.setTotalPrice(booking.getAdvancedPayment());
        }

        booking.setPenalty(penalty(booking));
        booking.setStatus(BookingStatus.CANCELED);
        if (booking.getRoom().getStatus() == RoomStatus.RESERVADO) {
            boolean hasOtherActiveBookings = bookingRepository.findByRoom(booking.getRoom()).stream()
                    .anyMatch(other -> !other.getBookingId().equals(bookingId)
                            && (other.getStatus() == BookingStatus.RESERVED
                            || other.getStatus() == BookingStatus.CHECKED_IN));
            if (!hasOtherActiveBookings) {
                booking.getRoom().setStatus(RoomStatus.DISPONIVEL);
            }
        }

        return toResponse(booking);

    }

    @Transactional
    public List<BookingResponseDTO> getFutureBooking() {
        LocalDateTime now = LocalDateTime.now();
        return bookingRepository.findAll().stream()
                .filter(booking -> booking.getBookingEnter().isAfter(now)
                        && booking.getStatus() != BookingStatus.CANCELED
                        && booking.getStatus() != BookingStatus.EXPIRED)
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public List<BookingResponseDTO> getBookingHistoryByGuest(Long id) {
        Client client = clientService.retrieveClientById(id).getContent();
        List<Booking> bookings = bookingRepository.findByClient(client);

        List<BookingResponseDTO> clientBookings = new java.util.ArrayList<>();
        for (Booking booking : bookings) {
            clientBookings.add(toResponse(booking));

        }
        return clientBookings;
    }  
    
    @Transactional
    public BookingResponseDTO extendBooking(Long bookingId, LocalDateTime newCheckOut) throws BusinessException {
        Booking booking = bookingRepository.findByBookingId(bookingId);
        if (booking == null) {
            throw new SourceNotFound("Booking Not Found");
        }
        Room currentRoom = booking.getRoom();
        LocalDateTime oldCheckOut = booking.getBookingOut();

        if (!newCheckOut.isAfter(oldCheckOut)) {
            throw new BusinessException("The new check-ou date must be after the actual check-out");
        }

        if (booking.getStatus() != BookingStatus.CHECKED_IN && booking.getStatus() != BookingStatus.RESERVED) {
            throw new BusinessException("Unable to extend Unchecked-in or Unreserved bookings");
        }

        boolean hasConflict = bookingRepository.findByRoom(currentRoom).stream()
                .filter(other -> !other.getBookingId().equals(bookingId))
                .filter(other -> other.getStatus() == BookingStatus.RESERVED
                        || other.getStatus() == BookingStatus.CHECKED_IN)
                .anyMatch(other -> newCheckOut.isAfter(other.getBookingEnter())
                        && oldCheckOut.isBefore(other.getBookingOut()));
        if (hasConflict) {
            throw new BusinessException("Cannot extend, the room is reserved by another client");
        }

        booking.setBookingOut(newCheckOut);
        booking.setTotalPrice(booking.getTotalPrice().add(
                pricingService.extensionPrice(Duration.between(oldCheckOut, newCheckOut))));
        return toResponse(booking);
    }

    @Transactional
    public BookingResponseDTO getBookingById(Long id) throws BusinessException {
        Booking booking = bookingRepository.findByBookingId(id);
        if (booking == null) {
            throw new BusinessException("Booking not Found");
        }
        return toResponse(booking);
    }
    
    @Transactional
    public List<BookingResponseDTO> getCurrentBooking(Long id){
        Client client = clientService.retrieveClientById(id).getContent();
        if(client==null){
            throw new SourceNotFound("Client Not Found");
        }
        
        List<Booking> allBookings = bookingRepository.findByClient(client);
        List<BookingResponseDTO> currentBookings = new java.util.ArrayList<>();
        
        for(Booking booking : allBookings){
            if(booking.getStatus() != BookingStatus.CANCELED &&
               booking.getStatus() != BookingStatus.COMPLETED &&
               booking.getStatus() != BookingStatus.EXPIRED){
                currentBookings.add(toResponse(booking));
            }
          }
        return currentBookings;
    }

    private BookingResponseDTO toResponse(Booking booking) {
        return new BookingResponseDTO(
                booking.getBookingId(),
                booking.getRoom().getRoomId(),
                booking.getRoom().getRoomNumber(),
                booking.getBookingEnter(),
                booking.getBookingOut(),
                booking.getClient().getClientId(),
                booking.getStatus(),
                booking.getAdvancedPayment(),
                booking.getTotalPrice(),
                booking.getPenalty(),
                booking.getPaymentMethod(),
                booking.getPaymentStatus());
    }

    }
