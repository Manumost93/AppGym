import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BookingService } from '../../../core/booking/booking.service';
import { Activity, Booking, ScheduleSlot } from '../../../core/booking/booking.models';
import { BusinessService } from '../../../core/business/business.service';
import { Business } from '../../../core/business/business.models';
import { ActivityCalendarComponent } from '../../booking/activity-calendar/activity-calendar.component';

@Component({
  selector: 'app-padel',
  standalone: true,
  imports: [CommonModule, RouterLink, ActivityCalendarComponent],
  templateUrl: './padel.component.html',
})
export class PadelComponent implements OnInit {
  private readonly bookingService = inject(BookingService);
  private readonly businessService = inject(BusinessService);

  readonly heroImage =
    'https://images.pexels.com/photos/32897040/pexels-photo-32897040.jpeg?auto=compress&cs=tinysrgb&w=1600';

  readonly business = signal<Business | null>(null);
  readonly courts = signal<Activity[]>([]);
  readonly slots = signal<ScheduleSlot[]>([]);
  readonly myBookings = signal<Booking[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly bookingSlotId = signal<string | null>(null);

  readonly selectedCourtId = signal<string | null>(null);

  readonly visibleSlots = computed<ScheduleSlot[]>(() => {
    const courtId = this.selectedCourtId();
    return courtId ? this.slots().filter((s) => s.activityId === courtId) : this.slots();
  });

  ngOnInit(): void {
    this.businessService.getMyBusiness().subscribe((business) => this.business.set(business));
    this.bookingService.listActivities().subscribe((activities) => this.courts.set(activities));
    this.reload();
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

  selectCourt(courtId: string): void {
    this.selectedCourtId.set(this.selectedCourtId() === courtId ? null : courtId);
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
