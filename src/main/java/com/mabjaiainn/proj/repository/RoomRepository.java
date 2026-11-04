/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mabjaiainn.proj.repository;

import com.mabjaiainn.proj.entity.Room;
import com.mabjaiainn.proj.entity.RoomStatus;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author linnkjoe
 */
public interface RoomRepository extends JpaRepository<Room, Long>{
    List<Room> findByStatus(RoomStatus status);
    List<Room> findByStatusAndDataFimLimpezaLessThanEqual(RoomStatus status, LocalDateTime dataFimLimpeza);
    List<Room> findByStatusAndDataFimLimpezaIsNull(RoomStatus status);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select room from Room room where room.status in :statuses order by room.roomNumber")
    List<Room> findReservableRoomsForUpdate(@Param("statuses") Collection<RoomStatus> statuses);
    Room findByRoomId(Long roomId);
    
    
    
}
