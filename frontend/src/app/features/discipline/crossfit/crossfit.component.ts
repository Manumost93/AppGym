import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BookingService } from '../../../core/booking/booking.service';
import { Booking, ScheduleSlot } from '../../../core/booking/booking.models';
import { AiService } from '../../../core/ai/ai.service';
import { RecommendationResponse } from '../../../core/ai/ai.models';
import { BusinessService } from '../../../core/business/business.service';
import { Business } from '../../../core/business/business.models';
import { ActivityCalendarComponent } from '../../booking/activity-calendar/activity-calendar.component';

@Component({
  selector: 'app-crossfit',
  standalone: true,
  imports: [CommonModule, RouterLink, ActivityCalendarComponent],
  templateUrl: './crossfit.component.html',
})
export class CrossfitComponent implements OnInit {
  private readonly bookingService = inject(BookingService);
  private readonly aiService = inject(AiService);
  private readonly businessService = inject(BusinessService);

  readonly heroImage =
    'https://images.pexels.com/photos/37972529/pexels-photo-37972529.jpeg?auto=compress&cs=tinysrgb&w=1600';

  readonly business = signal<Business | null>(null);
  readonly slots = signal<ScheduleSlot[]>([]);
  readonly myBookings = signal<Booking[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly bookingSlotId = signal<string | null>(null);

  readonly recommendation = signal<RecommendationResponse | null>(null);
  readonly loadingRecommendation = signal(true);

  // El WOD mas proximo en el tiempo entre las franjas disponibles, para
  // destacarlo como "el WOD de hoy" independientemente de cuando se mire la demo.
  readonly todaysWod = computed<ScheduleSlot | null>(() => {
    const upcoming = this.slots().filter((s) => !s.full || this.activeBookingForSlot(s.id));
    return upcoming.length > 0 ? upcoming[0] : null;
  });

  readonly remainingSlots = computed<ScheduleSlot[]>(() => {
    const wod = this.todaysWod();
    return wod ? this.slots().filter((s) => s.id !== wod.id) : this.slots();
  });

  ngOnInit(): void {
    this.businessService.getMyBusiness().subscribe((business) => this.business.set(business));
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
