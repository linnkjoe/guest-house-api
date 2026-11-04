package com.mabjaiainn.proj.service;

import com.mabjaiainn.proj.entity.Room;
import com.mabjaiainn.proj.entity.RoomStatus;
import com.mabjaiainn.proj.exception.BusinessException;
import com.mabjaiainn.proj.repository.RoomRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomService roomService;

    @Test
    void reservingAvailableRoomChangesItsLifecycleState() throws BusinessException {
        Room room = new Room(101, RoomStatus.DISPONIVEL);
        room.setRoomId(12L);
        when(roomRepository.findByRoomId(12L)).thenReturn(room);

        Room reserved = roomService.reserveRoom(12L);

        assertEquals(RoomStatus.RESERVADO, reserved.getStatus());
    }

    @Test
    void roomWithActiveReservationCannotBeMarkedAvailable() {
        Room room = new Room(101, RoomStatus.RESERVADO);
        room.setRoomId(12L);
        when(roomRepository.findByRoomId(12L)).thenReturn(room);

        assertThrows(BusinessException.class, () -> roomService.markRoomAsAvalable(12L));
    }

    @Test
    void startingCleaningSetsReleaseDeadline() throws BusinessException {
        Room room = new Room(101, RoomStatus.DISPONIVEL);
        room.setRoomId(12L);
        when(roomRepository.findByRoomId(12L)).thenReturn(room);

        roomService.startCleaning(12L);

        assertEquals(RoomStatus.EM_LIMPEZA, room.getStatus());
        assertNotNull(room.getDataFimLimpeza());
    }
}
