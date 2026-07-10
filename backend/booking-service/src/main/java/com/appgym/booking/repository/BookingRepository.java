package com.appgym.booking.repository;

import com.appgym.booking.domain.Booking;
import com.appgym.common.dto.BookingStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    long countBySlotIdAndStatus(UUID slotId, BookingStatus status);

    List<Booking> findByMemberIdOrderByCreatedAtDesc(UUID memberId);

    Optional<Booking> findByIdAndMemberId(UUID id, UUID memberId);

    Optional<Booking> findFirstBySlotIdAndStatusOrderByCreatedAtAsc(UUID slotId, BookingStatus status);

    boolean existsBySlotIdAndMemberIdAndStatusNot(UUID slotId, UUID memberId, BookingStatus status);
}
