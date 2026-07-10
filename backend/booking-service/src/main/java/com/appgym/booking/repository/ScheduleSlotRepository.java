package com.appgym.booking.repository;

import com.appgym.booking.domain.ScheduleSlot;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScheduleSlotRepository extends JpaRepository<ScheduleSlot, UUID> {

    List<ScheduleSlot> findByBusinessIdAndStartTimeBetweenOrderByStartTimeAsc(
            UUID businessId, Instant from, Instant to);

    Optional<ScheduleSlot> findByIdAndBusinessId(UUID id, UUID businessId);

    /**
     * Bloquea la fila del slot durante la transaccion para serializar las
     * reservas concurrentes sobre el mismo slot y evitar sobre-reservar el cupo
     * (dos peticiones simultaneas no pueden leer el mismo recuento y confirmar
     * ambas por encima de la capacidad).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ScheduleSlot s where s.id = :id")
    Optional<ScheduleSlot> findByIdForUpdate(@Param("id") UUID id);
}
