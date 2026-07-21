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
import com.mabjaiainn.proj.entity.Packs;
import com.mabjaiainn.proj.entity.Room;
import com.mabjaiainn.proj.entity.RoomStatus;
import com.mabjaiainn.proj.repository.BookingRepository;
import com.mabjaiainn.proj.repository.ClientRepository;
import com.mabjaiainn.proj.repository.PacksRepository;
import com.mabjaiainn.proj.repository.RoomRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
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
        booking.setRoom(room);
        booking.setClient(client);
        booking.setAdvancedPayment(pack.getPackPrice().divide(BigDecimal.valueOf(2)));
        booking.setTotalPrice(pack.getPackPrice());
        booking.setBookingOut(checkOut);
        booking.setStatus(BookingStatus.RESERVED);

        Booking savedBooking = bookingRepository.save(booking);

        return new BookingResponseDTO(
                savedBooking.getBookingId(),
                savedBooking.getRoom().getRoomId(),
                savedBooking.getBookingEnter(),
                savedBooking.getBookingOut(),
                savedBooking.getClient().getClientId(),
                savedBooking.getStatus(),
                savedBooking.getAdvancedPayment(),
                savedBooking.getTotalPrice(),
                savedBooking.getPenalty()
        );

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

        booking.setStatus(BookingStatus.CHECKED_IN);
        booking.getRoom().setStatus(RoomStatus.OCUPADO);
        Booking checkedBooking = bookingRepository.saveAndFlush(booking);
        return new BookingResponseDTO(
                checkedBooking.getBookingId(),
                checkedBooking.getRoom().getRoomId(),
                checkedBooking.getBookingEnter(),
                checkedBooking.getBookingOut(),
                checkedBooking.getClient().getClientId(),
                checkedBooking.getStatus(),
                checkedBooking.getAdvancedPayment(),
                checkedBooking.getTotalPrice(),
                checkedBooking.getPenalty()
        );

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

        return new BookingResponseDTO(
                booking.getBookingId(),
                booking.getRoom().getRoomId(),
                booking.getBookingEnter(),
                booking.getBookingOut(),
                booking.getClient().getClientId(),
                booking.getStatus(),
                booking.getAdvancedPayment(),
                booking.getTotalPrice(),
                booking.getPenalty()
        );
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

        return new BookingResponseDTO(
                booking.getBookingId(),
                booking.getRoom().getRoomId(),
                booking.getBookingEnter(),
                booking.getBookingOut(),
                booking.getClient().getClientId(),
                booking.getStatus(),
                booking.getAdvancedPayment(),
                booking.getTotalPrice(),
                booking.getPenalty()
        );

    }

    public List<BookingResponseDTO> getFutureBooking() {
        List<Booking> bookings = retrieveAllBookings();
        List<BookingResponseDTO> futureBookings = new java.util.ArrayList<>();

        for (Booking booking : bookings) {
            if (booking.getBookingEnter().isAfter(LocalDateTime.now()) && booking.getStatus() != BookingStatus.CANCELED) {
                futureBookings.add(new BookingResponseDTO(
                        booking.getBookingId(),
                        booking.getRoom().getRoomId(),
                        booking.getBookingEnter(),
                        booking.getBookingOut(),
                        booking.getClient().getClientId(),
                        booking.getStatus(),
                        booking.getAdvancedPayment(),
                        booking.getTotalPrice(),
                        booking.getPenalty()));

            }
        }
        return futureBookings;
    }

    public List<BookingResponseDTO> getBookingHistoryByGuest(Long id) {
        Client client = clientService.retrieveClientById(id).getContent();
        List<Booking> bookings = bookingRepository.findByClient(client);

        List<BookingResponseDTO> clientBookings = new java.util.ArrayList<>();
        for (Booking booking : bookings) {
            clientBookings.add(new BookingResponseDTO(
                    booking.getBookingId(),
                    booking.getBookingId(),
                    booking.getBookingEnter(),
                    booking.getBookingOut(),
                    booking.getClient().getClientId(),
                    booking.getStatus(),
                    booking.getAdvancedPayment(),
                    booking.getTotalPrice(),
                    booking.getPenalty()));

        }
        return clientBookings;
    }  
    
    @Transactional
    public BookingResponseDTO extendBooking(Long bookingId, LocalDateTime newCheckOut) throws BusinessException{
        Booking booking = bookingRepository.findByBookingId(bookingId);
        if(booking == null){
            throw new SourceNotFound("Booking Not Found");
        }
         Room currentRoom = booking.getRoom();
        
         LocalDateTime oldCheckOut = booking.getBookingOut();
         
         if(newCheckOut.isBefore(oldCheckOut)){
             throw new BusinessException("The new check-ou date must be after the actual check-out");
         }
         
         if(booking.getStatus()!=BookingStatus.CHECKED_IN && booking.getStatus()!=BookingStatus.RESERVED){
             throw new BusinessException("Unable to extend Unchecked-in or Unreserved bookings");
         }
         
        List<Booking> otherBookings = bookingRepository.findByRoom(currentRoom);
            for(Booking other:otherBookings){
                if(other.getBookingId().equals(bookingId)){
                continue;
                }
            
                if(oldCheckOut.isBefore(other.getBookingOut()) && newCheckOut.isAfter(other.getBookingEnter())){
                    throw new BusinessException("Cannot extend, the room is reserved by Another Client");
                }
            }
            
            Duration extensionDuration = Duration.between(oldCheckOut, newCheckOut);
            BigDecimal extensionMinutes = BigDecimal.valueOf(extensionDuration.toMinutes());
            BigDecimal extensionHours = extensionMinutes.divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

            BigDecimal hourlyRate = BigDecimal.valueOf(500);
            BigDecimal aditionalPrice = extensionHours.multiply(hourlyRate);
            BigDecimal currentPrice = booking.getTotalPrice();
            BigDecimal newTotalPice = currentPrice.add(aditionalPrice);
            
            booking.setBookingOut(newCheckOut);
            booking.setTotalPrice(newTotalPice);
             
            return new BookingResponseDTO(
                booking.getBookingId(),
                booking.getRoom().getRoomId(),
                booking.getBookingEnter(),
                booking.getBookingOut(),
                booking.getClient().getClientId(),
                booking.getStatus(),
                booking.getAdvancedPayment(),
                booking.getTotalPrice(),
                booking.getPenalty()
        );
        }
    
    public BookingResponseDTO getBookingById(Long id) throws BusinessException{
        Booking booking = bookingRepository.findByBookingId(id);
        if(booking == null){
            throw new BusinessException("Booking not Found");
        }
        
         return new BookingResponseDTO(
                booking.getBookingId(),
                 booking.getRoom().getRoomId(),
                booking.getBookingEnter(),
                booking.getBookingOut(),
                booking.getClient().getClientId(),
                booking.getStatus(),
                booking.getAdvancedPayment(),
                booking.getTotalPrice(),
                booking.getPenalty()
        );   
    }
    
    public List<BookingResponseDTO> getCurrentBooking(Long id){
        Client client = clientService.retrieveClientById(id).getContent();
        if(client==null){
            throw new SourceNotFound("Client Not Found");
        }
        
        List<Booking> allBookings = bookingRepository.findByClient(client);
        List<BookingResponseDTO> currentBookings = new java.util.ArrayList<>();
        
        for(Booking booking : allBookings){
            if(booking.getStatus() != BookingStatus.CANCELED && 
               booking.getStatus() != BookingStatus.COMPLETED){
                currentBookings.add(new BookingResponseDTO(
                        booking.getBookingId(),
                        booking.getRoom().getRoomId(),
                        booking.getBookingEnter(),
                        booking.getBookingOut(),
                        booking.getClient().getClientId(),
                        booking.getStatus(),
                        booking.getAdvancedPayment(),
                        booking.getTotalPrice(),
                        booking.getPenalty()));
                
                
            }
          }
        return currentBookings;
    }
        
    }
