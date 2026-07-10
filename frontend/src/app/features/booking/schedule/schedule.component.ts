import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BookingService } from '../../../core/booking/booking.service';
import { Booking, ScheduleSlot } from '../../../core/booking/booking.models';
import { AiService } from '../../../core/ai/ai.service';
import { RecommendationResponse } from '../../../core/ai/ai.models';

@Component({
  selector: 'app-schedule',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './schedule.component.html',
})
export class ScheduleComponent implements OnInit {
  private readonly bookingService = inject(BookingService);
  private readonly aiService = inject(AiService);

  readonly slots = signal<ScheduleSlot[]>([]);
  readonly myBookings = signal<Booking[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly bookingSlotId = signal<string | null>(null);

  readonly recommendation = signal<RecommendationResponse | null>(null);
  readonly loadingRecommendation = signal(true);

  ngOnInit(): void {
    this.reload();
    this.loadRecommendation();
  }

  reload(): void {
    this.loading.set(true);
    this.bookingService.listSlots().subscribe({
      next: (slots) => {
        this.slots.set(slots);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
    this.bookingService.listMyBookings().subscribe((bookings) => this.myBookings.set(bookings));
  }

  loadRecommendation(): void {
    this.loadingRecommendation.set(true);
    this.aiService.recommend().subscribe({
      next: (recommendation) => {
        this.recommendation.set(recommendation);
        this.loadingRecommendation.set(false);
      },
      // El asistente de IA es un extra: si falla (p. ej. sin clave configurada),
      // el resto de la pagina de reservas debe seguir funcionando con normalidad.
      error: () => this.loadingRecommendation.set(false),
    });
  }

  activeBookingForSlot(slotId: string): Booking | undefined {
    return this.myBookings().find((b) => b.slotId === slotId && b.status !== 'CANCELLED');
  }

  book(slot: ScheduleSlot): void {
    this.bookingSlotId.set(slot.id);
    this.errorMessage.set(null);

    this.bookingService.book(slot.id).subscribe({
      next: () => {
        this.bookingSlotId.set(null);
        this.reload();
      },
      error: (err) => {
        this.bookingSlotId.set(null);
        this.errorMessage.set(err.error?.message ?? 'No se pudo reservar.');
      },
    });
  }

  cancel(booking: Booking): void {
    this.bookingService.cancelBooking(booking.id).subscribe(() => this.reload());
  }
}
