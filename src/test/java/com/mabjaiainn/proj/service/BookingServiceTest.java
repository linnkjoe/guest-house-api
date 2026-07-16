/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.mabjaiainn.proj.service;

import com.mabjaiainn.proj.DTO.BookingResponseDTO;
import com.mabjaiainn.proj.exception.BusinessException;
import com.mabjaiainn.proj.exception.SourceNotFound;
import com.mabjaiainn.proj.model.Booking;
import com.mabjaiainn.proj.model.BookingStatus;
import com.mabjaiainn.proj.model.Client;
import com.mabjaiainn.proj.model.Packs;
import com.mabjaiainn.proj.model.Room;
import com.mabjaiainn.proj.model.RoomStatus;
import com.mabjaiainn.proj.repository.BookingRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.EntityModel;

/**
 *
 * @author linnkjoe
 */
@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
  
    @Mock
    private RoomService roomService;
    
    @Mock 
    private ClientService clientService;
    
    @Mock
    private PackService packService;
    
    @Mock
    private BookingRepository bookingRepository;
    
    @InjectMocks
    private BookingService bookingService;


    @Test
    void shouldCreateBooking_basicScenario() throws BusinessException{
        LocalDateTime bookingTime = LocalDateTime.now().plusHours(2);
        Long clientId = 1L;
        Long packId = 2L;
        
        Client mockClient = new Client();
        mockClient.setClientId(clientId);
        when(clientService.retrieveClientById(clientId)).thenReturn(EntityModel.of(mockClient));
        
        
        Packs mockPack = new Packs();
        mockPack.setPackId(packId);
        mockPack.setDuration(24);
        mockPack.setPackPrice(BigDecimal.valueOf(10000));
        when(packService.findPackById(packId)).thenReturn(Optional.of(mockPack));
        
        Room room = new Room();
        room.setRoomId(12L);
        room.setRoomNumber(100);
        room.setStatus(RoomStatus.DISPONIVEL);
        when(roomService.retrieveAllRooms()).thenReturn( List.of(room));
       
        
        Booking booking = new Booking(bookingTime);
        booking.setClient(mockClient);
        booking.setPack(mockPack);
        booking.setAdvancedPayment(mockPack.getPackPrice().divide(BigDecimal.valueOf(2)));
        booking.setTotalPrice(mockPack.getPackPrice());
        booking.setBookingId(2L);
        booking.setStatus(BookingStatus.RESERVED);
        booking.setPenalty(BigDecimal.ZERO);
        booking.setBookingOut(bookingTime.plusHours(24));
        booking.setRoom(room);
        booking.setBookingId(2L);
        
        
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        
        BookingResponseDTO response = bookingService.createBookingForClient(booking, clientId, packId);
        
        
        //        checkedBooking.getBookingId(),
//                checkedBooking.getBookingEnter(),
//                checkedBooking.getBookingOut(),
//                checkedBooking.getClient().getClientId(),
//                checkedBooking.getRoom().getRoomNumber(),
//                checkedBooking.getStatus(),
//                checkedBooking.getAdvancedPayment(),
//                checkedBooking.getTotalPrice(),
//                checkedBooking.getPenalty()
        
        
        assertNotNull(response);
        assertEquals(2L, response.id());
        assertEquals(bookingTime, response.bookingEnter());
        assertEquals(bookingTime.plusHours(24), response.bookingOut());
        assertEquals(clientId, response.clientId());
        assertEquals(BookingStatus.RESERVED, booking.getStatus());
        
    }
    
    @Test
    void shouldThrowNotFound_missingClientIdScenario(){
    LocalDateTime bookingTime = LocalDateTime.now().plusHours(2);
        Long clientId = 1L;
        Long packId = 2L;
        
        Booking inputBooking = new Booking(bookingTime);
        
        Client mockClient = new Client();
        mockClient.setClientId(clientId);
        when(clientService.retrieveClientById(5L)).thenThrow(new SourceNotFound("ClientNotFound"));
        
        assertThrows(SourceNotFound.class, ()-> 
        {bookingService.createBookingForClient(inputBooking, 5L, packId);});
    };
    

    @Test
    void shouldThrowNotFound_missingPackIdScenario(){
    LocalDateTime bookingTime = LocalDateTime.now().plusHours(2);
        Long clientId = 1L;
        Long packId = 2L;
        
        Booking inputBooking = new Booking(bookingTime);
        
        Client mockClient = new Client();
        mockClient.setClientId(clientId);
        Packs mockPack = new Packs();
        mockPack.setPackId(5L);
        
        when(clientService.retrieveClientById(clientId))
                .thenReturn(EntityModel.of(mockClient));        
        when(packService.findPackById(5L)).thenThrow(new SourceNotFound("Package Not FOund"));
        
        assertThrows(SourceNotFound.class, ()-> 
        {bookingService.createBookingForClient(inputBooking, clientId, 5L);});
    };

    
    @Test
    void shouldThrowBusinessException_pastCheckinScenario() throws BusinessException{
    LocalDateTime bookingTime = LocalDateTime.now().plusHours(-24);
        Long clientId = 1L;
        Long packId = 2L;
        
        Booking inputBooking = new Booking(bookingTime);
        
        Client mockClient = new Client();
        mockClient.setClientId(clientId);
        Packs mockPack = new Packs();
        mockPack.setPackId(5L);
        
        when(clientService.retrieveClientById(clientId))
                .thenReturn(EntityModel.of(mockClient));        
        
        
        assertThrows(BusinessException.class, ()-> 
        {bookingService.createBookingForClient(inputBooking, clientId, 5L);});
    };

       @Test
       void shouldCheckIn() throws BusinessException{
           
           Long bookingId = 5L;
           Long roomId = 2L;
           Long clientId = 3L;
           Booking inputBooking = new Booking();
          
           Client client = new Client();
           client.setClientId(clientId);
           
           
           Room room = new Room();
           room.setRoomId(roomId);
           room.setRoomNumber(100);
           room.setStatus(RoomStatus.DISPONIVEL);
           
           
           inputBooking.setStatus(BookingStatus.RESERVED);
           inputBooking.setRoom(room);
           inputBooking.setBookingEnter(LocalDateTime.now());
           inputBooking.setBookingOut(LocalDateTime.now().plusHours(2));
           inputBooking.setClient(client);
           
           when(bookingRepository.findByBookingId(bookingId)).thenReturn(inputBooking);
           
           when(bookingRepository.saveAndFlush(inputBooking)).thenReturn(inputBooking);
           assertNotNull(inputBooking);

           bookingService.checkIn(bookingId);
           
                      assertEquals(BookingStatus.CHECKED_IN, inputBooking.getStatus());
           assertEquals(RoomStatus.OCUPADO, inputBooking.getRoom().getStatus());
           
       }
       
       @Test
       void shouldThrowNotFound_bookingCheckin(){
           Long bookingId = 5L;
           Booking inputBooking = new Booking();
           inputBooking.setBookingId(bookingId);
           
            when(bookingRepository.findByBookingId(7L)).
                    thenThrow( new SourceNotFound("Booking Not Found"));
              
            assertThrows(SourceNotFound.class, ()->
                    {bookingService.checkIn(7L);}); 
           
       }
       
       @Test
       void shoulThrowBusinessException_reservedBookingsOnly() throws BusinessException{
           Long bookingId = 5L;
           
           Booking inputBooking = new Booking();
           inputBooking.setStatus(BookingStatus.CANCELED);
           inputBooking.setBookingId(bookingId);
           when(bookingRepository.findByBookingId(bookingId)).thenReturn(inputBooking);
           
           
           assertThrows(BusinessException.class,()->
                    {bookingService.checkIn(bookingId);});
                  
       }
       
       @Test
       void shouldThrowBusinessException_notInTime(){
           Long bookingId = 5L;
           LocalDateTime checkInTime = LocalDateTime.now().plusHours(2);
           
           Booking inpuBooking = new Booking();
           inpuBooking.setStatus(BookingStatus.RESERVED);
           inpuBooking.setBookingId(bookingId);
           inpuBooking.setBookingEnter(checkInTime);
           
           when(bookingRepository.findByBookingId(bookingId)).thenReturn(inpuBooking);
           
           assertThrows(BusinessException.class, ()->
                    {bookingService.checkIn(bookingId);});
       }
       
       @Test
       void shouldThrowBusinessException_expiredBooking(){
           Long bookingId = 5L;
           
           LocalDateTime checkInTime = LocalDateTime.now().plusHours(-4);
           LocalDateTime checkOutTime = LocalDateTime.now().plusHours(-2);
           
           Booking inputBooking = new Booking();
           
           inputBooking.setBookingId(bookingId);
           inputBooking.setBookingEnter(checkInTime);
           inputBooking.setBookingOut(checkOutTime);
           
           when(bookingRepository.findByBookingId(bookingId)).thenReturn(inputBooking);
           
           assertThrows(BusinessException.class, ()->
           {bookingService.checkIn(bookingId);}
           );
           
       }
       
       @Test
       void shouldCheckOu_basicScenario() throws BusinessException{
           Long bookingId = 2L;
           Long roomId = 3L;
           
           
           Client mockClient = new Client();
           mockClient.setClientId(4L);
           
           Room mockRoom = new Room();
           mockRoom.setRoomId(10L);
           mockRoom.setStatus(RoomStatus.EM_LIMPEZA);
           mockRoom.setRoomNumber(100);
           
           Booking booking = new Booking();
           booking.setStatus(BookingStatus.CHECKED_IN);
           booking.setBookingOut(LocalDateTime.now().plusMinutes(30));
           booking.setBookingId(bookingId);
           booking.setRoom(mockRoom);
           booking.setClient(mockClient);
           booking.setPenalty(BigDecimal.ZERO);
           when(bookingRepository.findByBookingId(bookingId)).thenReturn(booking);
           
           bookingService.checkOut(bookingId);
           
           assertEquals(RoomStatus.EM_LIMPEZA, booking.getRoom().getStatus());
           assertEquals(BookingStatus.COMPLETED, booking.getStatus());
           assertEquals(BigDecimal.ZERO, booking.getPenalty());
       }
       

       @Test
       void shouldApplyPenalty() throws BusinessException{
       Long bookingId = 2L;
           Long roomId = 3L;
           
           
           Client mockClient = new Client();
           mockClient.setClientId(4L);
           
           Room mockRoom = new Room();
           mockRoom.setRoomId(10L);
           mockRoom.setStatus(RoomStatus.EM_LIMPEZA);
           mockRoom.setRoomNumber(100);
           
           Booking booking = new Booking();
           booking.setStatus(BookingStatus.CHECKED_IN);
           booking.setBookingOut(LocalDateTime.now().plusHours(-2));
           booking.setBookingId(bookingId);
           booking.setRoom(mockRoom);
           booking.setClient(mockClient);
           booking.setPenalty(BigDecimal.ZERO);
           when(bookingRepository.findByBookingId(bookingId)).thenReturn(booking);
           
           bookingService.checkOut(bookingId);
           
           
           assertEquals(RoomStatus.EM_LIMPEZA, booking.getRoom().getStatus());
           assertEquals(BookingStatus.COMPLETED, booking.getStatus());
           assertEquals(new BigDecimal("1000.00"), booking.getPenalty());

       
       }
       
       @Test
       void shouldCancel() throws BusinessException{
          Long bookingId = 2L;
           Long roomId = 3L;
           
           
           Client mockClient = new Client();
           mockClient.setClientId(4L);
           
           Room mockRoom = new Room();
           mockRoom.setRoomId(10L);
           mockRoom.setStatus(RoomStatus.EM_LIMPEZA);
           mockRoom.setRoomNumber(100);
           
           Booking booking = new Booking();
           booking.setStatus(BookingStatus.RESERVED);
           booking.setBookingEnter(LocalDateTime.now().plusMinutes(30));
           booking.setBookingOut(LocalDateTime.now().plusHours(5));
           booking.setBookingId(bookingId);
           booking.setRoom(mockRoom);
           booking.setClient(mockClient);
           booking.setPenalty(BigDecimal.ZERO);
           when(bookingRepository.findByBookingId(bookingId)).thenReturn(booking);
           
           bookingService.cancelBooking(bookingId);
           
           assertEquals(BookingStatus.CANCELED, booking.getStatus());
           assertEquals(BigDecimal.ZERO, booking.getPenalty());
                   
           
       }
       
       
    
    }



    
    
        

