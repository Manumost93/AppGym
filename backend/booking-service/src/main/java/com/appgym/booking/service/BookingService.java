package com.appgym.booking.service;

import com.appgym.booking.domain.Activity;
import com.appgym.booking.domain.Booking;
import com.appgym.booking.domain.ScheduleSlot;
import com.appgym.booking.repository.BookingRepository;
import com.appgym.booking.repository.ScheduleSlotRepository;
import com.appgym.booking.web.dto.BookingResponse;
import com.appgym.common.dto.BookingStatus;
import com.appgym.common.security.ForbiddenException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ScheduleSlotRepository slotRepository;
    private final ActivityService activityService;

    public BookingService(BookingRepository bookingRepository, ScheduleSlotRepository slotRepository,
                           ActivityService activityService) {
        this.bookingRepository = bookingRepository;
        this.slotRepository = slotRepository;
        this.activityService = activityService;
    }

    /**
     * Bloquea la fila del slot (SELECT ... FOR UPDATE) para que dos reservas
     * concurrentes sobre el mismo slot se serialicen: la segunda transaccion
     * espera a que la primera confirme antes de contar las plazas ocupadas,
     * evitando sobre-reservar el cupo.
     */
    @Transactional
    public BookingResponse book(UUID businessId, UUID memberId, UUID slotId) {
        ScheduleSlot slot = slotRepository.findByIdForUpdate(slotId)
                .orElseThrow(() -> new NoSuchElementException("Franja horaria no encontrada: " + slotId));

        if (!slot.getBusinessId().equals(businessId)) {
            throw new ForbiddenException("La franja horaria no pertenece a tu negocio");
        }
        if (bookingRepository.existsBySlotIdAndMemberIdAndStatusNot(slotId, memberId, BookingStatus.CANCELLED)) {
            throw new IllegalStateException("Ya tienes una reserva activa para esta franja horaria");
        }

        long confirmedCount = bookingRepository.countBySlotIdAndStatus(slotId, BookingStatus.CONFIRMED);
        BookingStatus status = confirmedCount < slot.getCapacity() ? BookingStatus.CONFIRMED : BookingStatus.WAITLIST;

        Booking booking = new Booking(slotId, businessId, memberId, status);
        // flush inmediato: @CreationTimestamp solo rellena createdAt al volcar a BD,
        // y la respuesta se construye justo despues, en la misma transaccion.
        bookingRepository.saveAndFlush(booking);

        return toResponse(booking, slot, activityService.findOwned(businessId, slot.getActivityId()));
    }

    public List<BookingResponse> listMine(UUID memberId) {
        return bookingRepository.findByMemberIdOrderByCreatedAtDesc(memberId).stream()
                .map(this::toResponseUnsafe)
                .toList();
    }

    @Transactional
    public void cancel(UUID memberId, UUID bookingId) {
        Booking booking = bookingRepository.findByIdAndMemberId(bookingId, memberId)
                .orElseThrow(() -> new NoSuchElementException("Reserva no encontrada: " + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return;
        }

        boolean wasConfirmed = booking.getStatus() == BookingStatus.CONFIRMED;
        booking.cancel();

        if (wasConfirmed) {
            // Bloqueamos el slot para que la promocion de lista de espera no
            // compita con una reserva nueva que estuviera contando plazas a la vez.
            slotRepository.findByIdForUpdate(booking.getSlotId());
            bookingRepository.findFirstBySlotIdAndStatusOrderByCreatedAtAsc(booking.getSlotId(), BookingStatus.WAITLIST)
                    .ifPresent(Booking::confirm);
        }
    }

    private BookingResponse toResponseUnsafe(Booking booking) {
        ScheduleSlot slot = slotRepository.findById(booking.getSlotId()).orElse(null);
        if (slot == null) {
            return new BookingResponse(booking.getId(), booking.getSlotId(), "(actividad eliminada)", null,
                    booking.getMemberId(), booking.getStatus(), booking.getCreatedAt());
        }
        Activity activity = activityService.findOwned(booking.getBusinessId(), slot.getActivityId());
        return toResponse(booking, slot, activity);
    }

    private BookingResponse toResponse(Booking booking, ScheduleSlot slot, Activity activity) {
        return new BookingResponse(
                booking.getId(),
                slot.getId(),
                activity.getName(),
                slot.getStartTime(),
                booking.getMemberId(),
                booking.getStatus(),
                booking.getCreatedAt()
        );
    }
}
