import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BookingService } from '../../../core/booking/booking.service';
import { Booking, ScheduleSlot } from '../../../core/booking/booking.models';

@Component({
  selector: 'app-schedule',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './schedule.component.html',
})
export class ScheduleComponent implements OnInit {
  private readonly bookingService = inject(BookingService);

  readonly slots = signal<ScheduleSlot[]>([]);
  readonly myBookings = signal<Booking[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly bookingSlotId = signal<string | null>(null);

  ngOnInit(): void {
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
